package mplayer4anime.ui.about;

import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mplayer4anime.MainFX;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class AboutWindow {

    public AboutWindow(HostServices hostServices){
        Stage stageAbout = new Stage();

        stageAbout.setMinWidth(500);
        stageAbout.setMinHeight(500);

        FXMLLoader loaderAbout = new FXMLLoader(getClass().getResource("/About/AboutLayout.fxml"));
        ResourceBundle resourceBundle;

        Locale userLocale = new Locale(Locale.getDefault().getISO3Language());      // NOTE: user locale based on ISO3 Language codes
        resourceBundle = ResourceBundle.getBundle("locale", userLocale);
        loaderAbout.setResources(resourceBundle);

        try {
            Parent parentAbout = loaderAbout.load();
            AboutController abtController = loaderAbout.getController();
            abtController.setHostServices(hostServices);

            stageAbout.setTitle(resourceBundle.getString("about_AboutName"));
            stageAbout.getIcons().addAll(
                    new Image(MainFX.class.getResourceAsStream("/res/app_icon32x32.png")),
                    new Image(MainFX.class.getResourceAsStream("/res/app_icon48x48.png")),
                    new Image(MainFX.class.getResourceAsStream("/res/app_icon64x64.png")),
                    new Image(MainFX.class.getResourceAsStream("/res/app_icon128x128.png"))
            ); // TODO: change to something reliable
            stageAbout.setScene(new Scene(parentAbout, 500, 500));

            stageAbout.show();

        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
