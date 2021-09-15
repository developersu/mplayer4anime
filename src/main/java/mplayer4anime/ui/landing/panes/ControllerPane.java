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
package mplayer4anime.ui.landing.panes;

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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class ControllerPane implements Initializable {
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
        appPreferences = AppPreferences.getINSTANCE();
    }

    public void setPaneType(String paneType){
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
                break;
        }
    }

    /** Get index of the selected in pane element */
    public int getElementSelectedIndex(){
        return this.paneListView.getSelectionModel().getSelectedIndex();
    }
    /** Select element name (full path) using index recieved */
    public String getElementSelected(){
        File item = paneListView.getSelectionModel().getSelectedItem();
        if (item != null) {
            return item.getAbsolutePath();
        }
        return null;
    }
    /** Select element in pane using index recieved */
    public void setElementSelectedByIndex(int index){
        this.paneListView.getSelectionModel().select(index);
    }
    /** Get number of elements loaded into the pane */
    public int getElementsCount(){
        return this.paneFileList.size();
    }
    /** Check if there are any elements loaded */
    public boolean isElementsListEmpty(){
        return paneFileList.isEmpty();
    }
    /** Get all elements
     * Used in Json playlist writer only */
    public String[] getElementsAll(){
        String[] elementsArray = new String[this.getElementsCount()];
        for (int i = 0; i < elementsArray.length; i++){
            elementsArray[i] = paneFileList.get(i).toString();
        }
        return  elementsArray;
    }

    private void SetCellFactory(ListView<File> listView) {
        listView.setCellFactory(cb -> new ListCell<File>() {
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

    /** Open file selector (Open folder button in UI) */
    @FXML
    void openDirChooser(){
        String[] filesExtensionTmp;
        switch (paneType) {
            case "Video":
                filesExtensionTmp = appPreferences.getVideoExtensionsList();
                break;
            case "Audio":
                filesExtensionTmp = appPreferences.getAudioExtensionsList();
                break;
            case "Subtitles":
                filesExtensionTmp = appPreferences.getSubsExtensionsList();
                break;
            default:
                filesExtensionTmp = new String[]{"*"};
                break;
        }

        List<String> lowerAndUpperExts = new ArrayList<>();
        for (String s: filesExtensionTmp) {
            lowerAndUpperExts.add(s);
            lowerAndUpperExts.add(s.toUpperCase());
        }
        String[] filesExtension = lowerAndUpperExts.toArray(new String[lowerAndUpperExts.size()]);

        File directoryReceived;      // Store files (folder) received from selector
        DirectoryChooser dirSelect;

        // Show directory selector
        dirSelect = new DirectoryChooser();
        dirSelect.setTitle(resourceBundle.getString("SelectDirectoryTitle"));
        if (folderToOpen == null)
            dirSelect.setInitialDirectory(new File(System.getProperty("user.home")));
        else
            dirSelect.setInitialDirectory(new File(folderToOpen));
        directoryReceived = dirSelect.showDialog(paneListView.getScene().getWindow());

        // GET LIST OF FILES from directory
        if (directoryReceived != null) {
            File[] files;                // Store files mkv/mka

            files = directoryReceived.listFiles((file, Name) -> {
                int lastIndexOfDot = Name.lastIndexOf('.');
                if (lastIndexOfDot > 0) {
                    String ext = Name.substring(lastIndexOfDot);
                    for (String key : filesExtension){          // TODO: add toLowerCase and validate whatever registry extension noted
                        if (ext.equals(key.substring(1)))
                            return true;
                    }
                }
                return false;
            });

            displayFiles(files);
        }
        else {
            System.out.println("No folder selected");
        }
    }
    /** Open file selector (Open files button in UI) */
    @FXML
    void openFilesChooser(){
        String[] filesExtension;
        switch (paneType) {
            case "Video":
                filesExtension = appPreferences.getVideoExtensionsList();
                break;
            case "Audio":
                filesExtension = appPreferences.getAudioExtensionsList();
                break;
            case "Subtitles":
                filesExtension = appPreferences.getSubsExtensionsList();
                break;
            default:
                filesExtension = new String[]{"*"};
                break;
        }

        List<String> lowerAndUpperExts = new ArrayList<>();
        for (String s: filesExtension) {
            lowerAndUpperExts.add(s);
            lowerAndUpperExts.add(s.toUpperCase());
        }

        List<File> filesReceivedList;

        FileChooser fc = new FileChooser();
        fc.setTitle(resourceBundle.getString("SelectFile"));
        if (folderToOpen == null)
            fc.setInitialDirectory(new File(System.getProperty("user.home")));
        else
            fc.setInitialDirectory(new File(folderToOpen));

        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(paneType,
                lowerAndUpperExts.toArray(new String[0])));

        filesReceivedList = fc.showOpenMultipleDialog(paneListView.getScene().getWindow());
        if (filesReceivedList == null) {
            System.out.println("No files selected");
            return;
        }

        File[] filesReceived = new File[filesReceivedList.size()];
        filesReceivedList.toArray(filesReceived);
        displayFiles(filesReceived);
    }
    /**
     * Set files using lists. Used if playlist loaded
     * */
    public void setFilesFromList(String[] fileLocations){
        cleanList();
        if (fileLocations != null && fileLocations.length != 0) {
            File[] files = new File[fileLocations.length];
            for (int i=0; i < fileLocations.length; i++)
                files[i] = new File(fileLocations[i]);
            displayFiles(files);
        }
    }

    private void displayFiles(File[] files){
        if (files != null && files.length > 0) {
            // spiced java magic
            Arrays.sort(files);
            // Remember the folder used for MKV and reuse it when user opens MKA/subs folder (as new default path instead of user.home)
            folderToOpen = files[0].getParent();
            //System.out.println(folderToOpen);

                paneFileList.addAll(files);
                paneListView.setItems(paneFileList);
                paneListView.getSelectionModel().select(0);

        } else {
            System.out.println("\tNo files in this folder");
        }
    }
    @FXML
    public void cleanList(){
        paneListView.getItems().clear(); // wipe elements from ListView
         }

    @FXML
    private void Up(){
        int index;
        index = paneListView.getSelectionModel().getSelectedIndex();
        if (index >0){
            paneFileList.add(index-1, paneListView.getSelectionModel().getSelectedItem());
            paneFileList.remove(index+1);
            paneListView.getSelectionModel().select(index-1);
        }
    }
    @FXML
    private void Down(){
        int index;
        index = paneListView.getSelectionModel().getSelectedIndex();
        if (index+1 < paneFileList.size() ){
            paneFileList.add(index+2, paneListView.getSelectionModel().getSelectedItem());
            paneFileList.remove(index);
            paneListView.getSelectionModel().select(index+1);
        }
    }
    @FXML
    private void Del(){ paneFileList.remove(paneListView.getSelectionModel().getSelectedItem()); }

    @FXML
    private void KeyPressed(KeyEvent event){
        if (event.getCode().toString().equals("DELETE"))
            Del();
    }
}