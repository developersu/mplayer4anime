package sample;
/***********************************************
* Name: mplayer4anime                          *
* Author: Dmitry Isaenko                       *
* License: GNU GPL v.3                         *
* Version: 0.5.2                               *
* Site: https://developersu.blogspot.com/      *
* 2018, Russia                                 *
***********************************************/

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader loader = new FXMLLoader(getClass().getResource("landingPage.fxml"));

        Parent root = loader.load();

        // tmp?
        Controller controller = loader.getController();
        controller.setHostServices(getHostServices());
        // end

        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/sample/res/app.png")));
        primaryStage.setTitle("mplayer4anime");
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(375);
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.show();
    }
}
