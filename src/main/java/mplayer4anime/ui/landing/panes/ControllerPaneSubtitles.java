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
package mplayer4anime.ui.landing.panes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import mplayer4anime.AppPreferences;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class ControllerPaneSubtitles extends ControllerPane {
    private final ObservableList<String> encodings = FXCollections.observableArrayList();

    @FXML
    private ChoiceBox<String> encodingChoiceBox;

    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
    }

    public String getEncoding() {
        return encodingChoiceBox.getValue();
    }

    public void setEncodings(String[] newEncodingsList, String selection) {
        var currentlySelectedValue = encodingChoiceBox.getValue();

        encodings.clear();
        encodings.setAll(newEncodingsList);
        encodingChoiceBox.setItems(encodings);

        if (encodings.isEmpty())
            return;

        // Use the explicitly requested selection if it exists in the new newEncodingsList.
        // Otherwise, try to restore the previously selected value if it still exists.
        var value = selection;
        if (value == null || value.isEmpty())
            value = currentlySelectedValue;
        if (value == null || !encodings.contains(value))
            value = encodings.getFirst();
        encodingChoiceBox.setValue(value);
    }

    // TODO: consider revising
    // Select encoding and adds it into the drop-down if it's not in the list
    // Updates stored lists/selection of encoding

    public void selectEncodingValue(String encodingValue, AppPreferences preferences) {
        if (encodingValue == null)
            return;
        encodingValue = encodingValue.trim();

        // Ignore values that contain whitespace (spaces or tabs) anywhere
        if (encodingValue.isEmpty() || encodingValue.chars().anyMatch(Character::isWhitespace))
            return;

        if (!encodings.contains(encodingValue)) {
            encodings.add(encodingValue);
            preferences.setSubsEncodingList(encodings.toArray(new String[0]));
        }
        if (!Objects.equals(encodingChoiceBox.getValue(), encodingValue)) {
            encodingChoiceBox.setValue(encodingValue);
            preferences.setLastTimeUsedSubsEncoding(encodingValue);
        }
    }
}
