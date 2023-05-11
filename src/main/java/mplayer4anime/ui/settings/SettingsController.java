/*
    Copyright 2018-2023 Dmitry Isaenko

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

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import mplayer4anime.AppPreferences;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import mplayer4anime.MediatorControl;

public class SettingsController implements Initializable {
    private AppPreferences appPreferences;
    @FXML
    private ControllerListsSelector subExtensionListController,
            subEncodingListController,
            videoExtensionListController,
            audioExtensionListController;
    @FXML
    private Label pathToMplayerLbl;
    @FXML
    private CheckBox subtitlesFirstCheckBox, openLatestPlaylistCheckBox;
    @FXML
    private ChoiceBox<String> backEndEngineChoiceBox;

    @Override
    public void initialize(URL url, ResourceBundle resBundle) {
        appPreferences = AppPreferences.getINSTANCE();
        pathToMplayerLbl.setText(appPreferences.getPath());

        // Subtitles should be shown first? If TRUE, then set checkbox.
        subtitlesFirstCheckBox.setSelected(appPreferences.getSubtilesFirst());
        // Fill lists of extensions and encodings
        subExtensionListController.setList(appPreferences.getSubsExtensionsList(), true);
        subEncodingListController.setList(appPreferences.getSubsEncodingList(), false);
        videoExtensionListController.setList(appPreferences.getVideoExtensionsList(), true);
        audioExtensionListController.setList(appPreferences.getAudioExtensionsList(), true);
        backEndEngineChoiceBox.getItems().addAll("mplayer", "mpv");
        backEndEngineChoiceBox.getSelectionModel().select(appPreferences.getBackendEngineIndexId());
        openLatestPlaylistCheckBox.setSelected(appPreferences.getOpenLatestPlaylistOnStart());
    }

    @FXML
    private void findPathToMplayer(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("mplayer");

        // In case we use Windows, limit selectable file to .exe
        if (System.getProperty("os.name").contains("Windows"))
            fileChooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter("mplayer", "*.exe"));

        File mplayerExecutableFile = fileChooser.showOpenDialog(null);

        if (mplayerExecutableFile != null)
            pathToMplayerLbl.setText(mplayerExecutableFile.toString());
    }

    @FXML
    private void clearPath(){
        if (System.getProperty("os.name").contains("Windows"))
            pathToMplayerLbl.setText("mplayer\\mplayer.exe");
        else
            pathToMplayerLbl.setText("mplayer");
    }

    @FXML
    private void Cancel(){
        close();
    }

    @FXML
    private void Ok(){
        Apply();
        close();
    }

    @FXML
    private void Apply(){
        appPreferences.setPath(pathToMplayerLbl.getText());
        appPreferences.setSubtilesFirst(subtitlesFirstCheckBox.isSelected());
        appPreferences.setSubsExtensionsList(subExtensionListController.getList());
        appPreferences.setSubsEncodingList(subEncodingListController.getList());
        appPreferences.setVideoExtensionsList(videoExtensionListController.getList());
        appPreferences.setAudioExtensionsList(audioExtensionListController.getList());
        appPreferences.setBackendEngineIndexId(backEndEngineChoiceBox.getSelectionModel().getSelectedIndex());
        appPreferences.setOpenLatestPlaylistOnStart(openLatestPlaylistCheckBox.isSelected());

        MediatorControl.getInstance().updateAfterSettingsChanged();    // TODO: implement list to track what should be updated
    }

    private void close(){
        Stage currentWindowStage = (Stage) pathToMplayerLbl.getScene().getWindow();
        currentWindowStage.close();
    }
}
