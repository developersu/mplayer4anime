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
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import mplayer4anime.AppPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

public class ControllerPane implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(ControllerPane.class);

    private ResourceBundle resourceBundle;
    private static String folderToOpen;
    private AppPreferences appPreferences;

    @FXML
    private ListView<File> paneListView;
    private final ObservableList<File> paneFileList = FXCollections.observableArrayList();

    @FXML
    private Label paneLbl;
    private String paneType;

    @Override
    public void initialize(URL url, ResourceBundle resBundle) {
        SetCellFactory(paneListView);
        resourceBundle = resBundle;
        appPreferences = AppPreferences.get();
    }

    public ObservableList<File> getPaneFileList() {
        return paneFileList;
    }

    public void setPaneType(String paneType) {
        this.paneType = paneType;

        switch (paneType) {
            case "Video":
                paneLbl.setText(resourceBundle.getString("lbl_VideoPane"));
                break;
            case "Audio":
                paneLbl.setText(resourceBundle.getString("lbl_AudioPane"));
                break;
            case "Subtitles":
                paneLbl.setText(resourceBundle.getString("lbl_SubsPane"));
                break;
            default:
                paneLbl.setText(resourceBundle.getString("?"));
        }
    }

    /**
     * Get index of the selected in pane element
     */
    public int getElementSelectedIndex() {
        return this.paneListView.getSelectionModel().getSelectedIndex();
    }

    /**
     * Select element name (full path) using index received
     */
    public String getElementSelected() {
        var item = paneListView.getSelectionModel().getSelectedItem();
        return item == null ?
                null :
                item.getAbsolutePath();
    }

    /**
     * Select element in pane using index recieved
     */
    public void setElementSelectedByIndex(int index) {
        paneListView.getSelectionModel().select(index);
    }

    /**
     * Get number of elements loaded into the pane
     */
    public int getElementsCount() {
        return paneFileList.size();
    }

    /**
     * Get all elements
     * Used in Json playlist writer only
     */
    public String[] getElementsAll() {
        var elementsArray = new String[this.getElementsCount()];

        for (int i = 0; i < elementsArray.length; i++)
            elementsArray[i] = paneFileList.get(i).toString();

        return elementsArray;
    }

    private void SetCellFactory(ListView<File> listView) {
        listView.setCellFactory(_ -> new ListCell<>() {
            @Override
            public void updateItem(File item, boolean empty) {
                // have to call super here
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    return;
                }
                setText(item.getName());
            }
        });
    }

    /**
     * Open file selector (Open folder button in UI)
     */
    @FXML
    void openDirChooser() {
        var filesExtensionTmp = switch (paneType) {
            case "Video" -> appPreferences.getVideoExtensionsList();
            case "Audio" -> appPreferences.getAudioExtensionsList();
            case "Subtitles" -> appPreferences.getSubsExtensionsList();
            default -> new String[]{"*"};
        };

        var lowerAndUpperExts = new ArrayList<String>();
        for (var s : filesExtensionTmp) {
            lowerAndUpperExts.add(s);
            lowerAndUpperExts.add(s.toUpperCase());
        }
        var filesExtension = lowerAndUpperExts.toArray(new String[0]);

        // Show directory selector
        var dirSelect = new DirectoryChooser();
        dirSelect.setTitle(resourceBundle.getString("SelectDirectoryTitle"));
        if (folderToOpen == null)
            dirSelect.setInitialDirectory(new File(System.getProperty("user.home")));
        else
            dirSelect.setInitialDirectory(new File(folderToOpen));
        var directoryReceived = dirSelect.showDialog(paneListView.getScene().getWindow());

        // GET LIST OF FILES from directory
        if (directoryReceived != null) {
            var files = directoryReceived.listFiles((_, Name) -> {     // Store files mkv/mka
                var lastIndexOfDot = Name.lastIndexOf('.');
                if (lastIndexOfDot > 0) {
                    var ext = Name.substring(lastIndexOfDot);
                    for (var key : filesExtension) {          // TODO: add toLowerCase and validate whatever registry extension noted
                        if (ext.equals(key.substring(1)))
                            return true;
                    }
                }
                return false;
            });

            displayFiles(files);
        }
        else {
            log.debug("No folder selected");
        }
    }

    /**
     * Open file selector (Open files button in UI)
     */
    @FXML
    void openFilesChooser() {
        var filesExtension = switch (paneType) {
            case "Video" -> appPreferences.getVideoExtensionsList();
            case "Audio" -> appPreferences.getAudioExtensionsList();
            case "Subtitles" -> appPreferences.getSubsExtensionsList();
            default -> new String[]{"*"};
        };

        var lowerAndUpperExts = new ArrayList<String>();
        for (var s : filesExtension) {
            lowerAndUpperExts.add(s);
            lowerAndUpperExts.add(s.toUpperCase());
        }

        var fileChooser = new FileChooser();
        fileChooser.setTitle(resourceBundle.getString("SelectFile"));
        if (folderToOpen == null)
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        else
            fileChooser.setInitialDirectory(new File(folderToOpen));

        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(paneType,
                lowerAndUpperExts.toArray(new String[0])));

        var filesReceivedList = fileChooser.showOpenMultipleDialog(paneListView.getScene().getWindow());
        if (filesReceivedList == null) {
            log.debug("No files selected");
            return;
        }

        var filesReceived = new File[filesReceivedList.size()];
        filesReceivedList.toArray(filesReceived);
        displayFiles(filesReceived);
    }

    /**
     * Set files using lists. Used if playlist loaded
     *
     */
    public void setFilesFromList(String[] fileLocations) {
        cleanList();

        if (fileLocations == null || fileLocations.length == 0)
            return;

        var files = new File[fileLocations.length];
        for (var i = 0; i < fileLocations.length; i++)
            files[i] = new File(fileLocations[i]);
        displayFiles(files);
    }

    private void displayFiles(File[] files) {
        if (files == null || files.length == 0)
            return;

        Arrays.sort(files);
        // Remember the folder used for MKV and reuse it when user opens MKA/subs folder (as new default path instead of user.home)
        folderToOpen = files[0].getParent();

        paneFileList.addAll(files);
        paneListView.setItems(paneFileList);
        paneListView.getSelectionModel().select(0);
    }

    @FXML
    public void cleanList() {
        paneListView.getItems().clear();
    }

    @FXML
    private void up() {
        var index = paneListView.getSelectionModel().getSelectedIndex();
        if (index > 0) {
            paneFileList.add(index - 1, paneListView.getSelectionModel().getSelectedItem());
            paneFileList.remove(index + 1);
            paneListView.getSelectionModel().select(index - 1);
        }
    }

    @FXML
    private void down() {
        var index = paneListView.getSelectionModel().getSelectedIndex();
        if (index + 1 < paneFileList.size()) {
            paneFileList.add(index + 2, paneListView.getSelectionModel().getSelectedItem());
            paneFileList.remove(index);
            paneListView.getSelectionModel().select(index + 1);
        }
    }

    @FXML
    private void delete() {
        paneFileList.remove(paneListView.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void keyPressed(KeyEvent event) {
        if (event.getCode().toString().equals("DELETE"))
            delete();
    }

    public ReadOnlyIntegerProperty getSelectedIndexProperty() {
        return paneListView.getSelectionModel().selectedIndexProperty();
    }
}