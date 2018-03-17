package sample.About;

import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AboutWindow {

    public AboutWindow(HostServices hostServices){
        Stage stageAbout = new Stage();

        stageAbout.setMinWidth(415);
        stageAbout.setMinHeight(220);

        FXMLLoader loaderAbout = new FXMLLoader(getClass().getResource("AboutLayout.fxml"));

        try {
            Parent parentAbout = loaderAbout.load();
            AboutController abtController = loaderAbout.getController();
            abtController.setHostServices(hostServices);

            stageAbout.setScene(new Scene(parentAbout, 415, 220));

            stageAbout.show();

        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
