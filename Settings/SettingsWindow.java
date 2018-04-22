package sample.Settings;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import sample.Main;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class SettingsWindow {

    public SettingsWindow(){
        Stage stageAbout = new Stage();

        stageAbout.setMinWidth(570);
        stageAbout.setMinHeight(150);

        FXMLLoader loaderSettings = new FXMLLoader(getClass().getResource("SettingsLayout.fxml"));
        ResourceBundle resourceBundle;

        if (Locale.getDefault().getISO3Language().equals("rus")) {
            resourceBundle = ResourceBundle.getBundle("sample.localization.locale", new Locale("ru"));
        } else {
            resourceBundle = ResourceBundle.getBundle("sample.localization.locale", new Locale("en"));
        }
        loaderSettings.setResources(resourceBundle);

        try {
            Parent parentAbout = loaderSettings.load();
            //SettingsController settingsController = loaderSettings.getController();

            stageAbout.setTitle(resourceBundle.getString("SettingsName"));
            stageAbout.getIcons().add(new Image(Main.class.getResourceAsStream("/sample/res/app.png"))); // TODO: change to something reliable
            stageAbout.setScene(new Scene(parentAbout, 570, 150));

            stageAbout.show();

        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
