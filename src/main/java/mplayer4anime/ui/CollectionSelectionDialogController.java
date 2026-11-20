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

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import mplayer4anime.media.MediaCollection;

import java.util.List;

public class CollectionSelectionDialogController {
    @FXML
    private Label messageLbl;
    @FXML
    private ListView<String> listView;

    private CollectionSelectionDialog dialog;
    private List<MediaCollection> collections = List.of();

    public void setMessage(String message) {
        messageLbl.setText(message);
    }

    public void setDialog(CollectionSelectionDialog dialog) {
        this.dialog = dialog;
    }

    public void setCollections(List<MediaCollection> collections) {
        this.collections = collections;
        listView.setItems(FXCollections.observableArrayList(
                collections.stream().map(dialog::entryFor).toList()));
        if (!collections.isEmpty())
            listView.getSelectionModel().selectFirst();
    }

    public MediaCollection selectedCollection() {
        var index = listView.getSelectionModel().getSelectedIndex();
        return index >= 0 ? collections.get(index) : null;
    }
}
