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
import java.util.Arrays;
import java.util.ResourceBundle;

public class ControllerPaneSubtitles extends ControllerPane {
    @FXML
    private ChoiceBox<String> subtEncoding;
    private ObservableList<String> subEncodingList;

    public void initialize(URL url, ResourceBundle rb) {
        super.initialize(url, rb);
        this.subEncodingList = FXCollections.observableArrayList();
    }
    /** Return selected file encoding */
    public String getSelectedEncoding(){
        return subtEncoding.getSelectionModel().getSelectedItem();
    }
    /**
     * Set encoding list and select element
     * */
    public void setEncoding(String[] list, String selection){
        var currentlySelectedValue = subtEncoding.getValue();

        subEncodingList.clear();
        subEncodingList.setAll(list);
        subtEncoding.setItems(subEncodingList);

        if (selection == null || selection.isEmpty())
            // Try to restore previously selected value if it existed before and exists in the new list
            if (currentlySelectedValue !=null && !currentlySelectedValue.isEmpty() && subEncodingList.contains(currentlySelectedValue))
                subtEncoding.getSelectionModel().select(currentlySelectedValue);
            else
                subtEncoding.setValue(subEncodingList.getFirst());
        else {
            if (subEncodingList.contains(selection))
                subtEncoding.getSelectionModel().select(selection);
            else
                subtEncoding.setValue(subEncodingList.getFirst());
        }
    }
    /**
     * Select encoding and adds it into the drop-down if it's not in the list
     * Updates stored lists/selection of encoding
     * */
    public void selectEncodingValue(String encodingValue, AppPreferences preferences){
        if (encodingValue != null && !(encodingValue = encodingValue.trim()).isEmpty()) {
            if (!encodingValue.contains("\t") && !encodingValue.contains(" ")){             // If contains spaces or tabs, ignore.
                if (!subEncodingList.contains(encodingValue)) {
                    subEncodingList.add(encodingValue);
                    preferences.setSubsEncodingList(Arrays.copyOf(subEncodingList.toArray(), subEncodingList.toArray().length, String[].class));
                }
                if (!subtEncoding.getValue().equals(encodingValue)) {
                    subtEncoding.getSelectionModel().select(encodingValue);
                    preferences.setLastTimeUsedSubsEncoding(encodingValue);
                }
            }
        }
    }
}