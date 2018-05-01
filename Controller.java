package sample;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.*;
import javafx.util.Callback;
import sample.About.AboutWindow;
import sample.Settings.SettingsWindow;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    // Class of settings used
    private AppPreferences appPreferences;

    private ResourceBundle resourceBundle;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Register this controller in mediator
        MediatorControl.getInstance().registerMainController(this);

        resourceBundle = rb;
        //System.out.println(resourceBundle.getLocale().toString());
        //System.out.println(resourceBundle.getKeys());

        // Get preferences
        appPreferences = new AppPreferences();

        // Set default extension of the subtitles files:
        subtExtList = FXCollections.observableArrayList( appPreferences.getSubsExtensionsList() ); // Receive list from storage
        subtExt.setItems(subtExtList);
        if (appPreferences.getLastTimeUsedSubsExt().isEmpty())      // not sure that it's possible
            subtExt.setValue(subtExtList.get(0));
        else
            subtExt.setValue(appPreferences.getLastTimeUsedSubsExt());

        // Set default list of codepages of the subtitles files:
        subtCodepageList = FXCollections.observableArrayList(appPreferences.getSubsCodepageList());
        subtCodepage.setItems(subtCodepageList);
        if (appPreferences.getLastTimeUsedSubsCodepage().isEmpty())
            subtCodepage.setValue(subtCodepageList.get(0));
        else
            subtCodepage.setValue(appPreferences.getLastTimeUsedSubsCodepage());

        // Define CallFactory to handle MKV+MKA ListView
        SetCellFactory(mkvListView);
        SetCellFactory(mkaListView);
        SetCellFactory(subtListView);

        // If subtitles should be opened first, per user's settings let's show it first
        if (appPreferences.getSubtilesFirst()){
            tabPane.getSelectionModel().select(1);  // 0 is mka 1 is subs
        }

        /* Populating settings from the previous run /*/

        // Populating lists
        if (appPreferences.getLoadListsOnStart()){
            if (!appPreferences.getList("MKV").isEmpty()){
                getFilesFromFolder(new File(appPreferences.getList("MKV")), ".mkv");
            }
            if (!appPreferences.getList("MKA").isEmpty()){
                getFilesFromFolder(new File(appPreferences.getList("MKA")), ".mka");
            }
            if (!appPreferences.getList("SUB").isEmpty()){
                getFilesFromFolder(new File(appPreferences.getList("SUB")), appPreferences.getLastTimeUsedSubsExt());
            }
        }
    }
    @FXML
    private ListView<File> mkvListView;
    @FXML
    private ListView<File> mkaListView;
    @FXML
    private ListView<File> subtListView;

    @FXML
    private ChoiceBox<String> subtExt;
    // Observable list of the content subtExt
    private ObservableList<String> subtExtList;

    @FXML
    private ChoiceBox<String> subtCodepage;
    // Observable list of the content subtCodepage
    private ObservableList<String> subtCodepageList;

    @FXML
    private CheckMenuItem fullScreen;

    private ObservableList<File> mkvFileList = FXCollections.observableArrayList();

    private ObservableList<File> mkaFileList = FXCollections.observableArrayList();

    private ObservableList<File> subtFileList = FXCollections.observableArrayList();

    private String folderToOpen;
    // Get host services for opening URLs etc.
    private HostServices hostServices;

    @FXML
    private TabPane tabPane;
    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

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

    public void mkvOpenAction() {
        openFileChooser(".mkv");
    }

    public void mkaOpenAction() { openFileChooser(".mka"); }

    public void subtOpenAction() { openFileChooser(subtExt.getValue()); } // TODO: check if non-empty and show error if needed

    private void openFileChooser (String key){
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
            System.out.println("\tNo folder selected --");
        }
    }

    private void getFilesFromFolder(File directoryReceived, String key){
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
            } else if (subtExt.getItems().contains(key)) {
                subtListView.getItems().clear(); // wipe elements from ListView
                subtFileList.addAll(files);
                subtListView.setItems(subtFileList);
                subtListView.getSelectionModel().select(0);
            }

        } else {
            System.out.println("\tNo files in this folder --");
        }
    }

    /*
     *               MKV Buttons
     */
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
    /*
     *           MKA Buttons
     */
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
    /*
     *           Subtitles Buttons
     */
    @FXML
    private void sUp() {
        int index;
        index = subtListView.getSelectionModel().getSelectedIndex();
        if (index > 0) {
            subtFileList.add(index - 1, subtListView.getSelectionModel().getSelectedItem());
            subtFileList.remove(index + 1);
            subtListView.getSelectionModel().select(index - 1);
        }
    }
    @FXML
    private void sDown(){
        int index;
        index = subtListView.getSelectionModel().getSelectedIndex();
        if (index+1 < subtFileList.size() ){
            subtFileList.add(index+2, subtListView.getSelectionModel().getSelectedItem());
            subtFileList.remove(index);
            subtListView.getSelectionModel().select(index+1);
        }
    }
    @FXML
    private void sDel(){ subtFileList.remove(subtListView.getSelectionModel().getSelectedItem()); }

    /**             PLAYER COMMANDS          */

    private boolean playerSingleCommand(String command){
        if (player != null && player.isAlive()) {
            playerIn.print(command);
            playerIn.print("\n");
            playerIn.flush();
            return true;
        } else { return false; }
    }
    @FXML
    private void fullscreenBtn(){ playerSingleCommand("vo_fullscreen"); }
    @FXML
    private void muteBtn(){ playerSingleCommand("mute"); }
    @FXML
    private void playPrevTrackBtn(){
        if (player != null && player.isAlive()) {
            playerSingleCommand("quit");
            while (player.isAlive());               // TODO: remove crutch, implement bike
        }
        int index;
        index = mkvListView.getSelectionModel().getSelectedIndex();
        if (index > 0) {
            mkvListView.getSelectionModel().select(index-1);
           playBtn();
        }
    }
    @FXML
    private void playNextTrackBtn(){
        if (player != null && player.isAlive()) {
            playerSingleCommand("quit");
            while (player.isAlive());               // TODO: remove crutch, implement bike
        }
        int index;
        index = mkvListView.getSelectionModel().getSelectedIndex();
        if (index+1 < mkvFileList.size() ) {
            mkvListView.getSelectionModel().select(index+1);
            playBtn();
        }
    }

    private Process player;
    private PrintStream playerIn;
    private BufferedReader playerOutErr;

    @FXML
    private void playBtn(){
        if (mkvListView.getSelectionModel().getSelectedItem() != null) {
            boolean Audio = !mkaFileList.isEmpty() && mkvListView.getSelectionModel().getSelectedIndex() < mkaFileList.size();
            boolean Subtitles = !subtFileList.isEmpty() && mkvListView.getSelectionModel().getSelectedIndex() < subtFileList.size();
            boolean SubCodepageDefault = subtCodepage.getValue().equals("default");

            try {
                if (player == null || !player.isAlive()) {
                    //System.out.println("mplayer"  + " " + "-slave"  + " " +"-quiet"  + " " + (fullScreen.isSelected() ? "-fs" : "")  + " " + mkvFileList.get(mkvListView.getSelectionModel().getSelectedIndex()).toPath().toString()  + " " + (Audio?"-audiofile":"")  + " " + (Audio?mkaFileList.get(mkvListView.getSelectionModel().getSelectedIndex()).toPath().toString():"")  + " " + (Subtitles?"-sub":"")  + " " + (Subtitles?subtFileList.get(mkvListView.getSelectionModel().getSelectedIndex()).toPath().toString():""));
                    player = new ProcessBuilder(                                                    // FUCKING MAGIC! DON'T CHANGE SEQUENCE
                            appPreferences.getPath(),                                       // It's a chance for Windows ;)
                            "-slave",
                            Audio?"-audiofile":"",
                            Audio?mkaFileList.get(mkvListView.getSelectionModel().getSelectedIndex()).toPath().toString():"",
                            "-quiet",
                            fullScreen.isSelected() ? "-fs" : "",
                            mkvFileList.get(mkvListView.getSelectionModel().getSelectedIndex()).toPath().toString(),
                            Subtitles?"-sub":"",
                            Subtitles?subtFileList.get(mkvListView.getSelectionModel().getSelectedIndex()).toPath().toString():"",
                            Subtitles?SubCodepageDefault?"":"-subcp":"",                           // Use subtitles -> YES -> Check if we need codepage
                            Subtitles?SubCodepageDefault?"":subtCodepage.getValue():""
                            ).start();

                    PipedInputStream readFrom = new PipedInputStream(256 * 1024);
                    PipedOutputStream writeTo = new PipedOutputStream(readFrom);

                    playerOutErr = new BufferedReader(new InputStreamReader(readFrom));

                    new LineRedirecter(player.getInputStream(), writeTo).start();
                    new LineRedirecter(player.getErrorStream(), writeTo).start();

                    playerIn = new PrintStream(player.getOutputStream());
                } else  {
                    playerIn.print("pause");
                    playerIn.print("\n");
                    playerIn.flush();
                }
            } catch (IOException e) {
                // e.printStackTrace();         // No need
                Alert alertBox = new Alert(Alert.AlertType.ERROR);
                alertBox.setTitle(resourceBundle.getString("Error"));
                alertBox.setHeaderText(null);
                alertBox.setContentText(resourceBundle.getString("unableToStartMplayerError"));
                alertBox.show();
            }
        } else { System.out.println("File not selected"); }
    }

    @FXML
    private void stopBtn(){playerSingleCommand("stop"); }
    @FXML
    private void volumeUpBtn(){ playerSingleCommand("volume +1 0"); }
    @FXML
    private void volumeDownBtn(){ playerSingleCommand("volume -1 0"); }

    @FXML
    private void infoBnt(){ new AboutWindow(this.hostServices); } // TODO: fix this shit with hostSerivces that doesn't work @ linux

    // HANDLING KEYS
    @FXML
    private void vKeyPressed(KeyEvent event){
        if (event.getCode().toString().equals("DELETE"))
            vDel();
    }
    @FXML
    private void aKeyPressed(KeyEvent event){
        if (event.getCode().toString().equals("DELETE"))
            aDel();
    }
    @FXML
    private void sKeyPressed(KeyEvent event){
        if (event.getCode().toString().equals("DELETE"))
            sDel();
    }

    // Will be used to store lists previously opened.
    // Linkage established by ohHidden in Main.java class
    public void shutdown(){
        // If we should save/restore lists ...

        if (appPreferences.getLoadListsOnStart()) {
            if (mkvFileList.isEmpty())
                appPreferences.setList("MKV", "");
            else
                appPreferences.setList("MKV", mkvFileList.get(0).getParent());

            if (mkaFileList.isEmpty())
                appPreferences.setList("MKA", "");
            else
                appPreferences.setList("MKA", mkaFileList.get(0).getParent());

            if (subtFileList.isEmpty())
                appPreferences.setList("SUB", "");
            else {
                appPreferences.setList("SUB", subtFileList.get(0).getParent());
            }
        }
        appPreferences.setLastTimeUsedSusExt(subtExt.getValue());
        appPreferences.setLastTimeUsedSubsCodepage(subtCodepage.getValue());

        Platform.exit();
    }
    // Get event that notify application in case some settings has been changed
    /**             SETTINGS HANDLE          */
    @FXML
    private void settingsBtn(){ new SettingsWindow(); }

    // This function called from MediatorControl after mediator receives request form SettingsController indicating that user updated some required fields.
    public void updateAfterSettingsChanged(){
        //** update list of extensions */

        // Clear and update list
        String extensionPrevSelected = subtExt.getValue();
        subtExtList.clear();
        subtExtList.setAll(appPreferences.getSubsExtensionsList());
        // Try to restore previously selected element
        if (subtExtList.contains(extensionPrevSelected))
            subtExt.setValue(extensionPrevSelected);
        else
            subtExt.setValue(subtExtList.get(0));
        // In case of application failure should be better to save this immediately
        appPreferences.setLastTimeUsedSusExt(subtExt.getValue());

        //** update list of codepage */

        String codepagePrevSelected = subtCodepage.getValue();
        subtCodepageList.clear();
        subtCodepageList.setAll(appPreferences.getSubsCodepageList());
        // Try to restore previously selected element
        if (subtCodepageList.contains(codepagePrevSelected))
            subtCodepage.setValue(codepagePrevSelected);
        else
            subtCodepage.setValue(subtCodepageList.get(0));
        // In case of application failure should be better to save this immediately
        appPreferences.setLastTimeUsedSubsCodepage(subtCodepage.getValue());
    }
}
