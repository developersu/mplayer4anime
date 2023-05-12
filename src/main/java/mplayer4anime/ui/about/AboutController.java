package mplayer4anime.ui.about;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;


public class AboutController implements Initializable {

    private HostServices hostServices;

    void setHostServices(HostServices hs){
        this.hostServices = hs;
    }

    @FXML
    private Button buttonOk;
    @FXML
    private TextArea GSONLicense;
    @FXML
    private Label versionLbl;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        versionLbl.setText(ResourceBundle.getBundle("app").getString("_version"));

        GSONLicense.setText("Copyright 2008 Google Inc.\n" +
                "\n" +
                "Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                "you may not use this file except in compliance with the License.\n" +
                "You may obtain a copy of the License at\n" +
                "\n" +
                "    http://www.apache.org/licenses/LICENSE-2.0\n" +
                "\n" +
                "Unless required by applicable law or agreed to in writing, software\n" +
                "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                "See the License for the specific language governing permissions and\n" +
                "limitations under the License.");
    }

    @FXML
    private void buttonClickOk(){
        Stage thisStage = (Stage) buttonOk.getScene().getWindow();
        thisStage.close();
    }
    @FXML
    private void gitHubUrl(){
        try {
            hostServices.showDocument("https://github.com/developersu/mplayer4anime");
        } catch (Exception ignored){ }
    }
    @FXML
    private void bloggerUrl(){
        try {
            hostServices.showDocument("https://developersu.blogspot.com/search/label/mplayer4anime");
        } catch (Exception ignored){ }
    }
    @FXML
    private void libGSON(){
        try {
            hostServices.showDocument("https://github.com/google/gson");
        } catch (Exception ignored){ }
    }
    @FXML
    private void iconsMaterial(){
        try {
            hostServices.showDocument("https://materialdesignicons.com/");
        } catch (Exception ignored){ }
    }
}
