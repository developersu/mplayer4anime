package mplayer4anime.Settings;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyEvent;

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

    private boolean isListOfExtensions;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        resourceBundle = rb;
    }
    /**
     * Must be run on start
     * Set list content
    */
    void setList(String[] listFromStorage, boolean isListOfExtensions){
        this.isListOfExtensions = isListOfExtensions;
        observableList = FXCollections.observableArrayList(listFromStorage);
        listView.setItems(observableList);
        if (isListOfExtensions) {
            newRecordText.setText("*.");
            newRecordText.setTextFormatter(new TextFormatter<Object>(change -> {
                if (change.getControlNewText().matches("(^\\*\\.[A-Za-z0-9]$)|(^\\*\\.[A-Za-z0-9][A-Za-z0-9\\.]+?$)|(^\\*\\.$)"))
                    return change;
                return null;
            }));
        }
        else {
            newRecordText.setTextFormatter(new TextFormatter<Object>(change -> {
                if (change.getControlNewText().matches("(^[A-Za-z0-9\\-]+?$)|(^$)"))
                    return change;
                return null;
            }));
        }

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
        String addingItem = newRecordText.getText().toLowerCase();
        if (!addingItem.isEmpty()) {                   // If this field is non-empty
            if (!observableList.contains(addingItem))
                observableList.add(addingItem);
        }
        if (isListOfExtensions)
            newRecordText.setText("*.");
        else
            newRecordText.clear();
    }
}