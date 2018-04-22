package sample.Settings;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sample.AppPreferences;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    @FXML
    private Label pathToMplayerLbl;

    @FXML
    private Label unixOsInfoLbl;
    // Class of settings used
    private AppPreferences appPreferences;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        appPreferences = new AppPreferences();
        pathToMplayerLbl.setText(appPreferences.getPath());
        if (System.getProperty("os.name").contains("Windows")) {
            unixOsInfoLbl.setVisible(false);
        }
    }

    @FXML
    private void findPathToMplayer(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("mplayer");

        // In case we use Windows, limit selectable file to .exe
        if (System.getProperty("os.name").contains("Windows")) {
            fileChooser.getExtensionFilters().setAll(
                    new FileChooser.ExtensionFilter("mplayer", "*.exe")
            );
        }

        File mplayerExecutableFile = fileChooser.showOpenDialog(null);

        if (mplayerExecutableFile != null) {
            pathToMplayerLbl.setText(mplayerExecutableFile.toString());
        }
    }
    @FXML
    private void Cancel(){
        Stage thisStage = (Stage) pathToMplayerLbl.getScene().getWindow();  // TODO: consider refactoring. Non-urgent.
        thisStage.close();
    }

    @FXML
    private void Save(){
        Stage thisStage = (Stage) pathToMplayerLbl.getScene().getWindow();  // TODO: consider refactoring. Non-urgent.
        appPreferences.setPath(pathToMplayerLbl.getText());
        thisStage.close();
    }

    @FXML
    private void clearPath(){
        pathToMplayerLbl.setText("mplayer");
    }

}
