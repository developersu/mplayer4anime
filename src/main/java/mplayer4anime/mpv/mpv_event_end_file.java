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
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

// struct mpv_event_end_file { int reason; int error; ... };
// "reason" is an mpv_end_file_reason enum value (EOF=0, STOP=2, QUIT=3,
// ERROR=4, REDIRECT=5) in every mpv since v0.17; the "eof" string only
// exists in the JSON IPC protocol. Newer mpv (API 1.108+) appends
// playlist_entry_id, playlist_insert_id and playlist_insert_num_entries
// after these fields, which we do not read.
@FieldOrder({"reason","error"})
public class mpv_event_end_file extends Structure {
    public mpv_event_end_file(){}

    public mpv_event_end_file(Pointer ptr) {
        super(ptr);
    }

    public int reason;
    public int error;
}
