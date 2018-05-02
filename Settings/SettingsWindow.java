package mplayer4anime.Settings;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mplayer4anime.Main;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class SettingsWindow {

    public SettingsWindow(){
        Stage stageAbout = new Stage();

        stageAbout.setMinWidth(570);
        stageAbout.setMinHeight(500);

        FXMLLoader loaderSettings = new FXMLLoader(getClass().getResource("SettingsLayout.fxml"));
        ResourceBundle resourceBundle;

        if (Locale.getDefault().getISO3Language().equals("rus")) {
            resourceBundle = ResourceBundle.getBundle("mplayer4anime.localization.locale", new Locale("ru"));
        } else {
            resourceBundle = ResourceBundle.getBundle("mplayer4anime.localization.locale", new Locale("en"));
        }
        loaderSettings.setResources(resourceBundle);


        try {
            Parent parentAbout = loaderSettings.load();
            //SettingsController settingsController = loaderSettings.getController();

            stageAbout.setTitle(resourceBundle.getString("settings_SettingsName"));
            stageAbout.getIcons().addAll(
                    new Image(Main.class.getResourceAsStream("/mplayer4anime/res/settings_icon32x32.png")),
                    new Image(Main.class.getResourceAsStream("/mplayer4anime/res/settings_icon48x48.png")),
                    new Image(Main.class.getResourceAsStream("/mplayer4anime/res/settings_icon64x64.png")),
                    new Image(Main.class.getResourceAsStream("/mplayer4anime/res/settings_icon128x128.png"))
            );       // TODO: change to something reliable
            stageAbout.setScene(new Scene(parentAbout, 570, 500));
            stageAbout.show();

        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
