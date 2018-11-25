package mplayer4anime.appPanes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import mplayer4anime.AppPreferences;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class ControllerSUB extends ControllerPane {
    // TODO: Move file extension filtering options to the OS-selector and remove drop-down with file ext. filtering options
    @FXML
    private ChoiceBox<String> subtExt;
    // Observable list of the content subtExt
    private ObservableList<String> subtExtList;

    @FXML
    private ChoiceBox<String> subtEncoding;
    // Observable list of the content subtEncoding
    private ObservableList<String> subEncodingList;

    public void initialize(URL url, ResourceBundle rb) {
        super.initialize(url, rb);
        this.subtExtList = FXCollections.observableArrayList();
        this.subEncodingList = FXCollections.observableArrayList();
    }
    @Override
    protected void openAction() { openFileChooser(subtExt.getValue());} // TODO: check if non-empty and show error if needed
    /** Return selected file extension */
    // Walking on the thin ice..
    public String getSelectedExt(){
        return subtExt.getSelectionModel().getSelectedItem();
    }
    /** Return selected file encoding */
    public String getSelectedEncoding(){
        return subtEncoding.getSelectionModel().getSelectedItem();
    }
    /**
     * Set Files extensions list and select element
     * */
    public void setSubtExt(String[] list, String selection){
        setMenuElements(subtExt, subtExtList, list, selection);
    }
    /**
     * Set encoding list and select element
     * */
    public void setEncoding(String[] list, String selection){
        setMenuElements(subtEncoding, subEncodingList, list, selection);
    }
    /* Encoding and Extension common setter */
    private void setMenuElements(ChoiceBox<String> menu, ObservableList<String> obsList, String[] list, String selection){
        String currentlySelectedValue = menu.getValue();

        obsList.clear();
        obsList.setAll(list);
        menu.setItems(obsList);

        if (selection == null || selection.isEmpty())
            // Try to restore previously selected value if it existed before and exists in the new list
            if (currentlySelectedValue !=null && !currentlySelectedValue.isEmpty() && obsList.contains(currentlySelectedValue))
                menu.getSelectionModel().select(currentlySelectedValue);
            else
                menu.setValue(obsList.get(0));
        else {
            if (obsList.contains(selection))
                menu.getSelectionModel().select(selection);
            else
                menu.setValue(obsList.get(0));
        }
    }
    /**
     * Select encoding and adds it into the drop-down if it's not in the list
     * Updates stored lists/selection of encoding
     * */
    public void selectEncodingValue(String encodingValue, AppPreferences preferences){
        if (encodingValue != null && !encodingValue.isEmpty()) {
            if (!subEncodingList.contains(encodingValue)) {
                subEncodingList.add(encodingValue);
                preferences.setSubsEncodingList(Arrays.copyOf(subEncodingList.toArray(), subEncodingList.toArray().length, String[].class));
            }
            if (!subtEncoding.getValue().equals(encodingValue)) {
                subtEncoding.getSelectionModel().select(encodingValue);
                preferences.setLastTimeUsedSubsEncoding(encodingValue);
            }
        }
    }
}