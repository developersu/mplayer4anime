/*
    Copyright 2026 Dmitry Isaenko

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
package mplayer4anime.media;

import java.io.File;
import java.util.Arrays;

/**
 * A set of external media files (audio tracks or subtitles) located
 * in one single directory, usually next to the video files.
 * <p>
 * When one directory contains files of several releases (for example,
 * audio tracks of different release groups), each release is reported
 * as a separate collection distinguished by its {@link #label () label}.
 */
public record MediaCollection(File directory, File[] files, String label) {
    public MediaCollection(File directory, File[] files) {
        this(directory, files, null);
    }

    public MediaCollection(File directory, File[] files, String label) {
        this.directory = directory;
        this.files = Arrays.copyOf(files, files.length);
        this.label = label;
    }

    /**
     * @return a short name that distinguishes this collection from other
     * collections of the same directory, or {@code null} when the directory
     * contains a single collection
     */
    @Override
    public String label() {
        return label;
    }
}
