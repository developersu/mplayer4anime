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

public enum Mpv_Events {
    MPV_EVENT_NONE,//0 
    MPV_EVENT_SHUTDOWN,//1 
    MPV_EVENT_LOG_MESSAGE,//2 
    MPV_EVENT_GET_PROPERTY_REPLY,//3 
    MPV_EVENT_SET_PROPERTY_REPLY,//4 
    MPV_EVENT_COMMAND_REPLY,//5 
    MPV_EVENT_START_FILE,//6 
    MPV_EVENT_END_FILE,//7 
    MPV_EVENT_FILE_LOADED,//8 
    MPV_EVENT_TRACKS_CHANGED,//9 DEPRECATED
    MPV_EVENT_TRACK_SWITCHED,//10 DEPRECATED
    MPV_EVENT_IDLE,//11 DEPRECATED
    MPV_EVENT_PAUSE,//12 DEPRECATED
    MPV_EVENT_UNPAUSE,//13 DEPRECATED
    MPV_EVENT_TICK,//14 DEPRECATED
    MPV_EVENT_SCRIPT_INPUT_DISPATCH,//15 DEPRECATED
    MPV_EVENT_CLIENT_MESSAGE,//16 
    MPV_EVENT_VIDEO_RECONFIG,//17 
    MPV_EVENT_AUDIO_RECONFIG,//18 
    MPV_EVENT_METADATA_UPDATE,//19 DEPRECATED
    MPV_EVENT_SEEK,//20 
    MPV_EVENT_PLAYBACK_RESTART,//21 
    MPV_EVENT_PROPERTY_CHANGE,//22 
    MPV_EVENT_CHAPTER_CHANGE,//23 DEPRECATED
    MPV_EVENT_QUEUE_OVERFLOW,//24 
    MPV_EVENT_HOOK,//25 ;
}
