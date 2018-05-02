package mplayer4anime.appPanes;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;

public class ControllerSUB extends ControllerPane {
    @FXML
    public ChoiceBox<String> subtExt;
    // Observable list of the content subtExt
    public ObservableList<String> subtExtList;

    @FXML
    public ChoiceBox<String> subtCodepage;
    // Observable list of the content subtCodepage
    public ObservableList<String> subtCodepageList;

    @Override
    protected void openAction() { openFileChooser(subtExt.getValue()); } // TODO: check if non-empty and show error if needed
}
