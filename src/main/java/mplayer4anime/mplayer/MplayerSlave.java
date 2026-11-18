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
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class MplayerSlave implements IPlayer {
    private static final Logger log = LoggerFactory.getLogger(MplayerSlave.class);

    private Process player;
    private PrintStream playerIn;
    private BufferedReader playerOutErr;
    // Sole owner of the shared pipe writer: the pumps never close it,
    // only dispose() does, so reading from playerOutErr is not broken
    // by a premature EOF while a command reply is awaited.
    private PipedOutputStream playerOutErrWriter;
    private boolean paused = false;

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
            String returnedStr;
            var returnedInt = 1;
            while (player.isAlive() && (returnedStr = playerOutErr.readLine()) != null) {
                if (returnedStr.startsWith("ANS_SUB_VISIBILITY=")) {
                    returnedInt = Integer.parseInt(returnedStr.substring("ANS_SUB_VISIBILITY=".length()));
                    break;
                }
            }
            playerSingleCommand("sub_visibility %d".formatted((returnedInt == 1) ? 0 : 1));
        }
        catch (IOException e) {
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
        if (togglePause())
            return;

        var isAudio = request.audio() != null;
        var isSubtitles = request.subtitles() != null;
        var SubEncodingDefault = request.subtitlesEncoding().equals("default");

        try {
            var args = Stream.of(
                AppPreferences.get().getPath(),   // It's a chance for Windows ;)
                "-slave",
                isAudio ? "-audiofile" : "",
                isAudio ? request.audio() : "",
                "-quiet",
                request.fullscreen() ? "-fs" : "",
                request.video(),
                request.subtitlesHidden() || isSubtitles ? "-nosub" : "",  // Turn off MKV-embedded subtitles and use external if needed
                isSubtitles ? "-sub" : "",
                isSubtitles ? request.subtitles() : "",
                isSubtitles && !SubEncodingDefault ? "-subcp" : "",       // Use subtitles? Then check if encoding should be set
                isSubtitles && !SubEncodingDefault ? request.subtitlesEncoding() : ""
            ).toList();

            player = new ProcessBuilder(args).start();
            paused = false;

            var readFrom = new PipedInputStream(256 * 1024);
            playerOutErrWriter = new PipedOutputStream(readFrom);

            playerOutErr = new BufferedReader(new InputStreamReader(readFrom));

            new LineRedirector(player.getInputStream(), playerOutErrWriter).start();
            new LineRedirector(player.getErrorStream(), playerOutErrWriter).start();

            playerIn = new PrintStream(player.getOutputStream());

            /* If the user selected a subtitle file in the SUB pane but wants it hidden, the file is
             * still passed to mplayer at startup (see -sub above) and turned off right after.
             * It must be loaded at startup, otherwise it could not be enabled later.
             */
            if (request.subtitlesHidden())
                playerSingleCommand("sub_visibility 0");
        }
        catch (IOException e) {
            ServiceWindow.getErrorNotification(resourceBundle.getString("Error"),
                    resourceBundle.getString("ErrorUnableToStartMplayer"));
        }
    }

    @Override
    public void stop() {
        playerSingleCommand("stop");
        paused = false;
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
            playerOutErr = null;
            paused = false;
            if (playerOutErrWriter != null) {
                try {
                    playerOutErrWriter.close();
                }
                catch (IOException e) {
                    log.warn("Could not close the mplayer output pipe", e);
                }
                playerOutErrWriter = null;
            }
        }
    }

    // The Play button toggles pause when a player is already running.
    // mplayer's `pause` command is a true toggle on this build: it
    // pauses while playing and resumes while paused, so the app just
    // mirrors the state it requested.
    private boolean togglePause() {
        if (player == null || !player.isAlive())
            return false;
        playerSingleCommand("pause");
        paused = !paused;
        return true;
    }
}
