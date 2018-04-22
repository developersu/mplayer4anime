package sample.About;

import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import sample.Main;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class AboutWindow {

    public AboutWindow(HostServices hostServices){
        Stage stageAbout = new Stage();

        stageAbout.setMinWidth(415);
        stageAbout.setMinHeight(220);

        FXMLLoader loaderAbout = new FXMLLoader(getClass().getResource("AboutLayout.fxml"));
        ResourceBundle resourceBundle;

        if (Locale.getDefault().getISO3Language().equals("rus")) {
            resourceBundle = ResourceBundle.getBundle("sample.localization.locale", new Locale("ru"));
        } else {
            resourceBundle = ResourceBundle.getBundle("sample.localization.locale", new Locale("en"));
        }
        loaderAbout.setResources(resourceBundle);

        try {
            Parent parentAbout = loaderAbout.load();
            AboutController abtController = loaderAbout.getController();
            abtController.setHostServices(hostServices);

            stageAbout.setTitle(resourceBundle.getString("AboutName"));
            stageAbout.getIcons().add(new Image(Main.class.getResourceAsStream("/sample/res/app.png"))); // TODO: change to something reliable
            stageAbout.setScene(new Scene(parentAbout, 415, 220));

            stageAbout.show();

        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
