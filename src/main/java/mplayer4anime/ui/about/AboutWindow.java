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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mplayer4anime.AppPreferences;

import java.util.ResourceBundle;

public class AboutWindow {

    public AboutWindow(HostServices hostServices) throws Exception {
        var stageAbout = new Stage();
        stageAbout.setMinWidth(500);
        stageAbout.setMinHeight(300);

        var loaderAbout = new FXMLLoader(getClass().getResource("/About/AboutLayout.fxml"));
        var resourceBundle = ResourceBundle.getBundle("locale", AppPreferences.get().getLocale());
        loaderAbout.setResources(resourceBundle);

        Parent parentAbout = loaderAbout.load();
        ((AboutController) loaderAbout.getController())
                .setHostServices(hostServices);

        stageAbout.setTitle(resourceBundle.getString("about_AboutName"));
        stageAbout.getIcons().addAll(
                new Image("/res/app_icon32x32.png"),
                new Image("/res/app_icon48x48.png"),
                new Image("/res/app_icon64x64.png"),
                new Image("/res/app_icon128x128.png"));
        stageAbout.setScene(new Scene(parentAbout, 500, 300));
        stageAbout.show();
    }
}
