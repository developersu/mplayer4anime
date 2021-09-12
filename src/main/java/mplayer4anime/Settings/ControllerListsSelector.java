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
package mplayer4anime.Settings;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyEvent;

import java.util.Arrays;

public class ControllerListsSelector{
    @FXML
    private ListView<String> listView;
    @FXML
    private TextField newRecordText;
    private ObservableList<String> observableList;

    private boolean isListOfExtensions;

    /**
     * Must be run on start
     * Set list content
    */
    void setList(String[] listFromStorage, boolean isListOfExtensions){
        this.isListOfExtensions = isListOfExtensions;
        observableList = FXCollections.observableArrayList(listFromStorage);
        listView.setItems(observableList);
        if (isListOfExtensions) {
            newRecordText.setText("*.");
            newRecordText.setTextFormatter(new TextFormatter<Object>(change -> {
                if (change.getControlNewText().matches("(^\\*\\.[A-Za-z0-9]$)|(^\\*\\.[A-Za-z0-9][A-Za-z0-9\\.]+?$)|(^\\*\\.$)"))
                    return change;
                return null;
            }));
        }
        else {
            newRecordText.setTextFormatter(new TextFormatter<Object>(change -> {
                if (change.getControlNewText().matches("(^[A-Za-z0-9\\-]+?$)|(^$)"))
                    return change;
                return null;
            }));
        }

    }
    /**
     * Return list content
     * */
    String[] getList(){
        return Arrays.copyOf(observableList.toArray(), observableList.toArray().length, String[].class);
    }
    @FXML
    private void listKeyPressed(KeyEvent event){
        if (event.getCode().toString().equals("DELETE"))
            removeRecord();
    }
    @FXML
    private void upRecord(){
        int index;
        index = listView.getSelectionModel().getSelectedIndex();
        if (index >0){
            observableList.add(index-1, listView.getSelectionModel().getSelectedItem());
            observableList.remove(index+1);
            listView.getSelectionModel().select(index-1);
        }
    }
    @FXML
    private void downRecord(){
        int index;
        index = listView.getSelectionModel().getSelectedIndex();
        if (index+1 < observableList.size() ){
            observableList.add(index+2, listView.getSelectionModel().getSelectedItem());
            observableList.remove(index);
            listView.getSelectionModel().select(index+1);
        }
    }
    @FXML
    private void removeRecord(){ observableList.remove(listView.getSelectionModel().getSelectedItem()); }
    @FXML
    private void addNewRecord(){
        String addingItem = newRecordText.getText().toLowerCase();
        if (!addingItem.isEmpty()) {                   // If this field is non-empty
            if (!observableList.contains(addingItem))
                observableList.add(addingItem);
        }
        if (isListOfExtensions)
            newRecordText.setText("*.");
        else
            newRecordText.clear();
    }
}