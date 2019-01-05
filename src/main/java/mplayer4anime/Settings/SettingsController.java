package mplayer4anime.Settings;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import mplayer4anime.AppPreferences;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import mplayer4anime.MediatorControl;

public class SettingsController implements Initializable {

    private AppPreferences appPreferences;
    @FXML
    private ControllerListsSelector subExtensionListController;
    @FXML
    private ControllerListsSelector subEncodingListController;
    @FXML
    private ControllerListsSelector videoExtensionListController;
    @FXML
    private ControllerListsSelector audioExtensionListController;
    @FXML
    private Label pathToMplayerLbl;
    @FXML
    private CheckBox subtitlesFirstCheckBox;

    @Override
    public void initialize(URL url, ResourceBundle resBundle) {
        appPreferences = new AppPreferences();
        pathToMplayerLbl.setText(appPreferences.getPath());

        // Subtitles should be shown first? If TRUE, then set checkbox.
        subtitlesFirstCheckBox.setSelected(appPreferences.getSubtilesFirst());
        // Fill lists of extensions and encodings
        subExtensionListController.setList(appPreferences.getSubsExtensionsList(), true);
        subEncodingListController.setList(appPreferences.getSubsEncodingList(), false);
        videoExtensionListController.setList(appPreferences.getVideoExtensionsList(), true);
        audioExtensionListController.setList(appPreferences.getAudioExtensionsList(), true);
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
    private void clearPath(){
        if (System.getProperty("os.name").contains("Windows"))
            pathToMplayerLbl.setText("mplayer\\mplayer.exe");
        else
            pathToMplayerLbl.setText("mplayer");
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
        appPreferences.setSubtilesFirst(subtitlesFirstCheckBox.isSelected());
        appPreferences.setSubsExtensionsList(subExtensionListController.getList());
        appPreferences.setSubsEncodingList(subEncodingListController.getList());
        appPreferences.setVideoExtensionsList(videoExtensionListController.getList());
        appPreferences.setAudioExtensionsList(audioExtensionListController.getList());

        MediatorControl.getInstance().sentUpdates();    // TODO: implement list to track what should be updated

        thisStage.close();
    }
}
