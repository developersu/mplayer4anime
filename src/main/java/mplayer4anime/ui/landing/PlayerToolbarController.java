/*
    Copyright 2018-2021 Dmitry Isaenko
     
    This file is part of mcontroller.player.anime.

    mcontroller.player.anime is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    mcontroller.player.anime is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with mcontroller.player.anime.  If not, see <https://www.gnu.org/licenses/>.
 */
package mplayer4anime.ui.landing;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckMenuItem;
import mplayer4anime.AppPreferences;

import java.net.URL;
import java.util.ResourceBundle;

public class PlayerToolbarController implements Initializable {
    @FXML
    private CheckMenuItem fullScreen;
    @FXML
    private CheckMenuItem subsHide;

    private AppPreferences appPreferences;
    private LandingController landingController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.appPreferences = AppPreferences.getINSTANCE();
        
        fullScreen.setSelected(appPreferences.getFullScreenSelected());
        subsHide.setSelected(appPreferences.getSubtitlesHideSelected());
    }
    public void initializeMainUiController(LandingController landingController){
        this.landingController = landingController;
    }
    
    @FXML
    private void subsTriggerBtn(){
        landingController.player.subtitlesSwitch();
    }
    @FXML
    private void fullscreenBtn(){
        landingController.player.fullscreenSwitch();
    }
    @FXML
    private void muteBtn(){
        landingController.player.mute();
    }
    @FXML
    private void playPrevTrackBtn(){
        int index = landingController.mkvPaneController.getElementSelectedIndex();
        if (index <= 0)
            return;

        landingController.mkvPaneController.setElementSelectedByIndex(index-1);
        landingController.player.forcePlay(appPreferences.getPath(),
                landingController.mkvPaneController.getElementSelected(),
                landingController.mkaPaneController.getElementSelected(),
                landingController.subPaneController.getElementSelected(),
                landingController.subPaneController.getSelectedEncoding(),
                subsHide.isSelected(),
                fullScreen.isSelected()
        );
    }
    @FXML
    private void playNextTrackBtn(){
        int index = landingController.mkvPaneController.getElementSelectedIndex();

        if (index + 1 < landingController.mkvPaneController.getElementsCount()) {
            landingController.mkvPaneController.setElementSelectedByIndex(index + 1);
        }
        index = landingController.mkaPaneController.getElementSelectedIndex();
        if (index + 1 < landingController.mkaPaneController.getElementsCount()) {
            landingController.mkaPaneController.setElementSelectedByIndex(index + 1);
        }
        index = landingController.subPaneController.getElementSelectedIndex();
        if (index + 1 < landingController.subPaneController.getElementsCount()) {
            landingController.subPaneController.setElementSelectedByIndex(index + 1);
        }

        landingController.player.forcePlay(appPreferences.getPath(),
                landingController.mkvPaneController.getElementSelected(),
                landingController.mkaPaneController.getElementSelected(),
                landingController.subPaneController.getElementSelected(),
                landingController.subPaneController.getSelectedEncoding(),
                subsHide.isSelected(),
                fullScreen.isSelected()
        );
    }
    @FXML
    private void playBtn(){
        if (landingController.mkvPaneController.getElementSelected() == null)
            return;

        landingController.player.playPause(appPreferences.getPath(),
                landingController.mkvPaneController.getElementSelected(),
                landingController.mkaPaneController.getElementSelected(),
                landingController.subPaneController.getElementSelected(),
                landingController.subPaneController.getSelectedEncoding(),
                subsHide.isSelected(),
                fullScreen.isSelected()
        );
    }
    @FXML
    private void stopBtn(){
        landingController.player.stop();
    }
    @FXML
    private void volumeUpBtn(){
        landingController.player.volumeUp();
    }
    @FXML
    private void volumeDownBtn(){
        landingController.player.volumeDown();
    }

    void shutdown(){
        appPreferences.setFullScreenSelected(fullScreen.isSelected());
        appPreferences.setSubtitlesHideSelected(subsHide.isSelected());
    }
}
