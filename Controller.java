package sample;

import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.*;
import javafx.util.Callback;
import sample.About.AboutWindow;

import java.io.*;
import java.util.Arrays;

public class Controller {

    @FXML
    private ListView<File> mkvListView;
    @FXML
    private ListView<File> mkaListView;
    @FXML
    private ListView<File> subtListView;

    @FXML
    private ChoiceBox<String> subtType;
    @FXML
    private ChoiceBox<String> subtCodepage;
    @FXML
    private CheckMenuItem fullScreen;

    private ObservableList<File> mkvFileList = FXCollections.observableArrayList();

    private ObservableList<File> mkaFileList = FXCollections.observableArrayList();

    private ObservableList<File> subtFileList = FXCollections.observableArrayList();

    // Get host services for opening URLs etc.
    private HostServices hostServices;

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

    @FXML
    public void initialize() {
        // Set default extension of the subtitles files:
        subtType.setItems(FXCollections.observableArrayList(
                ".ass",
                ".str"
        ));
        subtType.setValue(".ass");
        // Set default list of codepages of the subtitles files:
        subtCodepage.setItems(FXCollections.observableArrayList(
                "default",
                "utf8",
                "cp1251",
                "koi8-r"
        ));
        subtCodepage.setValue("utf8");
        // Define CallFactory to handle MKV+MKA ListView
        SetCellFactory(mkvListView);
        SetCellFactory(mkaListView);
        SetCellFactory(subtListView);
    }

    public void mkvOpenAction() {
        fileFiltering(".mkv");
    }

    public void mkaOpenAction() {
        fileFiltering(".mka");
    }

    public void subtOpenAction() {
        fileFiltering(subtType.getValue());
    } // FIX

    private void fileFiltering (String key){
        File directoryReceived;      // Store files (folder) received from selector
        File[] files;                // Store files mkv/mka
        DirectoryChooser dirSelect;

        // Show directory selector
        dirSelect = new DirectoryChooser();
        dirSelect.setTitle("Select directory");
        dirSelect.setInitialDirectory(new File(System.getProperty("user.home")));
        directoryReceived = dirSelect.showDialog(null);                 // TODO: Clarify how the fuck is it works

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
                } else if (key.equals(".srt") || key.equals(".ass")) {
                    subtListView.getItems().clear(); // wipe elements from ListView
                    subtFileList.addAll(files);
                    subtListView.setItems(subtFileList);
                    subtListView.getSelectionModel().select(0);
                }

            } else {
                System.out.println("-- No files in this folder --");
            }
        } else {
            System.out.println("-- No folder selected --");
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
                    player = new ProcessBuilder(
                            "mplayer" ,
                            "-slave",
                            Audio?"-audiofile":"",                                                 // FUCKING MAGIC! DON'T CHANGE SEQUENCE
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
                e.printStackTrace();
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
    private void infoBnt(){ new AboutWindow(this.hostServices); } // TODO: fix this shit with hostSerivces that doesn't work
}
