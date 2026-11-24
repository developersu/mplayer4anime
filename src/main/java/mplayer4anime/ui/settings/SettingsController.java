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

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import mplayer4anime.AppPreferences;
import mplayer4anime.BackendEngine;
import mplayer4anime.ui.LocaleHolder;
import mplayer4anime.ui.ServiceWindow;
import mplayer4anime.ui.SettingsLanguagesSetup;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

import static mplayer4anime.ui.settings.ListSelectorController.InputMode.ENCODING;
import static mplayer4anime.ui.settings.ListSelectorController.InputMode.EXTENSION;

public class SettingsController implements Initializable {
    private static final boolean IS_WINDOWS = System.getProperty("os.name").contains("Windows");
    private static final String DEFAULT_PATH_WINDOWS = "mplayer\\mplayer.exe";
    private static final String DEFAULT_PATH = "mplayer";

    private final AppPreferences appPreferences = AppPreferences.get();
    private LocaleHolder recentLanguage;
    private Runnable onApply;

    public void setOnApplyCallback(Runnable onApply) {
        this.onApply = onApply;
    }

    @FXML
    private ListSelectorController subExtensionListController,
            subEncodingListController,
            videoExtensionListController,
            audioExtensionListController;
    @FXML
    private Label pathToMplayerLbl;
    @FXML
    private CheckBox subtitlesFirstCheckBox,
            openLatestPlaylistCheckBox,
            autoImportExternalFilesCheckBox,
            playNextOnEndCheckBox;
    @FXML
    private ChoiceBox<BackendEngine> backEndEngineChoiceBox;
    @FXML
    private ChoiceBox<LocaleHolder> languageChoiceBox;
    @FXML
    private Button findPathToMplayerBtn, clearPathBtn, okBtn, applyBtn, cancelBtn;

    @Override
    public void initialize(URL url, ResourceBundle resBundle) {
        bindButtons();
        loadFromPreferences();
        fillLanguageSelector();
    }

    private void bindButtons() {
        findPathToMplayerBtn.setOnAction(_ -> findPathToPlayer());
        clearPathBtn.setOnAction(_ -> clearPath());
        okBtn.setOnAction(_ -> ok());
        applyBtn.setOnAction(_ -> apply());
        cancelBtn.setOnAction(_ -> close());
    }

    private void loadFromPreferences() {
        pathToMplayerLbl.setText(appPreferences.getPath());
        subtitlesFirstCheckBox.setSelected(appPreferences.getSubtitlesFirst());
        openLatestPlaylistCheckBox.setSelected(appPreferences.getOpenLatestPlaylistOnStart());
        autoImportExternalFilesCheckBox.setSelected(appPreferences.getAutoImportExternalFiles());
        playNextOnEndCheckBox.setSelected(appPreferences.getPlayNextOnEnd());

        // Fill lists of extensions and encodings
        subExtensionListController.configure(appPreferences.getSubsExtensionsList(), EXTENSION);
        subEncodingListController.configure(appPreferences.getSubsEncodingList(), ENCODING);
        videoExtensionListController.configure(appPreferences.getVideoExtensionsList(), EXTENSION);
        audioExtensionListController.configure(appPreferences.getAudioExtensionsList(), EXTENSION);
        backEndEngineChoiceBox.getItems().setAll(BackendEngine.values());
        backEndEngineChoiceBox.getSelectionModel().select(appPreferences.getBackendEngine());
    }

    private void fillLanguageSelector() {
        var settingsLanguagesSetup = new SettingsLanguagesSetup();
        recentLanguage = settingsLanguagesSetup.getRecentLanguage();
        languageChoiceBox.setItems(settingsLanguagesSetup.getLanguages());
        languageChoiceBox.getSelectionModel().select(recentLanguage);
    }

    private void findPathToPlayer() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("mplayer");

        // In case we use Windows, limit selectable file to .exe
        if (IS_WINDOWS)
            fileChooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter("mplayer", "*.exe"));

        var mplayerExecutableFile = fileChooser.showOpenDialog(null);

        if (mplayerExecutableFile != null)
            pathToMplayerLbl.setText(mplayerExecutableFile.toString());
    }

    private void clearPath() {
        pathToMplayerLbl.setText(IS_WINDOWS ?
                DEFAULT_PATH_WINDOWS :
                DEFAULT_PATH);
    }

    private void ok() {
        apply();
        close();
    }

    private void apply() {
        storePreferences();
        onApply.run();
        applyLanguageChange();
    }

    private void storePreferences() {
        appPreferences.setPath(pathToMplayerLbl.getText());
        appPreferences.setSubtitlesFirst(subtitlesFirstCheckBox.isSelected());
        appPreferences.setSubsExtensionsList(subExtensionListController.getList());
        appPreferences.setSubsEncodingList(subEncodingListController.getList());
        appPreferences.setVideoExtensionsList(videoExtensionListController.getList());
        appPreferences.setAudioExtensionsList(audioExtensionListController.getList());
        appPreferences.setBackendEngine(backEndEngineChoiceBox.getValue());
        appPreferences.setOpenLatestPlaylistOnStart(openLatestPlaylistCheckBox.isSelected());
        appPreferences.setAutoImportExternalFiles(autoImportExternalFilesCheckBox.isSelected());
        appPreferences.setPlayNextOnEnd(playNextOnEndCheckBox.isSelected());
    }

    private void applyLanguageChange() {
        var localeHolder = languageChoiceBox.getSelectionModel().getSelectedItem();
        if (Objects.equals(recentLanguage, localeHolder))
            return;
        // In case user willing to change it several times
        recentLanguage = localeHolder;
        appPreferences.setLocale(localeHolder.getLocaleCode());
        ServiceWindow.getInfoNotification("",
                ResourceBundle.getBundle("locale", localeHolder.getLocale())
                        .getString("windowBodyRestartToApplyLang"));
    }

    private void close() {
        var currentWindowStage = (Stage) pathToMplayerLbl.getScene().getWindow();
        currentWindowStage.close();
    }
}
