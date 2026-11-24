/*
    Copyright 2018-2026 Dmitry Isaenko
    
    This file is part of mplayer4anime.

    mplayer4anime is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    mplayer4anime is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with mplayer4anime.  If not, see <https://www.gnu.org/licenses/>.
 */
package mplayer4anime.mplayer;

import mplayer4anime.AppPreferences;
import mplayer4anime.IPlayer;
import mplayer4anime.PlayRequest;
import mplayer4anime.ui.ServiceWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class MplayerSlave implements IPlayer {
    private static final Logger log = LoggerFactory.getLogger(MplayerSlave.class);

    private volatile Process player;
    private volatile PrintStream playerIn;
    // Sole owner of the shared pipe writer: the pumps never close it, only dispose() (or the end watcher, once the
    // process has died) does, so the output reader is not broken by a premature EOF while a command reply is awaited.
    private PipedOutputStream playerOutErrWriter;
    private boolean paused = false;
    // The slave process stays alive after `stop`, so a `pause` toggle then does nothing: flag the stopped state so
    // Play restarts the file instead of toggling the pause.
    private volatile boolean stopped = false;
    // Set once the app tears the process down on purpose, so the watcher thread does not treat that exit as a
    // finished file.
    private volatile boolean disposed = false;
    // The sole reader of the mplayer output: every line passes through it,  so an END_FILE line can never be missed
    // by a competing reader.
    private Thread outputReader;
    // Command replies (ANS_* lines) collected by the output reader, waiting  to be consumed by the getters that issued
    // the corresponding commands.
    private final Queue<String> pendingReplies = new ConcurrentLinkedQueue<>();
    // An end-of-file line has appeared in the output (see isEndOfFileLine).
    // Note: on this Gentoo build (only?) closing mplayer's own window prints the same line, so the end watcher
    // additionally requires the last known position to be at the end of the file (see reachedEndOfFile).
    private volatile boolean eofSeen = false;
    // The last reported playback position and the file length, maintained by the position prober while the process
    // runs. They are what let the end watcher distinguish a natural end (position at the file end)
    // from the user closing mplayer's own window (position mid-file).
    private volatile double lastKnownPos = -1;
    private volatile double fileLength = -1;
    private volatile Runnable onFinishedListener;

    private final ResourceBundle resourceBundle;

    public MplayerSlave(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    private boolean playerSingleCommand(String command) {
        if (player == null || !player.isAlive())
            return false;

        // In mplayer any slave command that arrives while paused resumes playback before it is executed.
        // Prefixing with pausing_keep_force makes the command run and  keep the pause. `pause` itself must
        // stay unprefixed: it is the toggle (mplayer drops the `pause` command that breaks out of the pause
        // loop, so it resumes while paused).
        if (paused && !"pause".equals(command))
            command = "pausing_keep_force " + command;

        playerIn.print(command);
        playerIn.print("\n");
        playerIn.flush();
        return true;
    }

    @Override
    public void subtitlesSwitch() {
        if (!playerSingleCommand("get_sub_visibility"))
            return;

        try {
            var returnedStr = waitForReply("ANS_SUB_VISIBILITY=");
            var returnedInt = returnedStr == null ? 1
                    : Integer.parseInt(returnedStr.substring("ANS_SUB_VISIBILITY=".length()));
            playerSingleCommand("sub_visibility %d".formatted((returnedInt == 1) ? 0 : 1));
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Can't determine whether subtitles enabled or disabled", e);
        }
    }

    @Override
    public void fullscreenSwitch() {
        playerSingleCommand("vo_fullscreen");
    }

    @Override
    public void mute() {
        playerSingleCommand("mute");
    }

    @Override
    public void forcePlay(PlayRequest request) {
        dispose();
        playPause(request);
    }

    @Override
    public void playPause(PlayRequest request) {
        if (!stopped && togglePause())
            return;

        var isAudio = request.audio() != null;
        var isSubtitles = request.subtitles() != null;
        var SubEncodingDefault = request.subtitlesEncoding().equals("default");
        var startPosition = request.startPositionSeconds() > 0;

        try {
            var args = Stream.of(
                            AppPreferences.get().getPath(),   // It's a chance for Windows ;)
                            "-slave",
                            isAudio ? "-audiofile" : "",
                            isAudio ? request.audio() : "",
                            "-quiet",
                            request.fullscreen() ? "-fs" : "",
                            startPosition ? "-ss" : "",       // Start from the requested offset, if any
                            startPosition ? String.valueOf(request.startPositionSeconds()) : "",
                            request.video(),
                            request.subtitlesHidden() || isSubtitles ? "-nosub" : "",  // Turn off MKV-embedded subtitles and use external if needed
                            isSubtitles ? "-sub" : "",
                            isSubtitles ? request.subtitles() : "",
                            isSubtitles && !SubEncodingDefault ? "-subcp" : "",       // Use subtitles? Then check if encoding should be set
                            isSubtitles && !SubEncodingDefault ? request.subtitlesEncoding() : ""
                    )
                    // mplayer's argument parsing gets confused by empty entries (e.g. it starts silently ignoring -ss after them)
                    .filter(arg -> !arg.isEmpty())
                    .toList();

            player = new ProcessBuilder(args).start();
            paused = false;
            stopped = false;
            disposed = false;
            eofSeen = false;
            fileLength = -1;
            lastKnownPos = -1;
            pendingReplies.clear();

            var readFrom = new PipedInputStream(256 * 1024);
            playerOutErrWriter = new PipedOutputStream(readFrom);

            var playerOut = new BufferedReader(new InputStreamReader(readFrom));

            new LineRedirector(player.getInputStream(), playerOutErrWriter).start();
            new LineRedirector(player.getErrorStream(), playerOutErrWriter).start();

            playerIn = new PrintStream(player.getOutputStream());

            // Every output line passes through this single reader, so the end-of-file line (natural end of the file)
            // is never missed by a competing reader, and ANS_* replies land in the queue instead.
            outputReader = new Thread(() -> {
                try {
                    String line;
                    while ((line = playerOut.readLine()) != null) {
                        if (isEndOfFileLine(line))
                            eofSeen = true;
                        else if (line.startsWith("ANS_"))
                            pendingReplies.add(line);
                    }
                }
                catch (IOException e) {
                    log.warn("Could not read the mplayer output", e);
                }
            }, "mplayer-output-reader");
            outputReader.setDaemon(true);
            outputReader.start();

            // mplayer prints the same "end of file" line both when the file  played to its end and when the user
            // closes mplayer's own window (on this build), so the line alone cannot decide  whether to advance.
            // The position prober tracks the last known position and the file length while the process runs, so the
            // end watcher can require playback to actually be at the end.
            new Thread(() -> {
                while (player != null && player.isAlive()) {
                    if (fileLength <= 0)
                        // Learn the file length once (needed to tell a natural
                        // end from a window close). Newer builds answer
                        // get_time_length, classic 1.0rc5 answers get_length.
                        probeFileLength();
                    else
                        probePosition();
                    try {
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }, "mplayer-position-prober").start();

            /* If the user selected a subtitle file in the SUB pane but wants it hidden, the file is
             * still passed to mplayer at startup (see -sub above) and turned off right after.
             * It must be loaded at startup, otherwise it could not be enabled later.
             */
            if (request.subtitlesHidden())
                playerSingleCommand("sub_visibility 0");

            // mplayer exits on its own when the file is over, and it exits the exact same way (same output line, same
            // exit code) when the user closes mplayer's own window. The end-of-file line (see isEndOfFileLine)
            // therefore only narrows the exit down to "natural end or closed window": wait for the process,
            // drain the output reader, and fire the listener only if that line was seen AND the last known position
            // is at the end of the file (see reachedEndOfFile), and the exit was neither a deliberate dispose()
            // nor a user Stop.
            getWatcher().start();
        }
        catch (IOException e) {
            ServiceWindow.getErrorNotification(resourceBundle.getString("Error"),
                    resourceBundle.getString("ErrorUnableToStartMplayer"));
        }
    }

    private Thread getWatcher() {
        var process = player;
        var outWriter = playerOutErrWriter;
        var reader = outputReader;
        var watcher = new Thread(() -> {
            try {
                process.waitFor();
            }
            catch (InterruptedException _) {
                Thread.currentThread().interrupt();
                return;
            }
            // No more output is coming: let the reader finish draining
            try {
                outWriter.close();
            }
            catch (IOException _) {
            }
            try {
                reader.join(1_000);
            }
            catch (InterruptedException _) {
                Thread.currentThread().interrupt();
            }
            if (!disposed && !stopped && eofSeen && reachedEndOfFile()) {
                var listener = onFinishedListener;
                if (listener != null)
                    listener.run();
            }
        }, "mplayer-end-watcher");
        watcher.setDaemon(true);
        return watcher;
    }

    @Override
    public void stop() {
        playerSingleCommand("stop");
        paused = false;
        stopped = true;
    }

    @Override
    public void setOnFinishedListener(Runnable onFinished) {
        this.onFinishedListener = onFinished;
    }

    @Override
    public double getTimePos() {
        if (!playerSingleCommand("get_time_pos"))
            return -1;
        try {
            // mplayer builds differ in the reply prefix: newer ones
            // (SVN) reply ANS_TIME_POSITION, classic 1.0rc5 replies ANS_TIMEPOS.
            var returnedStr = waitForReply("ANS_TIME_POSITION=", "ANS_TIMEPOS=");
            if (returnedStr == null)
                return -1;
            return Double.parseDouble(returnedStr.substring(returnedStr.indexOf('=') + 1));
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Can't determine the current playback position", e);
            return -1;
        }
        catch (NumberFormatException e) {
            log.error("Can't parse the current playback position", e);
            return -1;
        }
    }

    @Override
    public void volumeUp() {
        playerSingleCommand("volume +1 0");
    }

    @Override
    public void volumeDown() {
        playerSingleCommand("volume -1 0");
    }

    @Override
    public void dispose() {
        if (player == null)
            return;
        // The exit that follows is on purpose, not a finished file
        disposed = true;
        try {
            if (player.isAlive()) {
                playerSingleCommand("quit");
                if (!player.waitFor(1, TimeUnit.SECONDS))
                    player.destroyForcibly();
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            player.destroyForcibly();
            log.error("Interrupted while disposing mplayer process", e);
        }
        finally {
            player = null;
            playerIn = null;
            paused = false;
            stopped = false;
            if (playerOutErrWriter != null) {
                try {
                    playerOutErrWriter.close();
                }
                catch (IOException e) {
                    log.warn("Could not close the mplayer output pipe", e);
                }
                playerOutErrWriter = null;
            }
            // Let the output reader finish draining, so it cannot leave
            // stale lines or an END_FILE flag behind for the next process
            if (outputReader != null) {
                try {
                    outputReader.join(500);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    // mplayer has no stable, locale-independent marker for "the file ended  naturally": some builds emit a literal
    // END_FILE line, English-locale builds print "Exiting... (End of file)" or "Выход… (Конец файла)".
    // Match all known variants. (Both a user `q` and closing the mplayer window exit without it.)
    private static boolean isEndOfFileLine(String line) {
        return line.startsWith("END_FILE")
                || line.contains("End of file")
                || line.contains("Конец файла");
    }

    // Ask for the file length (ANS_LENGTH) once and remember it. Returns true when a positive length was learned.
    private boolean probeFileLength() {
        for (var command : new String[]{"get_time_length", "get_length"}) {
            if (!playerSingleCommand(command))
                return false;
            try {
                var reply = waitForReply("ANS_LENGTH=");
                if (reply != null) {
                    var value = Double.parseDouble(reply.substring(reply.indexOf('=') + 1));
                    fileLength = value;
                    return value > 0;
                }
            }
            catch (NumberFormatException e) {
                log.warn("Can't parse the mplayer length reply", e);
            }
            catch (InterruptedException _) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    // Ask for the current playback position (ANS_TIME_POSITION / ANS_TIMEPOS)
    // and remember it as the last known one.
    private boolean probePosition() {
        if (!playerSingleCommand("get_time_pos"))
            return false;
        try {
            var reply = waitForReply("ANS_TIME_POSITION=", "ANS_TIMEPOS=");
            if (reply != null) {
                lastKnownPos = Double.parseDouble(reply.substring(reply.indexOf('=') + 1));
                return true;
            }
        }
        catch (NumberFormatException e) {
            log.warn("Can't parse the mplayer position reply", e);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        return false;
    }

    // The end-of-file line alone can't prove the file was played to its end: on this build closing mplayer's own
    // window prints the same line and exits the same way. At a natural end the last reported position sits at the end
    // of the file, while a window close leaves it mid-file. If either value is unknown, fall back to trusting the line.
    private boolean reachedEndOfFile() {
        if (fileLength <= 0 || lastKnownPos < 0)
            return true;
        return lastKnownPos >= fileLength - 1.0;
    }

    // Take the next reply to the command just issued, skipping unrelated replies that piled up in the queue
    // in the meantime.
    private String waitForReply(String... prefixes) throws InterruptedException {
        var deadline = System.currentTimeMillis() + 2_000;
        while (System.currentTimeMillis() < deadline) {
            String line;
            while ((line = pendingReplies.poll()) != null) {
                for (var prefix : prefixes)
                    if (line.startsWith(prefix))
                        return line;
            }
            Thread.sleep(25);
        }
        return null;
    }

    // The Play button toggles pause when a player is already running.  mplayer's `pause` command is a true toggle:
    // it pauses while playing and resumes while paused, so the app just mirrors the state it requested.
    private boolean togglePause() {
        if (player == null || !player.isAlive())
            return false;
        playerSingleCommand("pause");
        paused = !paused;
        return true;
    }
}
