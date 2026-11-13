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
package mplayer4anime.ui.landing;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import mplayer4anime.AppPreferences;
import mplayer4anime.IPlayer;
import mplayer4anime.MediatorControl;
import mplayer4anime.mplayer.MplayerSlave;
import mplayer4anime.mpv.MpvSlave;
import mplayer4anime.playlists.JsonStorage;
import mplayer4anime.playlists.Playlists;
import mplayer4anime.ui.ServiceWindow;
import mplayer4anime.ui.about.AboutWindow;
import mplayer4anime.ui.landing.panes.ControllerPane;
import mplayer4anime.ui.landing.panes.ControllerPaneSubtitles;
import mplayer4anime.ui.settings.SettingsWindow;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class LandingController implements Initializable {
    @FXML
    private ControllerPane mkvPaneController, mkaPaneController;
    @FXML
    private ControllerPaneSubtitles subPaneController;
    @FXML
    private PlayerToolbarController playerToolbarController;
    @FXML
    private Label statusLbl;
    @FXML
    private Menu recentlyOpenedMenu;

    @FXML
    private TabPane tabPane;

    private final AppPreferences appPreferences = AppPreferences.get();

    private ResourceBundle resourceBundle;
    private HostServices hostServices;
    private String currentPlaylistLocation;
    private String backend;

    private IPlayer player;

    // If application started with playlist passed as an argument, then we'll try to load it (if it's valid).
    public void setPlaylistAsArgument(String playlist) {
        var jsonStorage = Playlists.readByPath(resourceBundle, new File(playlist));
        setAllLists(jsonStorage);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        // Register this controller in mediator
        MediatorControl.getInstance().registerMainController(this);

        mkvPaneController.setPaneType("Video");
        mkaPaneController.setPaneType("Audio");
        subPaneController.setPaneType("Subtitles");

        // Set default list of encodings of the subtitles files:
        subPaneController.setEncoding(appPreferences.getSubsEncodingList(), appPreferences.getLastTimeUsedSubsEncoding());

        // If subtitles should be opened first, per user's settings let's show it first
        if (appPreferences.getSubtitlesFirst()) {
            tabPane.getSelectionModel().select(1);  // 0 is mka 1 is subs
        }

        var recentPlaylists = appPreferences.getRecentPlaylists();
        for (int i = recentPlaylists.length - 1; i >= 0; i--) {
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

        playerToolbarController.initializeMainUiController(player, mkvPaneController, mkaPaneController, subPaneController);

        if (appPreferences.getOpenLatestPlaylistOnStart())
            loadLatestPlaylist();
    }

    private void loadLatestPlaylist() {
        try {
            var recentPlaylist = appPreferences.getRecentPlaylist();
            if ("".equals(recentPlaylist))
                return;
            // TODO: IF video set, if playlist has been opened before
            var jsonStorage = Playlists.readByPathSilent(new File(recentPlaylist));
            setAllLists(jsonStorage);
            mkvPaneController.setElementSelectedByIndex(appPreferences.getLatestPositionVideo());
            mkaPaneController.setElementSelectedByIndex(appPreferences.getLatestPositionAudio());
            subPaneController.setElementSelectedByIndex(appPreferences.getLatestPositionSubs());
        }
        catch (Exception ignore) {
        }
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    private void closeBtn() {
        var currentStage = (Stage) tabPane.getScene().getWindow();
        currentStage.close();
    }

    // Will be used to store lists previously opened.
    // Linkage established by ohHidden in MainFX.java class
    public void shutdown() {
        appPreferences.setLastTimeUsedSubsEncoding(subPaneController.getSelectedEncoding());
        playerToolbarController.shutdown();
        // TODO: remove from here; too sophisticated
        var storeRecentArr = new String[10];
        for (var i = 0; i < recentlyOpenedMenu.getItems().size() - 2 && !(i > 9); i++) {       // Don't take separator and Clean button
            storeRecentArr[i] = (String) recentlyOpenedMenu.getItems().get(i).getUserData();
        }
        appPreferences.setRecentPlaylists(storeRecentArr);

        appPreferences.setLatestPositionVideo(mkvPaneController.getElementSelectedIndex());
        appPreferences.setLatestPositionAudio(mkaPaneController.getElementSelectedIndex());
        appPreferences.setLatestPositionSubs(subPaneController.getElementSelectedIndex());

        Platform.exit();
    }

    @FXML
    private void infoBtn() throws Exception {
        new AboutWindow(hostServices);
    } // TODO: fix this shit with hostSerivces that doesn't work @ linux

    /**
     * SETTINGS HANDLE
     */
    @FXML
    private void settingsBtn() throws Exception {
        new SettingsWindow();
    }

    // Get event that notify application in case some settings has been changed
    // This function called from MediatorControl after mediator receives request form SettingsController indicating that user updated some required fields.
    public void updateAfterSettingsChanged() {
        /* update list of encoding */
        subPaneController.setEncoding(appPreferences.getSubsEncodingList(), null);
        // In case of application failure should be better to save this immediately
        appPreferences.setLastTimeUsedSubsEncoding(subPaneController.getSelectedEncoding());
        switchBackend(appPreferences.getBackendEngineIndexId());
    }

    private void switchBackend(int newBackEndId) {
        if (newBackEndId == 0 && backend.equals("mpv")) {
            backend = "mplayer";
            player = new MplayerSlave(resourceBundle);
            return;
        }
        if (newBackEndId == 1 && backend.equals("mplayer")) {
            backend = "mpv";
            player = new MpvSlave(resourceBundle);
        }
    }

    @FXML
    private void openBtn() {
        var jsonStorage = Playlists.openPlaylistFileChooser(resourceBundle);
        setAllLists(jsonStorage);
    }

    private void setAllLists(JsonStorage jsonStorage) {
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
        var jsonStorage = new JsonStorage(
                mkvPaneController.getElementsAll(),
                mkaPaneController.getElementsAll(),
                subPaneController.getElementsAll(),
                subPaneController.getSelectedEncoding());
        if (Playlists.saveCurrent(resourceBundle, jsonStorage)) {
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

        var jsonStorage = new JsonStorage(
                mkvPaneController.getElementsAll(),
                mkaPaneController.getElementsAll(),
                subPaneController.getElementsAll(),
                subPaneController.getSelectedEncoding());
        if (Playlists.saveAs(resourceBundle, jsonStorage)) {
            this.currentPlaylistLocation = Playlists.getPlaylistLocation();
            this.statusLbl.setText(currentPlaylistLocation);    //TODO: update header of the application to include this?
            addRecentlyOpened(currentPlaylistLocation);
        }
    }

    @FXML
    private void cleanAllRecentlyOpened() {
        recentlyOpenedMenu.getItems().remove(0, recentlyOpenedMenu.getItems().size() - 2);
    }

    private void addRecentlyOpened(String playlistPath) {
        var items = recentlyOpenedMenu.getItems();
        var existing = items.stream()
                .filter(item -> playlistPath.equals(item.getUserData()))
                .findFirst();
        if (existing.isPresent()) {
            items.remove(existing.get());
            items.addFirst(existing.get());
            return;
        }

        var playListName = playlistPath.substring(
                playlistPath.lastIndexOf(File.separator) + 1, playlistPath.lastIndexOf("."));
        var menuItem = new MenuItem();
        menuItem.setText(playListName);
        menuItem.setUserData(playlistPath);
        menuItem.setOnAction(_ ->
                setAllLists(Playlists.readByPath(resourceBundle, new File(playlistPath))));
        // Limit list to 13 elements (2 in the end are separator and clear button)
        if (items.size() >= 11)
            items.remove(9, recentlyOpenedMenu.getItems().size() - 2);
        items.addFirst(menuItem);
    }
}

