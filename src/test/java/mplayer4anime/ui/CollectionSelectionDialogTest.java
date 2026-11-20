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
package mplayer4anime.ui;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CollectionSelectionDialogTest {

    @Test
    void keepsFullPathWhenThereIsNoCommonPart() {
        var prefix = CollectionSelectionDialog.commonPathPrefix(List.of(
                "/home/user/Videos/Show A",
                "/mnt/data/Show B"));
        assertEquals("", prefix);
        assertEquals("/home/user/Videos/Show A",
                CollectionSelectionDialog.shortenPath("/home/user/Videos/Show A", prefix));
    }

    @Test
    void shortensToEllipsisWhenAllCollectionsAreInTheSameFolder() {
        var folder = "/mnt/nas/Series/Some Show";
        var prefix = CollectionSelectionDialog.commonPathPrefix(List.of(folder, folder));
        assertEquals(folder, prefix);
        assertEquals("...", CollectionSelectionDialog.shortenPath(folder, prefix));
    }

    @Test
    void keepsDifferingTailWhenOnePathIsAncestorOfAnother() {
        var a = "/mnt/nas/Series/Show";
        var b = "/mnt/nas/Series/Show/RUS sound";
        var prefix = CollectionSelectionDialog.commonPathPrefix(List.of(a, b));
        assertEquals(a, prefix);
        assertEquals("...", CollectionSelectionDialog.shortenPath(a, prefix));
        assertEquals(".../RUS sound", CollectionSelectionDialog.shortenPath(b, prefix));
    }

    @Test
    void doesNotAbbreviatePartialSegmentMatches() {
        // "Show" and "Show 2" are different segments, so only their common
        // parent may be abbreviated
        var a = "/home/user/Videos/Show";
        var b = "/home/user/Videos/Show 2";
        var prefix = CollectionSelectionDialog.commonPathPrefix(List.of(a, b));
        assertEquals("/home/user/Videos", prefix);
        assertEquals(".../Show", CollectionSelectionDialog.shortenPath(a, prefix));
        assertEquals(".../Show 2", CollectionSelectionDialog.shortenPath(b, prefix));
    }

    @Test
    void doesNotAbbreviateASinglePath() {
        assertEquals("", CollectionSelectionDialog.commonPathPrefix(
                List.of("/home/user/Videos/Show")));
    }
}
