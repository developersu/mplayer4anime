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
package mplayer4anime;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import javafx.stage.Stage;
import mplayer4anime.About.AboutWindow;
import mplayer4anime.Playlists.JsonStorage;
import mplayer4anime.Playlists.Playlists;
import mplayer4anime.Settings.SettingsWindow;
import mplayer4anime.appPanes.ControllerPane;
import mplayer4anime.appPanes.ControllerSUB;
import mplayer4anime.mplayer.MplayerSlave;
import mplayer4anime.mpv.MpvSlave;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private ControllerPane mkvPaneController, mkaPaneController;
    @FXML
    private ControllerSUB subPaneController;
    @FXML
    private Label statusLbl;
    @FXML
    private Menu recentlyOpenedMenu;
    @FXML
    private CheckMenuItem fullScreen;
    @FXML
    private TabPane tabPane;
    @FXML
    private CheckMenuItem subsHide;

    private final AppPreferences appPreferences = AppPreferences.getINSTANCE();

    private ResourceBundle resourceBundle;
    private HostServices hostServices;
    private String currentPlaylistLocation;
    private String backend;
    private ISlaveModeAppOrchestration player;

    // If application started with playlist passed as an argument, then we'll try to load it (if it's valid).
    public void setPlaylistAsArgument(String playlist) {
        JsonStorage jsonStorage = Playlists.ReadByPath(resourceBundle, new File(playlist));
        setAllLists(jsonStorage);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mkvPaneController.setPaneType("Video");
        mkaPaneController.setPaneType("Audio");
        subPaneController.setPaneType("Subtitles");
        // Register this controller in mediator
        MediatorControl.getInstance().registerMainController(this);

        resourceBundle = rb;

        // Set default list of encodings of the subtitles files:
        subPaneController.setEncoding(appPreferences.getSubsEncodingList(), appPreferences.getLastTimeUsedSubsEncoding());

        // If subtitles should be opened first, per user's settings let's show it first
        if (appPreferences.getSubtilesFirst()){
            tabPane.getSelectionModel().select(1);  // 0 is mka 1 is subs
        }

        fullScreen.setSelected(appPreferences.getFullScreenSelected());
        subsHide.setSelected(appPreferences.getSubtitlesHideSelected());

        String[] recentPlaylists = appPreferences.getRecentPlaylists();
        for (int i = recentPlaylists.length-1; i >= 0; i--) {
            if (recentPlaylists[i].isEmpty())
                continue;
            addRecentlyOpened(recentPlaylists[i]);
        }
        if (appPreferences.getBackendEngineIndexId() == 0) {
            backend = "mplayer";
            player = new MplayerSlave(resourceBundle);
        }
        else {
            backend = "mpv";
            player = new MpvSlave(resourceBundle);
        }
    }

    void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    private void closeBtn() {
        Stage currentStage = (Stage) tabPane.getScene().getWindow();
        currentStage.close();
    }

    // Will be used to store lists previously opened.
    // Linkage established by ohHidden in MainFX.java class
    void shutdown(){
        appPreferences.setLastTimeUsedSubsEncoding(subPaneController.getSelectedEncoding());
        appPreferences.setFullScreenSelected(fullScreen.isSelected());
        appPreferences.setSubtitlesHideSelected(subsHide.isSelected());

        String[] storeRecentArr = new String[10];
        for (int i =0; i < recentlyOpenedMenu.getItems().size() - 2 && !(i > 9); i++) {       // Don't take separator and Clean button
            storeRecentArr[i] = (String) recentlyOpenedMenu.getItems().get(i).getUserData();
        }
        appPreferences.setRecentPlaylists(storeRecentArr);
        Platform.exit();
    }

    @FXML
    private void infoBtn(){
        new AboutWindow(this.hostServices);
    } // TODO: fix this shit with hostSerivces that doesn't work @ linux

    /**             SETTINGS HANDLE          */
    @FXML
    private void settingsBtn(){
        new SettingsWindow();
    }
    // Get event that notify application in case some settings has been changed
    // This function called from MediatorControl after mediator receives request form SettingsController indicating that user updated some required fields.
    void updateAfterSettingsChanged(){
        /* update list of encoding */
        subPaneController.setEncoding(appPreferences.getSubsEncodingList(), null);
        // In case of application failure should be better to save this immediately
        appPreferences.setLastTimeUsedSubsEncoding(subPaneController.getSelectedEncoding());
        switchBackend(appPreferences.getBackendEngineIndexId());
    }
    private void switchBackend(int newBackEndId){
        if (newBackEndId == 0 && backend.equals("mpv")){
            backend = "mplayer";
            player = new MplayerSlave(resourceBundle);
            return;
        }
        if (newBackEndId == 1 && backend.equals("mplayer")){
            backend = "mpv";
            player = new MpvSlave(resourceBundle);
        }
    }

    @FXML
    private void openBtn() {
        JsonStorage jsonStorage = Playlists.Read(resourceBundle);
        setAllLists(jsonStorage);
    }
    private void setAllLists(JsonStorage jsonStorage){
        if (jsonStorage != null) {
            mkvPaneController.setFilesFromList(jsonStorage.getVideo());
            mkaPaneController.setFilesFromList(jsonStorage.getAudio());
            subPaneController.setFilesFromList(jsonStorage.getSubs());
            subPaneController.selectEncodingValue(jsonStorage.getSubEncoding(), appPreferences);

            currentPlaylistLocation = Playlists.getPlaylistLocation();              // TODO: Implement listener? mmm...
            statusLbl.setText(currentPlaylistLocation);
            addRecentlyOpened(currentPlaylistLocation);
        }
    }
    @FXML
    private void saveBtn() {
        if (mkvPaneController.getElementsCount() == 0) {
            ServiceWindow.getErrorNotification(resourceBundle.getString("Error"),
                    resourceBundle.getString("ErrorUnableToSaveEmptyPlaylist"));
            return;
        }
        JsonStorage jsonStorage = new JsonStorage(
                mkvPaneController.getElementsAll(),
                mkaPaneController.getElementsAll(),
                subPaneController.getElementsAll(),
                subPaneController.getSelectedEncoding());
        if (Playlists.SaveCurrent(resourceBundle, jsonStorage)) {
            this.currentPlaylistLocation = Playlists.getPlaylistLocation();
            this.statusLbl.setText(currentPlaylistLocation);    //TODO: update header of the application to include this?
            addRecentlyOpened(currentPlaylistLocation);
        }
    }
    @FXML
    private void saveAsBtn() {
        if (mkvPaneController.getElementsCount() == 0) {
            ServiceWindow.getErrorNotification(resourceBundle.getString("Error"),
                    resourceBundle.getString("ErrorUnableToSaveEmptyPlaylist"));
            return;
        }

        JsonStorage jsonStorage = new JsonStorage(
                mkvPaneController.getElementsAll(),
                mkaPaneController.getElementsAll(),
                subPaneController.getElementsAll(),
                subPaneController.getSelectedEncoding());
        if (Playlists.SaveAs(resourceBundle, jsonStorage)) {
            this.currentPlaylistLocation = Playlists.getPlaylistLocation();
            this.statusLbl.setText(currentPlaylistLocation);    //TODO: update header of the application to include this?
            addRecentlyOpened(currentPlaylistLocation);
        }
    }
    @FXML
    private void cleanAllRecentlyOpened(){
        recentlyOpenedMenu.getItems().remove(0,recentlyOpenedMenu.getItems().size() - 2);
    }

    private void addRecentlyOpened(String playlistPath){
        ObservableList<MenuItem> items = recentlyOpenedMenu.getItems();
        for (MenuItem item : items) {
            if (item.getUserData() != null && item.getUserData().equals(playlistPath)) {
                items.remove(item);
                items.add(0, item);
                return;
            }
        }

        MenuItem menuItem = new MenuItem();
        String playListName;

        playListName = playlistPath.substring(
                playlistPath.lastIndexOf(File.separator) + 1, playlistPath.lastIndexOf("."));
        menuItem.setText(playListName);

        menuItem.setUserData(playlistPath);
        menuItem.setOnAction(actionEvent -> {
            JsonStorage jsonStorage = Playlists.ReadByPath(resourceBundle, new File(playlistPath));
            setAllLists(jsonStorage);
        });
        // Limit list to 13 elements (2 in the end are separator and clear button)
        if (items.size() >= 11)
            items.remove(9, recentlyOpenedMenu.getItems().size() - 2);
        items.add(0, menuItem);
    }

    /* PLAYER */
    @FXML
    private void subsTriggerBtn(){
        player.subtitlesSwitch();
    }
    @FXML
    private void fullscreenBtn(){
        player.fullscreenSwitch();
    }
    @FXML
    private void muteBtn(){
        player.mute();
    }
    @FXML
    private void playPrevTrackBtn(){
        int index = mkvPaneController.getElementSelectedIndex();
        if (index <= 0)
            return;

        mkvPaneController.setElementSelectedByIndex(index-1);
        player.forcePlay(appPreferences.getPath(),
                mkvPaneController.getElementSelected(),
                mkaPaneController.getElementSelected(),
                subPaneController.getElementSelected(),
                subPaneController.getSelectedEncoding(),
                subsHide.isSelected(),
                fullScreen.isSelected()
        );
    }
    @FXML
    private void playNextTrackBtn(){
        int index = mkvPaneController.getElementSelectedIndex();

        if (index + 1 < mkvPaneController.getElementsCount()) {
            mkvPaneController.setElementSelectedByIndex(index + 1);
        }
        index = mkaPaneController.getElementSelectedIndex();
        if (index + 1 < mkaPaneController.getElementsCount()) {
            mkaPaneController.setElementSelectedByIndex(index + 1);
        }
        index = subPaneController.getElementSelectedIndex();
        if (index + 1 < subPaneController.getElementsCount()) {
            subPaneController.setElementSelectedByIndex(index + 1);
        }

        player.forcePlay(appPreferences.getPath(),
                mkvPaneController.getElementSelected(),
                mkaPaneController.getElementSelected(),
                subPaneController.getElementSelected(),
                subPaneController.getSelectedEncoding(),
                subsHide.isSelected(),
                fullScreen.isSelected()
        );
    }
    @FXML
    private void playBtn(){
        if (mkvPaneController.getElementSelected() == null)
            return;

        player.playPause(appPreferences.getPath(),
                mkvPaneController.getElementSelected(),
                mkaPaneController.getElementSelected(),
                subPaneController.getElementSelected(),
                subPaneController.getSelectedEncoding(),
                subsHide.isSelected(),
                fullScreen.isSelected()
                );
    }
    @FXML
    private void stopBtn(){
        player.stop();
    }
    @FXML
    private void volumeUpBtn(){
        player.volumeUp();
    }
    @FXML
    private void volumeDownBtn(){
        player.volumeDown();
    }
}

