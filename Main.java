package sample;
/***********************************************
* Name: mplayer4anime                          *
* Author: Dmitry Isaenko                       *
* License: GNU GPL v.3                         *
* Version: 0.1                                 *
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


    @Override
    public void start(Stage primaryStage) throws Exception{

        Controller myController = new Controller();
        myController.thisStage = primaryStage;

        FXMLLoader loader = new FXMLLoader();
        loader.setController(myController);
        Parent root = loader.load(getClass().getResource("landingPage.fxml"));

        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/sample/app.png")));
        primaryStage.setTitle("mplayer4anime");
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(300);
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);

    }
}
