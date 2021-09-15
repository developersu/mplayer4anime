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

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mplayer4anime.ipc.SingleInstanceHandler;
import mplayer4anime.ui.landing.LandingController;

import java.util.Locale;
import java.util.ResourceBundle;

//TODO: Use one copy of AppPreferences object widely
//TODO: Drag-n-drop playlist/files/audio
//TODO: remember selected
//TODO: remember position
public class MainFX extends Application {

    public static void main(String[] args) { launch(args);  }

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/LandingPage.fxml"));

        Locale userLocale = new Locale(Locale.getDefault().getISO3Language());      // NOTE: user locale based on ISO3 Language codes
        ResourceBundle rb = ResourceBundle.getBundle("locale", userLocale);
        loader.setResources(rb);

        Parent root = loader.load();

        LandingController landingController = loader.getController();
        landingController.setHostServices(getHostServices());
        SingleInstanceHandler singleInstanceHandler;

        if (getParameters().getUnnamed().isEmpty())
            singleInstanceHandler = new SingleInstanceHandler(landingController, null);
        else
            singleInstanceHandler = new SingleInstanceHandler(landingController, getParameters().getUnnamed().get(0));

        primaryStage.getIcons().addAll(
                new Image(getClass().getResourceAsStream("/res/app_icon32x32.png")),
                new Image(getClass().getResourceAsStream("/res/app_icon48x48.png")),
                new Image(getClass().getResourceAsStream("/res/app_icon64x64.png")),
                new Image(getClass().getResourceAsStream("/res/app_icon128x128.png"))
        );
        primaryStage.setTitle("mplayer4anime");
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(375);
        Scene scene = new Scene(root,
                AppPreferences.getINSTANCE().getSceneWidth(),
                AppPreferences.getINSTANCE().getSceneHeight());
        primaryStage.setScene(scene);
        // Make linkage to controller method to handle exit() event in there.
        primaryStage.setOnHidden(e -> {
            AppPreferences.getINSTANCE().setSceneHeight(scene.getHeight());
            AppPreferences.getINSTANCE().setSceneWidth(scene.getWidth());
            singleInstanceHandler.finishWork();
            landingController.shutdown();
        });
        //Runtime.getRuntime().addShutdownHook(new Thread(sihThread::interrupt));
        primaryStage.show();
    }
}
