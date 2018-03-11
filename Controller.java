package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import java.io.*;
import java.util.Arrays;

public class Controller {

    @FXML
    private ListView<File> mkvListView;
    @FXML
    private ListView<File> mkaListView;

    @FXML
    private CheckBox fullScreen;

    private ObservableList<File> mkvFileList = FXCollections.observableArrayList();

    private ObservableList<File> mkaFileList = FXCollections.observableArrayList();

    public Stage thisStage;

/*    public Controller(Stage stage){
        thisStage = stage;
        System.out.println("executed");
    }
*/

    private void SetCellFactory (ListView<File> lv) {
        lv.setCellFactory(new Callback<ListView<File>, ListCell<File>>() {
            @Override
            public ListCell<File> call(ListView<File> fileListView) {
                return new ListCell<File>(){
                    @Override
                    public void updateItem(File item, boolean empty){
                        // have to call super here
                        super.updateItem(item, empty);

                        String trimmedName;

                        if (item != null){
                            trimmedName = item.getName();
                            setText(trimmedName);
                        }
                    }
                };
            }
        });
    }

    @FXML
    public void initialize() {
        // Define CallFactory to handle MKV+MKA ListView
   //     SetCellFactory(mkvListView);
   //     SetCellFactory(mkaListView);
    }

    public void mkvOpenAction() {
        fileFiltering(".mkv");
    }

    public void mkaOpenAction() {
        fileFiltering(".mka");
    }

    private void fileFiltering (String key){
        File directoryReceived;      // Store files (folder) received from selector
        File[] files;                // Store files mkv/mka
        DirectoryChooser dirSelect;

        // Show directory selector
        dirSelect = new DirectoryChooser();
        dirSelect.setTitle("Select directory");
        dirSelect.setInitialDirectory(new File(System.getProperty("user.home")));
        directoryReceived = dirSelect.showDialog(thisStage);

        // GET LIST OF MKV/MKA FILES within directory
        if (directoryReceived != null) {
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

            if (files != null) {
                // spiced java magic
                Arrays.sort(files);
                // DEBUG START
                for (File eachFile : files)
                    System.out.println(eachFile.getAbsoluteFile() + " " + eachFile.getName());
                // DEBUG END

                if (key.equals(".mkv")) {
                    mkvListView.getItems().clear(); // wipe elements from ListView
                    mkvFileList.addAll(files);
                    mkvListView.setItems(mkvFileList);
                    mkvListView.getSelectionModel().select(0);
                }
                else if (key.equals(".mka")) {
                    mkaListView.getItems().clear(); // wipe elements from ListView
                    mkaFileList.addAll(files);
                    mkaListView.setItems(mkaFileList);
                    mkaListView.getSelectionModel().select(0);
                }

            } else {
                System.out.println("-- No files in this folder --");
            }
        } else {
            System.out.println("-- No folder selected --");
        }
    }

    @FXML
    private void vUp(){
        int index;
        index = mkvListView.getSelectionModel().getSelectedIndex();
        if (index >0){
            mkvFileList.add(index-1, mkvListView.getSelectionModel().getSelectedItem());
            mkvFileList.remove(index+1);
            mkvListView.getSelectionModel().select(index-1);
        }
    }
    @FXML
    private void vDown(){
        int index;
        index = mkvListView.getSelectionModel().getSelectedIndex();
        if (index+1 < mkvFileList.size() ){
            mkvFileList.add(index+2, mkvListView.getSelectionModel().getSelectedItem());
            mkvFileList.remove(index);
            mkvListView.getSelectionModel().select(index+1);
        }
    }
    @FXML
    private void vDel(){ mkvFileList.remove(mkvListView.getSelectionModel().getSelectedItem()); }
    @FXML
    private void aUp() {
        int index;
        index = mkaListView.getSelectionModel().getSelectedIndex();
        if (index > 0) {
            mkaFileList.add(index - 1, mkaListView.getSelectionModel().getSelectedItem());
            mkaFileList.remove(index + 1);
            mkaListView.getSelectionModel().select(index - 1);
        }
    }
    @FXML
    private void aDown(){
        int index;
        index = mkaListView.getSelectionModel().getSelectedIndex();
        if (index+1 < mkaFileList.size() ){
            mkaFileList.add(index+2, mkaListView.getSelectionModel().getSelectedItem());
            mkaFileList.remove(index);
            mkaListView.getSelectionModel().select(index+1);
        }
    }
    @FXML
    private void aDel(){ mkaFileList.remove(mkaListView.getSelectionModel().getSelectedItem()); }
    @FXML
    private void play(){
        if (mkvFileList.get(mkvListView.getSelectionModel().getSelectedIndex()).toPath().toString() != null)
            if (mkaFileList.get(mkvListView.getSelectionModel().getSelectedIndex()).toPath().toString() != null) {
                try {
                    Process mplayer = new ProcessBuilder("mplayer",
                            fullScreen.isSelected()?"-fs":"",
                            mkvFileList.get(mkvListView.getSelectionModel().getSelectedIndex()).toPath().toString(),
                            "-audiofile",
                            mkaFileList.get(mkvListView.getSelectionModel().getSelectedIndex()).toPath().toString()).start();
                    InputStream inputStream = mplayer.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferReader = new BufferedReader(inputStreamReader);
                    String lines;

                    while ((lines = bufferReader.readLine()) != null)
                        System.out.println(lines);

                } catch(IOException e){
                e.printStackTrace();
            }
        }
        else
            System.out.println("FAILED");
    }

}