package mplayer4anime.Settings;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mplayer4anime.MainFX;

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
            //SettingsController settingsController = loaderSettings.getController();

            stageAbout.setTitle(resourceBundle.getString("settings_SettingsName"));
            stageAbout.getIcons().addAll(
                    new Image(MainFX.class.getResourceAsStream("/res/settings_icon32x32.png")),
                    new Image(MainFX.class.getResourceAsStream("/res/settings_icon48x48.png")),
                    new Image(MainFX.class.getResourceAsStream("/res/settings_icon64x64.png")),
                    new Image(MainFX.class.getResourceAsStream("/res/settings_icon128x128.png"))
            );       // TODO: change to something reliable
            stageAbout.setScene(new Scene(parentAbout, 570, 500));
            stageAbout.show();

        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
