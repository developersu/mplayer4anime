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

public class MpvProcess implements Runnable{
    private String videoFilename;
    private final LibC libC = LibC.INSTANCE;
    private final LibMpv libMpv = LibMpv.INSTANCE;

    /** MPV_FORMAT*/
    private final int MPV_FORMAT_NONE        = 0;
    private final int MPV_FORMAT_STRING      = 1;
    private final int MPV_FORMAT_OSD_STRING  = 2;
    private final int MPV_FORMAT_FLAG        = 3;
    private final int MPV_FORMAT_INT64       = 4;
    private final int MPV_FORMAT_DOUBLE      = 5;
    private final int MPV_FORMAT_NODE        = 6;
    private final int MPV_FORMAT_NODE_ARRAY  = 7;
    private final int MPV_FORMAT_NODE_MAP    = 8;
    private final int MPV_FORMAT_BYTE_ARRAY  = 9;

    /** mpv_event_id */
    private final int MPV_EVENT_NONE              = 0;
    private final int MPV_EVENT_SHUTDOWN          = 1;
    private final int MPV_EVENT_LOG_MESSAGE       = 2;
    private final int MPV_EVENT_GET_PROPERTY_REPLY = 3;
    private final int MPV_EVENT_SET_PROPERTY_REPLY = 4;
    private final int MPV_EVENT_COMMAND_REPLY     = 5;
    private final int MPV_EVENT_START_FILE        = 6;
    private final int MPV_EVENT_END_FILE          = 7;
    private final int MPV_EVENT_FILE_LOADED       = 8;
    private final int MPV_EVENT_TRACKS_CHANGED    = 9;//DEPRECATED
    private final int MPV_EVENT_TRACK_SWITCHED    = 10;//DEPRECATED
    private final int MPV_EVENT_IDLE              = 11;//DEPRECATED
    private final int MPV_EVENT_PAUSE             = 12;//DEPRECATED
    private final int MPV_EVENT_UNPAUSE           = 13;//DEPRECATED
    private final int MPV_EVENT_TICK              = 14;//DEPRECATED
    private final int MPV_EVENT_SCRIPT_INPUT_DISPATCH = 15;//DEPRECATED
    private final int MPV_EVENT_CLIENT_MESSAGE    = 16;
    private final int MPV_EVENT_VIDEO_RECONFIG    = 17;
    private final int MPV_EVENT_AUDIO_RECONFIG    = 18;
    private final int MPV_EVENT_METADATA_UPDATE   = 19;//DEPRECATED
    private final int MPV_EVENT_SEEK              = 20;
    private final int MPV_EVENT_PLAYBACK_RESTART  = 21;
    private final int MPV_EVENT_PROPERTY_CHANGE   = 22;
    private final int MPV_EVENT_CHAPTER_CHANGE    = 23;//DEPRECATED
    private final int MPV_EVENT_QUEUE_OVERFLOW    = 24;
    private final int MPV_EVENT_HOOK              = 25;

    MpvProcess(String filename){
        this.videoFilename = filename;
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
        libMpv.mpv_set_option(ctx, "osc", MPV_FORMAT_FLAG, value);
        libMpv.mpv_initialize(ctx);
        libMpv.mpv_command(ctx, new String[]{"loadfile", videoFilename, null});
        while (true){
            mpv_event event = libMpv.mpv_wait_event(ctx, 10000);
            if (event.event_id == MPV_EVENT_SHUTDOWN)
                break;
            System.out.println(event.event_id);
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
