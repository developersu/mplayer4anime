/*
    Copyright 2018-2021 Dmitry Isaenko
     
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

import com.sun.jna.ptr.IntByReference;
import mplayer4anime.ui.ServiceWindow;
import static mplayer4anime.mpv.Mpv_Events.*;
import static mplayer4anime.mpv.Mpv_Format.*;

public class MpvProcess implements Runnable{
    private final String videoFilename;
    private final String audioFilename;
    private final String subsFilename;

    private final LibC libC = LibC.INSTANCE;
    private final LibMpv libMpv = LibMpv.INSTANCE;

    MpvProcess(String videoFilename, String audioFilename, String subsFilename){
        this.videoFilename = videoFilename;
        this.audioFilename = audioFilename;
        this.subsFilename = subsFilename;
        libC.setlocale(LibC.LC_NUMERIC, "C");  //Somehow it's important
    }

    @Override
    public void run() {
        if (nonSupportedOs()) {
            return;
        }

        long ctx = libMpv.mpv_create();
        libMpv.mpv_set_option_string(ctx, "input-default-bindings", "yes");
        libMpv.mpv_set_option_string(ctx, "input-vo-keyboard", "yes");
        IntByReference value = new IntByReference(1);
        libMpv.mpv_set_option(ctx, "osc", MPV_FORMAT_FLAG.ordinal(), value);
        libMpv.mpv_initialize(ctx);

        libMpv.mpv_command(ctx, new String[]{"loadfile", videoFilename});
        //libMpv.mpv_command(ctx, new String[]{"audio-add", audioFilename});
        //libMpv.mpv_command(ctx, new String[]{"subs-add", subsFilename});
        while (true){
            mpv_event event = libMpv.mpv_wait_event(ctx, 10000);
            if (event.event_id == MPV_EVENT_SHUTDOWN.ordinal())
                break;
            System.out.println(Mpv_Events.values()[event.event_id].name());

            if (event.event_id == MPV_EVENT_START_FILE.ordinal())
                libMpv.mpv_command(ctx, new String[]{"audio-add", audioFilename});
        }
        libMpv.mpv_terminate_destroy(ctx);
    }

    private boolean nonSupportedOs(){
        if (! System.getProperty("os.name").toLowerCase().contains("linux")) {
            ServiceWindow.getErrorNotification("Error",
                    "Non-Linux OS are not supported. Yet. Please yse mplayer backend for now.");
            return true;
        }
        return false;
    }
}
