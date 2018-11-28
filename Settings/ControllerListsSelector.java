package mplayer4anime.Settings;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import mplayer4anime.ServiceWindow;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class ControllerListsSelector implements Initializable {
    @FXML
    private ListView<String> listView;
    @FXML
    private TextField newRecordText;
    private ObservableList<String> observableList;
    private ResourceBundle resourceBundle;

    private boolean listOfExtensions;       // Handle validation of the format importing items

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        resourceBundle = rb;
    }
    /**
     * Must be run on start
     * Set list content
    */
     void setList(String[] listFromStorage, boolean isListOfExtensions){
        observableList = FXCollections.observableArrayList(listFromStorage);
        listView.setItems(observableList);
        this.listOfExtensions = isListOfExtensions;
    }
    /**
     * Return list content
     * */
    String[] getList(){
        return Arrays.copyOf(observableList.toArray(), observableList.toArray().length, String[].class);
    }
    @FXML
    private void listKeyPressed(KeyEvent event){
        if (event.getCode().toString().equals("DELETE"))
            removeRecord();
    }
    @FXML
    private void upRecord(){
        int index;
        index = listView.getSelectionModel().getSelectedIndex();
        if (index >0){
            observableList.add(index-1, listView.getSelectionModel().getSelectedItem());
            observableList.remove(index+1);
            listView.getSelectionModel().select(index-1);
        }
    }
    @FXML
    private void downRecord(){
        int index;
        index = listView.getSelectionModel().getSelectedIndex();
        if (index+1 < observableList.size() ){
            observableList.add(index+2, listView.getSelectionModel().getSelectedItem());
            observableList.remove(index);
            listView.getSelectionModel().select(index+1);
        }
    }
    @FXML
    private void removeRecord(){ observableList.remove(listView.getSelectionModel().getSelectedItem()); }
    @FXML
    private void addNewRecord(){
        String addingItem = newRecordText.getText().trim();
        if (!addingItem.isEmpty())                   // If this field is non-empty
            if (!addingItem.contains(" ") && !addingItem.contains("\t")) {
                if (this.listOfExtensions) {
                    if (addingItem.startsWith("*."))
                        if (addingItem.substring(2).contains("*")) {
                            ServiceWindow.getErrorNotification(resourceBundle.getString("Error"), resourceBundle.getString("settings_fieldContainUnacceptedChars"));
                        } else {
                            observableList.add(addingItem);
                        }
                    else if (addingItem.contains("*")) {
                        ServiceWindow.getErrorNotification(resourceBundle.getString("Error"), resourceBundle.getString("settings_fieldContainUnacceptedChars"));
                    } else if (addingItem.startsWith(".")) {
                        observableList.add("*" + addingItem);
                    } else {
                        observableList.add("*." + addingItem);
                    }
                }
                else {
                    observableList.add(addingItem);
                }
            }
            else{
                ServiceWindow.getErrorNotification(resourceBundle.getString("Error"), resourceBundle.getString("settings_fieldContainUnacceptedChars"));
            }
        newRecordText.clear();
    }
}
