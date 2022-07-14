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

public enum Mpv_Format {
    MPV_FORMAT_NONE, //0;
    MPV_FORMAT_STRING, //1;
    MPV_FORMAT_OSD_STRING, //2;
    MPV_FORMAT_FLAG, //3;
    MPV_FORMAT_INT64, //4;
    MPV_FORMAT_DOUBLE, //5;
    MPV_FORMAT_NODE, //6;
    MPV_FORMAT_NODE_ARRAY, //7;
    MPV_FORMAT_NODE_MAP, //8;
    MPV_FORMAT_BYTE_ARRAY; //9;
}
