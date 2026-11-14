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

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import mplayer4anime.AppPreferences;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class ControllerPane implements Initializable {
    private static String folderToOpen = System.getProperty("user.home");

    private ResourceBundle resourceBundle;
    private AppPreferences preferences;

    private final ObservableList<File> paneFileList = FXCollections.observableArrayList();
    private PaneType paneType;

    @FXML
    private ListView<File> paneListView;
    @FXML
    private Label paneLbl;
    @FXML
    private Button paneBtnOpenDir;
    @FXML
    private Button paneBtnOpenFiles;
    @FXML
    private Button paneBtnClear;
    @FXML
    private Button paneBtnUp;
    @FXML
    private Button paneBtnDown;
    @FXML
    private Button paneBtnDelete;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;

        paneListView.setCellFactory(_ -> new ControllerListCell());
        paneListView.setItems(paneFileList);
        preferences = AppPreferences.get();

        paneBtnOpenDir.setOnAction(_ -> selectDir());
        paneBtnOpenFiles.setOnAction(_ -> selectFile());
        paneBtnClear.setOnAction(_ -> clear());
        paneBtnUp.setOnAction(_ -> up());
        paneBtnDown.setOnAction(_ -> down());
        paneBtnDelete.setOnAction(_ -> delete());
        paneListView.setOnKeyPressed(this::keyPressed);
    }

    public ObservableList<File> getPaneFileList() {
        return paneFileList;
    }

    public void setPaneType(PaneType type) {
        paneType = type;
        paneLbl.setText(str(paneType.getResourceKey()));
    }

    public String getSelectedAbsolutePath() {
        var item = paneListView.getSelectionModel().getSelectedItem();
        return item == null ?
                null :
                item.getAbsolutePath();
    }

    public int getSelected() {
        return paneListView.getSelectionModel().getSelectedIndex();
    }

    public void setSelected(int index) {
        paneListView.getSelectionModel().select(index);
    }

    public int getElementsSize() {
        return paneFileList.size();
    }

    public String[] getElements() {
        return paneFileList.stream()
                .map(File::getAbsolutePath)
                .toArray(String[]::new);
    }

    public ReadOnlyIntegerProperty getSelectedIndexProperty() {
        return paneListView.getSelectionModel().selectedIndexProperty();
    }

    // Set files using lists. Used once playlist applies
    public void setFiles(String[] fileLocations) {
        clear();

        if (fileLocations == null || fileLocations.length == 0)
            return;

        show(Arrays.stream(fileLocations)
                .map(File::new)
                .toArray(File[]::new));
    }

    private void selectDir() {
        var filesExtensions = preferences.getExtensionsForPane(paneType);

        var dirChooser = new DirectoryChooser();
        dirChooser.setTitle(str("SelectDirectoryTitle"));
        dirChooser.setInitialDirectory(new File(folderToOpen));

        var directoryReceived = dirChooser.showDialog(paneListView.getScene().getWindow());
        if (directoryReceived == null)
            return;

        var files = directoryReceived.listFiles((_, name) ->
                Arrays.stream(filesExtensions).anyMatch(ext -> name.endsWith(ext.substring(1))));

        show(files);
    }

    private void selectFile() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle(str("SelectFile"));
        fileChooser.setInitialDirectory(new File(folderToOpen));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(paneType.getDisplayName(), preferences.getExtensionsForPane(paneType)));

        var files = fileChooser.showOpenMultipleDialog(paneListView.getScene().getWindow());
        if (files == null)
            return;

        show(files.toArray(File[]::new));
    }

    private void show(File[] files) {
        if (files == null || files.length == 0)
            return;

        Arrays.sort(files);
        // The folder used for MKV is reused in any other pane and vise versa
        folderToOpen = files[0].getParent();

        paneFileList.addAll(files);
        paneListView.getSelectionModel().select(0);
    }

    private void clear() {
        paneFileList.clear();
    }

    private void up() {
        var index = paneListView.getSelectionModel().getSelectedIndex();
        if (index > 0)
            move(index, index - 1);
    }

    private void down() {
        var index = paneListView.getSelectionModel().getSelectedIndex();
        if (index + 1 < paneFileList.size())
            move(index, index + 1);
    }

    private void move(int from, int to) {
        paneFileList.add(to, paneFileList.remove(from));
        paneListView.getSelectionModel().select(to);
    }

    private void delete() {
        paneFileList.remove(paneListView.getSelectionModel().getSelectedItem());
    }

    private void keyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE)
            delete();
    }

    private String str(String key) {
        return resourceBundle.getString(key);
    }
}