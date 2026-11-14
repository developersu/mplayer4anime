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

package mplayer4anime.ui.about;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AboutController implements Initializable {

    private HostServices hostServices;

    void setHostServices(HostServices hs) {
        this.hostServices = hs;
    }

    @FXML
    private Button buttonOk;
    @FXML
    private Label versionLbl;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        versionLbl.setText(ResourceBundle.getBundle("app").getString("_version"));
    }

    @FXML
    private void buttonClickOk() {
        var thisStage = (Stage) buttonOk.getScene().getWindow();
        thisStage.close();
    }

    @FXML
    private void gitHubUrl() {
        hostServices.showDocument("https://github.com/developersu/mplayer4anime");
    }

    @FXML
    private void bloggerUrl() {
        hostServices.showDocument("https://developersu.blogspot.com/search/label/mplayer4anime");
    }
}
