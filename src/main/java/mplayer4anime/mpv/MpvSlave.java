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
package mplayer4anime.mpv;

import mplayer4anime.IPlayer;
import mplayer4anime.PlayRequest;
import mplayer4anime.ui.ServiceWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;

public class MpvSlave implements IPlayer {
    private static final Logger log = LoggerFactory.getLogger(MpvSlave.class);

    private static final double VOLUME_STEP = 2.0;
    private static final long SESSION_READY_TIMEOUT = 5_000; // ms to wait for a new session
    private static final long SESSION_READY_POLL = 25;       // ms between readiness checks

    private final ResourceBundle resourceBundle;
    private MpvProcess process;
    private Runnable onFinishedListener;

    public MpvSlave(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    @Override
    public void subtitlesSwitch() {
        var mpvProcess = live();
        if (mpvProcess == null)
            return;
        var visible = mpvProcess.getBooleanProperty("sub-visibility");
        if (visible != null)
            mpvProcess.setBooleanProperty("sub-visibility", !visible);
    }

    @Override
    public void fullscreenSwitch() {
        var mpvProcess = live();
        if (mpvProcess == null)
            return;
        var full = mpvProcess.getBooleanProperty("fullscreen");
        if (full != null)
            mpvProcess.setBooleanProperty("fullscreen", !full);
    }

    @Override
    public void mute() {
        var mpvProcess = live();
        if (mpvProcess == null)
            return;
        var muted = mpvProcess.getBooleanProperty("mute");
        if (muted != null)
            mpvProcess.setBooleanProperty("mute", !muted);
    }

    @Override
    public void playPause(PlayRequest request) {
        var mpvProcess = live();
        if (mpvProcess != null) {
            // The file ended (or nothing loaded): the session idles, pressing Play starts it over.
            // Note: core-idle is NOT a reliable "no file" indicator — mpv
            // reports core-idle=true while a file is merely paused, which made the
            // Play button restart playback instead of resuming. time-pos, on the
            // other hand, is readable as long as a file is loaded (even paused) and
            // becomes unreadable once the session goes idle.
            if (mpvProcess.getNumberProperty("time-pos") == null) {
                forcePlay(request);
                return;
            }
            // A file is in progress → Play button toggles pause (mplayer backend behavior)
            togglePause();
            return;
        }
        forcePlay(request);
    }

    @Override
    public void forcePlay(PlayRequest request) {
        if (nonSupportedOs())
            return;

        // Reuse the live session when there is one: the session stays idle after a file ends, so reloading the file
        // into it keeps the same player window. A new session is created only when none is alive.
        var mpvProcess = live();
        if (mpvProcess == null) {
            mpvProcess = new MpvProcess();
            process = mpvProcess;
            mpvProcess.setEndFileListener(onFinishedListener);
            if (!awaitReady(mpvProcess)) {
                log.error("mpv session did not start in time");
                ServiceWindow.getErrorNotification(resourceBundle.getString("Error"),
                        resourceBundle.getString("ErrorUnableToStartMpv"));
                process = null;
                return;
            }
        }

        // External audio replaces the embedded one (mplayer's -audiofile semantics)
        mpvProcess.setStringProperty("audio-files", request.audio() != null ? request.audio() : "");
        // External subtitles replace the embedded ones (mplayer's -sub semantics)
        mpvProcess.setStringProperty("sub-files", request.subtitles() != null ? request.subtitles() : "");
        // "default" means auto-detection for mpv
        mpvProcess.setStringProperty("sub-codepage", request.subtitlesEncoding() != null
                && !request.subtitlesEncoding().equals("default")
                ? request.subtitlesEncoding() : "auto");
        mpvProcess.setBooleanProperty("sub-visibility", !request.subtitlesHidden());
        mpvProcess.setBooleanProperty("fullscreen", request.fullscreen());
        mpvProcess.setBooleanProperty("pause", false);
        // "start" (renamed from "start-time" in mpv 0.40) is a global option that persists across files, so it is set
        // explicitly before every load: the requested offset, or "none" to clear a previous resume point.
        // Older mpv (< 0.40) only knows "start-time".
        if (request.startPositionSeconds() > 0) {
            if (!mpvProcess.setStringProperty("start", String.valueOf(request.startPositionSeconds())))
                mpvProcess.setNumberProperty("start-time", request.startPositionSeconds());
        }
        else {
            if (!mpvProcess.setStringProperty("start", "none"))
                mpvProcess.setNumberProperty("start-time", 0.0);
        }

        if (!mpvProcess.loadFile(request.video())) {
            log.error("Unable to load file {} in mpv", request.video());
            ServiceWindow.getErrorNotification(resourceBundle.getString("Error"),
                    resourceBundle.getString("ErrorUnableToLoadFile"));
            dispose();
        }
    }

    @Override
    public void stop() {
        var mpvProcess = live();
        if (mpvProcess != null)
            mpvProcess.command("stop");
    }

    @Override
    public void setOnFinishedListener(Runnable onFinished) {
        this.onFinishedListener = onFinished;
        var mpvProcess = live();
        if (mpvProcess != null)
            mpvProcess.setEndFileListener(onFinished);
    }

    @Override
    public double getTimePos() {
        var mpvProcess = live();
        if (mpvProcess == null)
            return -1;
        // time-pos is readable as long as a file is loaded (even paused), and becomes unreadable once session goes idle.
        var timePos = mpvProcess.getNumberProperty("time-pos");
        return timePos == null ? -1 : timePos;
    }

    @Override
    public void volumeUp() {
        changeVolume(VOLUME_STEP);
    }

    @Override
    public void volumeDown() {
        changeVolume(-VOLUME_STEP);
    }

    @Override
    public void dispose() {
        if (process != null) {
            process.dispose();
            process = null;
        }
    }

    // The Play button toggles pause: read the current state, then invert it.
    private boolean togglePause() {
        var mpvProcess = live();
        if (mpvProcess == null)
            return false;
        var paused = mpvProcess.getBooleanProperty("pause");
        mpvProcess.setBooleanProperty("pause", paused == null || !paused);
        return true;
    }

    private void changeVolume(double delta) {
        var mpvProcess = live();
        if (mpvProcess == null)
            return;
        var volume = mpvProcess.getNumberProperty("volume");
        if (volume != null)
            mpvProcess.setNumberProperty("volume", volume + delta);
    }

    /**
     * @return the current session if it is alive, otherwise null.
     * <p>
     * The field is read once so every use inside a method refers to the
     * same session instance.
     */
    private MpvProcess live() {
        var mpvProcess = process;
        return mpvProcess != null && mpvProcess.isAlive() ? mpvProcess : null;
    }

    // Block until a freshly created session is ready to accept commands.
    private boolean awaitReady(MpvProcess mpvProcess) {
        var deadline = System.currentTimeMillis() + SESSION_READY_TIMEOUT;
        while (!mpvProcess.isAlive()) {
            if (System.currentTimeMillis() > deadline)
                return false;
            try {
                Thread.sleep(SESSION_READY_POLL);
            }
            catch (InterruptedException _) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return true;
    }

    private boolean nonSupportedOs() {
        if (System.getProperty("os.name").toLowerCase().contains("linux"))
            return false;
        ServiceWindow.getErrorNotification(resourceBundle.getString("Error"),
                resourceBundle.getString("ErrorMpvNonLinux"));
        return true;
    }
}
