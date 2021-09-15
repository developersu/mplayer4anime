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
package mplayer4anime.ui.settings;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class SettingsWindow {

    public SettingsWindow(){
        Stage stageAbout = new Stage();

        stageAbout.setMinWidth(570);
        stageAbout.setMinHeight(500);

        FXMLLoader loaderSettings = new FXMLLoader(getClass().getResource("/Settings/SettingsLayout.fxml"));
        ResourceBundle resourceBundle;

        Locale userLocale = new Locale(Locale.getDefault().getISO3Language());      // NOTE: user locale based on ISO3 Language codes
        resourceBundle = ResourceBundle.getBundle("locale", userLocale);
        loaderSettings.setResources(resourceBundle);


        try {
            Parent parentAbout = loaderSettings.load();

            stageAbout.setTitle(resourceBundle.getString("settings_SettingsName"));
            stageAbout.getIcons().addAll(
                    new Image("/res/settings_icon32x32.png"),
                    new Image("/res/settings_icon32x32.png"),
                    new Image("/res/settings_icon48x48.png"),
                    new Image("/res/settings_icon64x64.png"),
                    new Image("/res/settings_icon128x128.png"));
            stageAbout.setScene(new Scene(parentAbout, 570, 500));
            stageAbout.show();

        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
