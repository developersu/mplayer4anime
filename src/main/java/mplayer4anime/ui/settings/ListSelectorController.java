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
package mplayer4anime.ui.settings;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * FXML controller of a small reusable list editor: a list of strings with
 * move up/down and remove buttons, plus a validated input field for adding
 * new entries.
 */
public class ListSelectorController implements Initializable {

    @FXML
    private ListView<String> listView;
    @FXML
    private TextField entryField;
    @FXML
    private Button moveUpBtn;
    @FXML
    private Button moveDownBtn;
    @FXML
    private Button removeBtn;
    @FXML
    private Button addBtn;

    private InputMode inputMode;

    public enum InputMode {
        EXTENSION(Pattern.compile("^\\*\\.[A-Za-z0-9]*$"), "*."),
        ENCODING(Pattern.compile("^[A-Za-z0-9-]*$"), "");

        private final Pattern validator;
        private final String defaultText;

        InputMode(Pattern validator, String defaultText) {
            this.validator = validator;
            this.defaultText = defaultText;
        }

        TextFormatter<String> textFormatter() {
            return new TextFormatter<>(change ->
                    validator.matcher(change.getControlNewText()).matches() ?
                            change :
                            null);
        }

        String normalize(String entry) {
            return this == EXTENSION ?
                    entry.toLowerCase() :
                    entry;
        }

        String defaultText() {
            return defaultText;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        moveUpBtn.setOnAction(_ -> moveSelected(-1));
        moveDownBtn.setOnAction(_ -> moveSelected(1));
        removeBtn.setOnAction(_ -> removeSelected());
        addBtn.setOnAction(_ -> addEntry());
        listView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE)
                removeSelected();
        });
    }

    /**
     * Fills the list and applies the input rule. (Safe to call more than once.)
     */
    void configure(String[] listFromStorage, InputMode mode) {
        inputMode = mode;
        listView.setItems(FXCollections.observableArrayList(listFromStorage));
        entryField.setTextFormatter(mode.textFormatter());
        entryField.setText(mode.defaultText());
    }

    String[] getList() {
        return listView.getItems().toArray(String[]::new);
    }

    private void moveSelected(int delta) {
        var index = listView.getSelectionModel().getSelectedIndex();
        var target = index + delta;
        var items = listView.getItems();
        if (index >= 0 && target >= 0 && target < items.size()) {
            items.add(target, items.remove(index));
            listView.getSelectionModel().select(target);
        }
    }

    private void removeSelected() {
        var index = listView.getSelectionModel().getSelectedIndex();
        if (index < 0)
            return;
        var items = listView.getItems();
        items.remove(index);
        if (!items.isEmpty())                                // Keep selection on the neighbor row
            listView.getSelectionModel().select(Math.min(index, items.size() - 1));
    }

    private void addEntry() {
        var entry = inputMode.normalize(entryField.getText());
        if (!entry.isEmpty() && !entry.equals(inputMode.defaultText) && !listView.getItems().contains(entry))
            listView.getItems().add(entry);
        entryField.setText(inputMode.defaultText());
    }
}
