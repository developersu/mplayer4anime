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
package mplayer4anime.ui.landing.panes;

public enum PaneType {
    VIDEO("lbl_VideoPane", "Video"),
    AUDIO("lbl_AudioPane", "Audio"),
    SUBTITLES("lbl_SubsPane", "Subtitles");

    private final String resourcesKey;
    private final String displayName;

    PaneType(String resourcesKey, String displayName) {
        this.resourcesKey = resourcesKey;
        this.displayName = displayName;
    }

    // Resolves this pane's title label from the given resource bundle.
    public String getResourceKey() {
        return resourcesKey;
    }

    public String getDisplayName() {
        return displayName;
    }
}
