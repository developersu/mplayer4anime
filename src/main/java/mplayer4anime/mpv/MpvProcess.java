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

import com.sun.jna.Pointer;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import static mplayer4anime.mpv.Mpv_Events.MPV_EVENT_SHUTDOWN;
import static mplayer4anime.mpv.Mpv_Format.MPV_FORMAT_DOUBLE;
import static mplayer4anime.mpv.Mpv_Format.MPV_FORMAT_FLAG;

/**
 * The session stays alive after a file ends (mpv goes idle), so the
 * application can drive track switching with {@link #loadFile(String)}
 * without recreating the player window.
 */
public class MpvProcess {
    private static final Logger log = LoggerFactory.getLogger(MpvProcess.class);

    private static final double EVENT_LOOP_TIMEOUT = 1.0;   // seconds between exit-checks
    private static final long DISPOSE_JOIN_TIMEOUT = 3_000; // ms to wait for the loop thread

    private final LibC libC = LibC.INSTANCE;
    private final LibMpv libMpv = LibMpv.INSTANCE;

    private final Thread thread;

    // Published only after the context is fully initialized, so any thread
    // that observes a non-zero ctx is guaranteed to find it usable.
    private volatile long ctx;
    private volatile boolean destroyed;

    public MpvProcess() {
        thread = new Thread(this::run, "mpv-event-loop");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * @return true once the session is created and running, false after dispose.
     */
    public boolean isAlive() {
        return ctx != 0 && !destroyed;
    }

    private void run() {
        libC.setlocale(LibC.LC_NUMERIC, "C");

        var context = libMpv.mpv_create();
        if (context == 0) {
            log.error("mpv_create failed");
            return;
        }
        libMpv.mpv_set_option_string(context, "config", "no");
        libMpv.mpv_set_option_string(context, "input-default-bindings", "yes");
        libMpv.mpv_set_option_string(context, "input-vo-keyboard", "yes");
        libMpv.mpv_set_option(context, "osc", MPV_FORMAT_FLAG.ordinal(), new IntByReference(1));
        // Keep the player (and its window) alive after a file ends so the app
        // can load the next track into the same session.
        libMpv.mpv_set_option_string(context, "idle", "yes");

        if (libMpv.mpv_initialize(context) < 0) {
            log.error("mpv failed to initialize");
            libMpv.mpv_terminate_destroy(context);
            return;
        }
        // Publish the context only once it is fully usable.
        ctx = context;

        while (!destroyed) {
            var event = libMpv.mpv_wait_event(context, EVENT_LOOP_TIMEOUT);
            if (event == null)
                continue;
            log.debug("MPV event: {}", Mpv_Events.values()[event.event_id].name());

            if (event.event_id == MPV_EVENT_SHUTDOWN.ordinal())
                break;
        }
        libMpv.mpv_terminate_destroy(context);
        ctx = 0;
    }

    // --- Commands (thread-safe)

    /**
     * @return true if command accepted
     */
    public boolean command(String... args) {
        if (!isAlive())
            return false;
        var ret = libMpv.mpv_command(ctx, args);
        libMpv.mpv_wakeup(ctx);
        return ret >= 0;
    }

    /**
     * Start/restart playback of the given file in current session
     */
    public boolean loadFile(String path) {
        return command("loadfile", path, "replace");
    }

    /**
     * Set a string property (e.g. "audio-files", "sub-files", "sub-codepage").
     */
    public boolean setStringProperty(String name, String value) {
        if (!isAlive())
            return false;
        var ret = libMpv.mpv_set_property_string(ctx, name, value);
        libMpv.mpv_wakeup(ctx);
        return ret >= 0;
    }

    /**
     * Set a flag property (e.g. "fullscreen", "mute", "pause", "sub-visibility").
     */
    public boolean setBooleanProperty(String name, boolean value) {
        if (!isAlive())
            return false;
        var data = new IntByReference(value ? 1 : 0);
        var ret = libMpv.mpv_set_property(ctx, name, MPV_FORMAT_FLAG.ordinal(), data);
        libMpv.mpv_wakeup(ctx);
        return ret >= 0;
    }

    /**
     * Set a numeric property (e.g. "volume").
     */
    public boolean setNumberProperty(String name, double value) {
        if (!isAlive())
            return false;
        var data = new DoubleByReference(value);
        var ret = libMpv.mpv_set_property(ctx, name, MPV_FORMAT_DOUBLE.ordinal(), data);
        libMpv.mpv_wakeup(ctx);
        return ret >= 0;
    }

    /**
     * @return the current flag property value, or null if unreadable
     */
    public Boolean getBooleanProperty(String name) {
        if (!isAlive())
            return null;
        var data = new IntByReference(0);
        if (libMpv.mpv_get_property(ctx, name, MPV_FORMAT_FLAG.ordinal(), data) < 0)
            return null;
        return data.getValue() != 0;
    }

    /**
     * @return the current numeric property value, or null if unreadable
     */
    public Double getNumberProperty(String name) {
        if (!isAlive())
            return null;
        var data = new DoubleByReference(0);
        if (libMpv.mpv_get_property(ctx, name, MPV_FORMAT_DOUBLE.ordinal(), data) < 0)
            return null;
        return data.getValue();
    }

    /**
     * @return the current string property value, or null if unreadable
     */
    public String getStringProperty(String name) {
        if (!isAlive())
            return null;
        Pointer ptr = libMpv.mpv_get_property_string(ctx, name);
        if (ptr == null)
            return null;
        try {
            return ptr.getString(0, StandardCharsets.UTF_8.name());
        }
        finally {
            libMpv.mpv_free(ptr);
        }
    }

    /**
     * Shut the session down and wait for the event loop thread to finish.
     * Safe to call more than once.
     */
    public void dispose() {
        if (destroyed)
            return;
        destroyed = true;
        if (ctx != 0)
            libMpv.mpv_wakeup(ctx);
        try {
            thread.join(DISPOSE_JOIN_TIMEOUT);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for the mpv event loop to stop", e);
        }
    }
}
