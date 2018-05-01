package sample.Settings;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sample.AppPreferences;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import sample.MediatorControl;

public class SettingsController implements Initializable {

    private ResourceBundle resourceBundle;
    // Class of settings used
    private AppPreferences appPreferences;

    @FXML
    private Label pathToMplayerLbl,
                  unixOsInfoLbl;

    @FXML
    private CheckBox subtitlesFirstCheckBox,
                     listsLoadOnStartCheckBox;

    // subs extensions and codepages error handle
    private void showFormattingError(){
        Alert alertBox = new Alert(Alert.AlertType.ERROR);
        alertBox.setTitle(resourceBundle.getString("Error"));
        alertBox.setHeaderText(null);
        alertBox.setContentText(resourceBundle.getString("settings_fieldContainSpacesTabs"));
        alertBox.show();
    }
    // subs extensions
    @FXML
    private ListView<String> subsExtListView;
    private ObservableList<String> subsExtObservableList;
    @FXML
    private TextField subsExtNewRecordText;
    @FXML
    private void subsExtAddNewRecord(){
        String add = subsExtNewRecordText.getText().trim();
        if (!add.isEmpty())                   // If this field is non-empty
            if (!add.contains(" ") && !add.contains("\t"))
                subsExtObservableList.add(subsExtNewRecordText.getText().trim());
            else{
                showFormattingError();
            }
        subsExtNewRecordText.clear();
    }
    @FXML
    private void subsExtListKeyPressed(KeyEvent event){
        if (event.getCode().toString().equals("DELETE"))
            subsExtRemoveRecord();
    }
    @FXML
    private void subsExtRemoveRecord(){ subsExtObservableList.remove(subsExtListView.getSelectionModel().getSelectedItem()); }
    @FXML
    private void subsExtUpRecord(){
        int index;
        index = subsExtListView.getSelectionModel().getSelectedIndex();
        if (index >0){
            subsExtObservableList.add(index-1, subsExtListView.getSelectionModel().getSelectedItem());
            subsExtObservableList.remove(index+1);
            subsExtListView.getSelectionModel().select(index-1);
        }
    }
    @FXML
    private void subsExtDownRecord(){
        int index;
        index = subsExtListView.getSelectionModel().getSelectedIndex();
        if (index+1 < subsExtObservableList.size() ){
            subsExtObservableList.add(index+2, subsExtListView.getSelectionModel().getSelectedItem());
            subsExtObservableList.remove(index);
            subsExtListView.getSelectionModel().select(index+1);
        }
    }

    // subs codepage
    @FXML
    private ListView<String> subsCodepageListView;
    private ObservableList<String> subsCodepageObservableList;
    @FXML
    private TextField subsCodepageNewRecordText;
    @FXML
    private void subsCodepageAddNewRecord(){
        String add = subsCodepageNewRecordText.getText().trim();
        if (!add.isEmpty())                   // If this field is non-empty
            if (!add.contains(" ") && !add.contains("\t"))
                subsCodepageObservableList.add(subsCodepageNewRecordText.getText().trim());
            else{
                showFormattingError();
            }
        subsCodepageNewRecordText.clear();
    }
    @FXML
    private void subsCodepageListKeyPressed(KeyEvent event){
        if (event.getCode().toString().equals("DELETE"))
            subsCodepageRemoveRecord();
    }
    @FXML
    private void subsCodepageRemoveRecord(){ subsCodepageObservableList.remove(subsCodepageListView.getSelectionModel().getSelectedItem()); }
    @FXML
    private void subsCodepageUpRecord(){
        int index;
        index = subsCodepageListView.getSelectionModel().getSelectedIndex();
        if (index >0){
            subsCodepageObservableList.add(index-1, subsCodepageListView.getSelectionModel().getSelectedItem());
            subsCodepageObservableList.remove(index+1);
            subsCodepageListView.getSelectionModel().select(index-1);
        }
    }
    @FXML
    private void subsCodepageDownRecord(){
        int index;
        index = subsCodepageListView.getSelectionModel().getSelectedIndex();
        if (index+1 < subsCodepageObservableList.size() ){
            subsCodepageObservableList.add(index+2, subsCodepageListView.getSelectionModel().getSelectedItem());
            subsCodepageObservableList.remove(index);
            subsCodepageListView.getSelectionModel().select(index+1);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resBundle) {
        resourceBundle = resBundle;
        appPreferences = new AppPreferences();
        pathToMplayerLbl.setText(appPreferences.getPath());
        if (System.getProperty("os.name").contains("Windows")) {
            unixOsInfoLbl.setVisible(false);
        }
        // Subtitles should be shown first? If TRUE, then set checkbox.
        subtitlesFirstCheckBox.setSelected(appPreferences.getSubtilesFirst());
        // Should application restore lists after startup?
        listsLoadOnStartCheckBox.setSelected(appPreferences.getLoadListsOnStart());
        //---------------------------------------------------------
        // Populate list of avaliable subtitles extensions
        subsExtObservableList = FXCollections.observableArrayList(appPreferences.getSubsExtensionsList());
        subsExtListView.setItems(subsExtObservableList);
        //---------------------------------------------------------
        // Populate list of avaliable codepages
        subsCodepageObservableList = FXCollections.observableArrayList(appPreferences.getSubsCodepageList());
        subsCodepageListView.setItems(subsCodepageObservableList);
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
        appPreferences.setLoadListsOnStart(listsLoadOnStartCheckBox.isSelected());
        appPreferences.setSubsExtensionsList(Arrays.copyOf(subsExtObservableList.toArray(), subsExtObservableList.toArray().length, String[].class));
        appPreferences.setSubsCodepageList(Arrays.copyOf(subsCodepageObservableList.toArray(), subsCodepageObservableList.toArray().length, String[].class));

        MediatorControl.getInstance().sentUpdates();    // TODO: implement list to track what should be updated

        thisStage.close();
    }
}
