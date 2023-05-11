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
package mplayer4anime.ui.landing;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.SplitMenuButton;
import mplayer4anime.AppPreferences;
import mplayer4anime.IPlayer;
import mplayer4anime.ui.landing.panes.ControllerPane;
import mplayer4anime.ui.landing.panes.ControllerPaneSubtitles;

import java.net.URL;
import java.util.ResourceBundle;

public class PlayerToolbarController implements Initializable {
    @FXML
    private CheckMenuItem fullScreen;
    @FXML
    private CheckMenuItem subsHide;
    @FXML
    private Button muteBtn, playPrevTrackBtn, playNextTrackBtn, playBtn, stopBtn, volumeUpBtn, volumeDownBtn;
    @FXML
    private SplitMenuButton subsTriggerBtn, fullscreenBtn;

    private AppPreferences appPreferences;
    private IPlayer player;
    private ControllerPane mkvPaneController, mkaPaneController;
    private ControllerPaneSubtitles subPaneController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.appPreferences = AppPreferences.getINSTANCE();

        fullScreen.setSelected(appPreferences.getFullScreenSelected());
        subsHide.setSelected(appPreferences.getSubtitlesHideSelected());

        stopBtn.setOnAction(event -> player.stop());
        volumeUpBtn.setOnAction(event -> player.volumeUp());
        volumeDownBtn.setOnAction(event -> player.volumeDown());
        muteBtn.setOnAction(event -> player.mute());
        playBtn.setOnAction(event -> play());
        playPrevTrackBtn.setOnAction(event -> playPrevTrack());
        playNextTrackBtn.setOnAction(event -> playNextTrack());
        subsTriggerBtn.setOnAction(event -> player.subtitlesSwitch());
        fullscreenBtn.setOnAction(event -> player.fullscreenSwitch());
    }
    public void initializeMainUiController(IPlayer player,
                                           ControllerPane mkvPaneController,
                                           ControllerPane  mkaPaneController,
                                           ControllerPaneSubtitles subPaneController){
        this.player = player;
        this.mkvPaneController = mkvPaneController;
        this.mkaPaneController = mkaPaneController;
        this.subPaneController = subPaneController;

        bindButtonToEmptyList(stopBtn);
        bindButtonToEmptyList(volumeUpBtn);
        bindButtonToEmptyList(volumeDownBtn);
        bindButtonToEmptyList(muteBtn);
        bindButtonToEmptyList(playBtn);
        bindButtonToEmptyList(subsTriggerBtn);
        bindButtonToEmptyList(fullscreenBtn);

        playPrevTrackBtn.disableProperty().bind(
                Bindings.isEmpty(mkvPaneController.getPaneFileList()).or(
                Bindings.equal(mkvPaneController.getSelectedIndexProperty(), 0)
                ));

        playNextTrackBtn.disableProperty().bind(
                Bindings.isEmpty(mkvPaneController.getPaneFileList()).or(
                Bindings.equal(mkvPaneController.getSelectedIndexProperty(),
                        Bindings.subtract(Bindings.size(mkvPaneController.getPaneFileList()), 1))
                ));
    }
    private void bindButtonToEmptyList(ButtonBase button){
        button.disableProperty().bind(Bindings.isEmpty(mkvPaneController.getPaneFileList()));
    }

    private void play(){
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

    private void playPrevTrack(){
        int index = mkvPaneController.getElementSelectedIndex() - 1;
        if (index < 0)
            return;

        mkvPaneController.setElementSelectedByIndex(index);

        index = mkaPaneController.getElementSelectedIndex();
        if (index > 0)
            mkaPaneController.setElementSelectedByIndex(index - 1);

        index = subPaneController.getElementSelectedIndex();
        if (index > 0)
            subPaneController.setElementSelectedByIndex(index - 1);

        player.forcePlay(appPreferences.getPath(),
                mkvPaneController.getElementSelected(),
                mkaPaneController.getElementSelected(),
                subPaneController.getElementSelected(),
                subPaneController.getSelectedEncoding(),
                subsHide.isSelected(),
                fullScreen.isSelected()
        );
    }

    private void playNextTrack(){
        int index = mkvPaneController.getElementSelectedIndex() + 1;
        if (index >= mkvPaneController.getElementsCount())
            return;

        mkvPaneController.setElementSelectedByIndex(index);

        index = mkaPaneController.getElementSelectedIndex() + 1;
        if (index < mkaPaneController.getElementsCount())
            mkaPaneController.setElementSelectedByIndex(index);

        index = subPaneController.getElementSelectedIndex() + 1;
        if (index < subPaneController.getElementsCount())
            subPaneController.setElementSelectedByIndex(index);

        player.forcePlay(appPreferences.getPath(),
                mkvPaneController.getElementSelected(),
                mkaPaneController.getElementSelected(),
                subPaneController.getElementSelected(),
                subPaneController.getSelectedEncoding(),
                subsHide.isSelected(),
                fullScreen.isSelected()
        );
    }

    void shutdown(){
        appPreferences.setFullScreenSelected(fullScreen.isSelected());
        appPreferences.setSubtitlesHideSelected(subsHide.isSelected());
    }
}
