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

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;

public interface LibMpv extends Library {
    LibMpv INSTANCE = Native.load("libmpv", LibMpv.class);

    long mpv_create();
    int mpv_set_option_string(long ctx, String name, String data);
    int mpv_set_option(long ctx, String name, int format, IntByReference data);
    int mpv_initialize(long ctx);
    int mpv_command(long ctx, String[] args);


    mpv_event mpv_wait_event(long ctx, double timeout);
    void mpv_terminate_destroy(long ctx);
}
