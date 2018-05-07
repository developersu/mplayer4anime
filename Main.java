package mplayer4anime;
/***********************************************
* Name: mplayer4anime                          *
* Author: Dmitry Isaenko                       *
* License: GNU GPL v.3                         *
* Version: 0.9                                 *
* Site: https://developersu.blogspot.com/      *
* 2018, Russia                                 *
***********************************************/

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;


public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("landingPage.fxml"));

        if (Locale.getDefault().getISO3Language().equals("rus")) {
            loader.setResources(ResourceBundle.getBundle("mplayer4anime.localization.locale", new Locale("ru")));
        } else {
            loader.setResources(ResourceBundle.getBundle("mplayer4anime.localization.locale", new Locale("en")));
        }

        Parent root = loader.load();

        // tmp?
        Controller controller = loader.getController();
        controller.setHostServices(getHostServices());
        // end

        primaryStage.getIcons().addAll(
                new Image(Main.class.getResourceAsStream("/mplayer4anime/res/app_icon32x32.png")),
                new Image(Main.class.getResourceAsStream("/mplayer4anime/res/app_icon48x48.png")),
                new Image(Main.class.getResourceAsStream("/mplayer4anime/res/app_icon64x64.png")),
                new Image(Main.class.getResourceAsStream("/mplayer4anime/res/app_icon128x128.png"))
        );
        primaryStage.setTitle("mplayer4anime");
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(375);
        primaryStage.setScene(new Scene(root, 1200, 800));
        // Make linkage to controller method to handle exit() event in there.
        primaryStage.setOnHidden(e -> controller.shutdown());
        primaryStage.show();
    }
}
