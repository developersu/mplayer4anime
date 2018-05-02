package mplayer4anime.About;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;


public class AboutController {

    private HostServices hostServices;

    public void setHostServices(HostServices hs){
        this.hostServices = hs;
    }

    @FXML
    private Button buttonOk;

    @FXML
    private void buttonClickOk(){
        Stage thisStage = (Stage) buttonOk.getScene().getWindow();
        thisStage.close();
    }
    @FXML
    private void gitHubUrl(){
        try {
            hostServices.showDocument("https://github.com/developersu/mplayer4anime");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    @FXML
    private void bloggerUrl(){
        try {
            hostServices.showDocument("https://developersu.blogspot.com/");
        } catch (Exception e){
            e.printStackTrace();
        }
    }


}
