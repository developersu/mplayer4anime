package mplayer4anime.appPanes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import mplayer4anime.AppPreferences;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class ControllerPane  implements Initializable {

    private ResourceBundle resourceBundle;
    // use folderToOpen same variable in all panes
    private static String folderToOpen;

    private AppPreferences appPreferences;

    @FXML
    private ListView<File> paneListView;
    private ObservableList<File> paneFileList = FXCollections.observableArrayList();

    @FXML
    private Label paneLbl;
    private String paneType;


    @Override
    public void initialize(URL url, ResourceBundle resBundle) {
        SetCellFactory(paneListView);
        resourceBundle = resBundle;
        appPreferences = new AppPreferences();
    }
    public void setPaneType(String paneType){
        this.paneType = paneType;

        switch (paneType) {
            case "Video":
                paneLbl.setText(resourceBundle.getString("lbl_VideoPane"));
                break;
            case "Audio":
                paneLbl.setText(resourceBundle.getString("lbl_AudioPane"));
                break;
            case "Subtitles":
                paneLbl.setText(resourceBundle.getString("lbl_SubsPane"));
                break;
            default:
                paneLbl.setText(resourceBundle.getString("?"));
                break;
        }
    }

    /** Get index of the selected in pane element */
    public int getElementSelectedIndex(){
        return this.paneListView.getSelectionModel().getSelectedIndex();
    }
    /** Select element name (full path) using index recieved */
    public String getElementSelected(){
        if (this.paneListView.getSelectionModel().getSelectedItem() != null) {
            return this.paneFileList.get(this.getElementSelectedIndex()).toPath().toString();
        }
        else {
            return null;
        }
    }
    /** Select element in pane using index recieved */
    public void setElementSelectedByIndex(int index){
        this.paneListView.getSelectionModel().select(index);
    }
    /** Get number of elements loaded into the pane */
    public int getElementsCount(){
        return this.paneFileList.size();
    }
    /** Check if there are any elements loaded */
    public boolean isElementsListEmpty(){
        return paneFileList.isEmpty();
    }
    /** Get all elements
     * Used in Json playlist writer only */
    public String[] getElementsAll(){
        String[] elementsArray = new String[this.getElementsCount()];
        for (int i = 0; i < elementsArray.length; i++){
            elementsArray[i] = paneFileList.get(i).toString();
        }
        return  elementsArray;
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
    /**
     * Open file selector (Open folder button in UI).
     * */
    @FXML
    void openDirChooser(){
        String[] filesExtension;
        switch (paneType) {
            case "Video":
                filesExtension = appPreferences.getVideoExtensionsList();
                break;
            case "Audio":
                filesExtension = appPreferences.getAudioExtensionsList();
                break;
            case "Subtitles":
                filesExtension = appPreferences.getSubsExtensionsList();
                break;
            default:
                filesExtension = new String[]{"*"};
                break;
        }

        File directoryReceived;      // Store files (folder) received from selector
        DirectoryChooser dirSelect;

        // Show directory selector
        dirSelect = new DirectoryChooser();
        dirSelect.setTitle(resourceBundle.getString("SelectDirectoryTitle"));
        if (folderToOpen == null)
            dirSelect.setInitialDirectory(new File(System.getProperty("user.home")));
        else
            dirSelect.setInitialDirectory(new File(folderToOpen));
        directoryReceived = dirSelect.showDialog(paneListView.getScene().getWindow());

        // GET LIST OF FILES from directory
        if (directoryReceived != null) {
            File[] files;                // Store files mkv/mka

            files = directoryReceived.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String Name) {
                    if (Name.lastIndexOf('.') > 0) {
                        int lastindex = Name.lastIndexOf('.');
                        String ext = Name.substring(lastindex);
                        for (String key : filesExtension){
                            if (ext.equals(key.substring(1)))
                                return true;
                        }
                    } else
                        return false;
                    return false;
                }
            });

            displayFiles(files);
        } else {
            System.out.println("No folder selected");
        }
    }
    /**
     * Open file selector (Open files button in UI).
     * */
    @FXML
    void openFilesChooser(){
        String[] filesExtension;
        switch (paneType) {
            case "Video":
                filesExtension = appPreferences.getVideoExtensionsList();
                break;
            case "Audio":
                filesExtension = appPreferences.getAudioExtensionsList();
                break;
            case "Subtitles":
                filesExtension = appPreferences.getSubsExtensionsList();
                break;
            default:
                filesExtension = new String[]{"*"};
                break;
        }

        List<File> filesRecievedList;

        FileChooser fc = new FileChooser();
        fc.setTitle(resourceBundle.getString("SelectFile"));
        if (folderToOpen == null)
            fc.setInitialDirectory(new File(System.getProperty("user.home")));
        else
            fc.setInitialDirectory(new File(folderToOpen));
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(paneType, filesExtension));

        filesRecievedList = fc.showOpenMultipleDialog(paneListView.getScene().getWindow());
        if (filesRecievedList != null){
            File[] filesRecieved = new File[filesRecievedList.size()];
            filesRecievedList.toArray(filesRecieved);
            displayFiles(filesRecieved);
        } else {
            System.out.println("No files selected");
        }
    }
    /**
     * Set files using lists. Used if playlist loaded
     * */
    public void setFilesFromList(String[] fileLocations){
        if (fileLocations != null && fileLocations.length != 0) {
            File[] files = new File[fileLocations.length];
            for (int i=0; i < fileLocations.length; i++)
                files[i] = new File(fileLocations[i]);
            displayFiles(files);
        }
    }

    private void displayFiles(File[] files){
        if (files != null && files.length > 0) {
            // spiced java magic
            Arrays.sort(files);

            // Remember the folder used for MKV and reuse it when user opens MKA/subs folder (as new default path instead of user.home)
            folderToOpen = files[0].getParent();
            //System.out.println(folderToOpen);

                paneFileList.addAll(files);
                paneListView.setItems(paneFileList);
                paneListView.getSelectionModel().select(0);

        } else {
            System.out.println("\tNo files in this folder");
        }
    }
    @FXML
    public void cleanList(){
        paneListView.getItems().clear(); // wipe elements from ListView
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
}