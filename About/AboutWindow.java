package mplayer4anime.About;

import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mplayer4anime.Main;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class AboutWindow {

    public AboutWindow(HostServices hostServices){
        Stage stageAbout = new Stage();

        stageAbout.setMinWidth(500);
        stageAbout.setMinHeight(230);

        FXMLLoader loaderAbout = new FXMLLoader(getClass().getResource("AboutLayout.fxml"));
        ResourceBundle resourceBundle;

        if (Locale.getDefault().getISO3Language().equals("rus")) {
            resourceBundle = ResourceBundle.getBundle("mplayer4anime.localization.locale", new Locale("ru"));
        } else {
            resourceBundle = ResourceBundle.getBundle("mplayer4anime.localization.locale", new Locale("en"));
        }
        loaderAbout.setResources(resourceBundle);

        try {
            Parent parentAbout = loaderAbout.load();
            AboutController abtController = loaderAbout.getController();
            abtController.setHostServices(hostServices);

            stageAbout.setTitle(resourceBundle.getString("about_AboutName"));
            stageAbout.getIcons().addAll(
                    new Image(Main.class.getResourceAsStream("/mplayer4anime/res/app_icon32x32.png")),
                    new Image(Main.class.getResourceAsStream("/mplayer4anime/res/app_icon48x48.png")),
                    new Image(Main.class.getResourceAsStream("/mplayer4anime/res/app_icon64x64.png")),
                    new Image(Main.class.getResourceAsStream("/mplayer4anime/res/app_icon128x128.png"))
            ); // TODO: change to something reliable
            stageAbout.setScene(new Scene(parentAbout, 500, 230));

            stageAbout.show();

        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
