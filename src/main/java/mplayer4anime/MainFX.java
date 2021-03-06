package mplayer4anime;
/**
Name: mplayer4anime
@author Dmitry Isaenko
License: GNU GPL v.3
@version 0.12
@see https://developersu.blogspot.com/search/label/mplayer4anime
@see https://github.com/developersu/mplayer4anime
2018-2019, Russia
*/

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mplayer4anime.IPC.SingleInstanceHandler;

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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/landingPage.fxml"));

        Locale userLocale = new Locale(Locale.getDefault().getISO3Language());      // NOTE: user locale based on ISO3 Language codes
        ResourceBundle rb = ResourceBundle.getBundle("locale", userLocale);
        loader.setResources(rb);

        Parent root = loader.load();

        // tmp?
        Controller controller = loader.getController();
        controller.setHostServices(getHostServices());
        SingleInstanceHandler sih;

        if (!getParameters().getUnnamed().isEmpty())
            sih = new SingleInstanceHandler(controller, getParameters().getUnnamed().get(0));
        else
            sih = new SingleInstanceHandler(controller, null);
        // end
        Thread tsih = new Thread(sih);
        tsih.start();

        // TODO: refactor needed?
        Runtime.getRuntime().addShutdownHook(new Thread(() -> tsih.interrupt()));

        primaryStage.getIcons().addAll(
                new Image(MainFX.class.getResourceAsStream("/res/app_icon32x32.png")),
                new Image(MainFX.class.getResourceAsStream("/res/app_icon48x48.png")),
                new Image(MainFX.class.getResourceAsStream("/res/app_icon64x64.png")),
                new Image(MainFX.class.getResourceAsStream("/res/app_icon128x128.png"))
        );
        primaryStage.setTitle("mplayer4anime");
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(375);
        primaryStage.setScene(new Scene(root, 1200, 800));
        // Make linkage to controller method to handle exit() event in there.
        primaryStage.setOnHidden(e -> {
            tsih.interrupt();
            controller.shutdown();
        });

        primaryStage.show();
    }
}
