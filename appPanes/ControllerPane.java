package mplayer4anime.appPanes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public abstract class ControllerPane  implements Initializable {

    private ResourceBundle resourceBundle;            // TODO: consider access thorough mediator
    // use folderToOpen same variable in all panes
    private static String folderToOpen;

    @FXML
    public ListView<File> paneListView;
    public ObservableList<File> paneFileList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resBundle) {
        SetCellFactory(paneListView);
        resourceBundle = resBundle;
    }

    private void SetCellFactory(ListView<File> lv) {
        lv.setCellFactory(new Callback<ListView<File>, ListCell<File>>() {
            @Override
            public ListCell<File> call(ListView<File> fileListView) {
                return new ListCell<File>(){
                    @Override
                    public void updateItem(File item, boolean empty){
                        // have to call super here
                        super.updateItem(item, empty);

                        String trimmedName;

                        if (item == null || empty){
                            setText(null);
                        }
                        else {
                            trimmedName = item.getName();
                            setText(trimmedName);
                        }
                    }
                };
            }
        });
    }

    protected void openFileChooser (String key){
        File directoryReceived;      // Store files (folder) received from selector
        DirectoryChooser dirSelect;

        // Show directory selector
        dirSelect = new DirectoryChooser();
        dirSelect.setTitle(resourceBundle.getString("SelectDirectoryTitle"));
        if (folderToOpen == null)
            dirSelect.setInitialDirectory(new File(System.getProperty("user.home")));
        else
            dirSelect.setInitialDirectory(new File(folderToOpen));
        directoryReceived = dirSelect.showDialog(null);                 // TODO: Clarify how the fuck is it works

        // GET LIST OF MKV/MKA FILES within directory
        if (directoryReceived != null) {
            getFilesFromFolder(directoryReceived, key);
        } else {
            System.out.println("\tNo folder selected");
        }
    }

    public void getFilesFromFolder(File directoryReceived, String key){
        File[] files;                // Store files mkv/mka

        files = directoryReceived.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String Name) {
                if (Name.lastIndexOf('.') > 0) {
                    int lastindex = Name.lastIndexOf('.');
                    String ext = Name.substring(lastindex);
                    if (ext.equals(key))
                        return true;
                } else
                    return false;
                return false;
            }
        });

        if (files != null && files.length > 0) {
            // spiced java magic
            Arrays.sort(files);
            // DEBUG START
            for (File eachFile : files)
                System.out.println(eachFile.getAbsoluteFile());
            // DEBUG END

            // Remember the folder used for MKV and reuse it when user opens MKA/subs folder (as new default path instead of user.home)
            folderToOpen = files[0].getParent();
            System.out.println(folderToOpen);

                paneListView.getItems().clear(); // wipe elements from ListView
                paneFileList.addAll(files);
                paneListView.setItems(paneFileList);
                paneListView.getSelectionModel().select(0);

        } else {
            System.out.println("\tNo files in this folder");
        }
    }
    @FXML
    private void Up(){
        int index;
        index = paneListView.getSelectionModel().getSelectedIndex();
        if (index >0){
            paneFileList.add(index-1, paneListView.getSelectionModel().getSelectedItem());
            paneFileList.remove(index+1);
            paneListView.getSelectionModel().select(index-1);
        }
    }
    @FXML
    private void Down(){
        int index;
        index = paneListView.getSelectionModel().getSelectedIndex();
        if (index+1 < paneFileList.size() ){
            paneFileList.add(index+2, paneListView.getSelectionModel().getSelectedItem());
            paneFileList.remove(index);
            paneListView.getSelectionModel().select(index+1);
        }
    }

    @FXML
    private void Del(){ paneFileList.remove(paneListView.getSelectionModel().getSelectedItem()); }

    @FXML
    private void KeyPressed(KeyEvent event){
        if (event.getCode().toString().equals("DELETE"))
            Del();
    }
    // TO IMPLEMENT IN REAL CLASS
    @FXML
    protected abstract void openAction();
}
