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

import javafx.scene.control.ListCell;

import java.io.File;

public class ControllerListCell extends ListCell<File> {
    @Override
    public void updateItem(File item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            setText(null);
            return;
        }
        setText(item.getName());
    }
}
