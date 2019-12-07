package mplayer4anime;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import javafx.stage.Stage;
import mplayer4anime.About.AboutWindow;
import mplayer4anime.Playlists.JsonStorage;
import mplayer4anime.Playlists.Playlists;
import mplayer4anime.Settings.SettingsWindow;
import mplayer4anime.appPanes.ControllerPane;
import mplayer4anime.appPanes.ControllerSUB;

import java.io.*;
import java.net.URL;
import java.util.ListIterator;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private ControllerPane mkvPaneController;
    @FXML
    private ControllerPane mkaPaneController;
    @FXML
    private ControllerSUB subPaneController;
    @FXML
    private Label statusLbl;
    @FXML
    private Menu recentlyOpenedMenu;
    // Get preferences
    private AppPreferences appPreferences = new AppPreferences();

    private ResourceBundle resourceBundle;

    @FXML
    private CheckMenuItem fullScreen;

    // Get host services for opening URLs etc.
    private HostServices hostServices;

    @FXML
    private TabPane tabPane;

    @FXML
    private CheckMenuItem subsHide;

    private String currentPlaylistLocation = null;  //TODO: move to the constructor?

    // If application started with playlist passed as an argument, then we'll try to load it (if it's valid).
    public void setPlaylistAsArgument(String playlist) {
        JsonStorage jsonStorage = Playlists.ReadByPath(resourceBundle, new File(playlist));
        setAllLists(jsonStorage);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mkvPaneController.setPaneType("Video");
        mkaPaneController.setPaneType("Audio");
        subPaneController.setPaneType("Subtitles");
        // Register this controller in mediator
        MediatorControl.getInstance().registerMainController(this);

        resourceBundle = rb;

        // Set default list of encodings of the subtitles files:
        subPaneController.setEncoding(appPreferences.getSubsEncodingList(), appPreferences.getLastTimeUsedSubsEncoding());

        // If subtitles should be opened first, per user's settings let's show it first
        if (appPreferences.getSubtilesFirst()){
            tabPane.getSelectionModel().select(1);  // 0 is mka 1 is subs
        }

        fullScreen.setSelected(appPreferences.getFullScreenSelected());
        subsHide.setSelected(appPreferences.getSubtitlesHideSelected());

        String[] recentPlaylists = appPreferences.getRecentPlaylists();
        for (int i = recentPlaylists.length-1; i >= 0; i--)
            if (!recentPlaylists[i].isEmpty())
                addRecentlyOpened(recentPlaylists[i]);
    }

    void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    /*             PLAYER COMMANDS          */
    private boolean playerSingleCommand(String command){
        if (player != null && player.isAlive()) {
            playerIn.print(command);
            playerIn.print("\n");
            playerIn.flush();
            return true;
        } else { return false; }
    }

    @FXML
    private void subsTriggerBtn(){
        if (playerSingleCommand("get_sub_visibility")) {
            String returnedStr;
            int returnedInt = 1;
            try {
                while ((returnedStr = playerOutErr.readLine()) != null) {
                    //System.out.println(returnedStr);
                    if (returnedStr.startsWith("ANS_SUB_VISIBILITY=")) {
                        returnedInt = Integer.parseInt(returnedStr.substring("ANS_SUB_VISIBILITY=".length()));
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("Can't determine whether subtitles enabled or disabled");
            }

            if (returnedInt == 1)
                playerSingleCommand("sub_visibility 0");
            else
                playerSingleCommand("sub_visibility 1");
        }
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
        index = mkvPaneController.getElementSelectedIndex();
        if (index > 0) {
            mkvPaneController.setElementSelectedByIndex(index-1); // .selectNext / .selectPrevious
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
        index = mkvPaneController.getElementSelectedIndex();
        // TODO: add 'link' button
        if (index+1 < mkvPaneController.getElementsCount() ) {
            mkvPaneController.setElementSelectedByIndex(index+1);
        }
        index = mkaPaneController.getElementSelectedIndex();
        if (index+1 < mkaPaneController.getElementsCount() ) {
            mkaPaneController.setElementSelectedByIndex(index+1);
        }
        index = subPaneController.getElementSelectedIndex();
        if (index+1 < subPaneController.getElementsCount() ) {
            subPaneController.setElementSelectedByIndex(index+1);
        }
        playBtn();
    }

    private Process player;
    private PrintStream playerIn;
    private BufferedReader playerOutErr;

    @FXML
    private void playBtn(){
        if (mkvPaneController.getElementSelected() != null) {
            boolean Audio = !mkaPaneController.isElementsListEmpty() && mkvPaneController.getElementSelectedIndex() < mkaPaneController.getElementsCount();
            boolean Subtitles = !subPaneController.isElementsListEmpty() && mkvPaneController.getElementSelectedIndex() < subPaneController.getElementsCount();
            boolean SubEncodingDefault = subPaneController.getSelectedEncoding().equals("default");

            try {
                if (player == null || !player.isAlive()) {
                    player = new ProcessBuilder(                                                    // FUCKING MAGIC! DON'T CHANGE SEQUENCE
                            appPreferences.getPath(),                                       // It's a chance for Windows ;)
                            "-slave",
                            Audio?"-audiofile":"",
                            Audio? mkaPaneController.getElementSelected():"",
                            "-quiet",
                            fullScreen.isSelected() ? "-fs" : "",
                            mkvPaneController.getElementSelected(),
                            subsHide.isSelected()||Subtitles?"-nosub":"",      // Turn off subtitles embedded into MKV file (and replace by localy-stored subs file if needed)
                            Subtitles?"-sub":"",
                            Subtitles? subPaneController.getElementSelected():"",
                            Subtitles?SubEncodingDefault?"":"-subcp":"",                           // Use subtitles -> YES -> Check if we need encoding
                            Subtitles?SubEncodingDefault?"": subPaneController.getSelectedEncoding():""
                            ).start();

                    PipedInputStream readFrom = new PipedInputStream(256 * 1024);
                    PipedOutputStream writeTo = new PipedOutputStream(readFrom);

                    playerOutErr = new BufferedReader(new InputStreamReader(readFrom));

                    new LineRedirecter(player.getInputStream(), writeTo).start();
                    new LineRedirecter(player.getErrorStream(), writeTo).start();

                    playerIn = new PrintStream(player.getOutputStream());

                    /* If user desired to disable subtitles but populated the list in the SUB pane, then load them and disable visibility.
                     * It's should be done this way because if we won't pass them to mplayer during start them user won't be able to enable them later on.
                     * There is another bike could be implemented such as passing input file during load but it's far more dumb idea then current implementation.
                     */
                    if (subsHide.isSelected())
                        playerSingleCommand("sub_visibility 0");
                } else  {
                    playerIn.print("pause");
                    playerIn.print("\n");
                    playerIn.flush();
                }
            } catch (IOException e) {
                ServiceWindow.getErrorNotification(resourceBundle.getString("Error"), resourceBundle.getString("ErrorUnableToStartMplayer"));
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
    private void closeBtn() {
        Stage currentStage = (Stage) tabPane.getScene().getWindow();
        currentStage.close();
    }

    // Will be used to store lists previously opened.
    // Linkage established by ohHidden in MainFX.java class
    void shutdown(){
        appPreferences.setLastTimeUsedSubsEncoding(subPaneController.getSelectedEncoding());
        appPreferences.setFullScreenSelected(fullScreen.isSelected());
        appPreferences.setSubtitlesHideSelected(subsHide.isSelected());

        String[] storeRecentArr = new String[10];
        for (int i =0; i < recentlyOpenedMenu.getItems().size() - 2 && !(i > 9); i++) {       // Don't take separator and Clean button
            storeRecentArr[i] = (String) recentlyOpenedMenu.getItems().get(i).getUserData();
        }
        appPreferences.setRecentPlaylists(storeRecentArr);
        Platform.exit();
    }

    @FXML
    private void infoBtn(){ new AboutWindow(this.hostServices); } // TODO: fix this shit with hostSerivces that doesn't work @ linux

    /**             SETTINGS HANDLE          */
    @FXML
    private void settingsBtn(){ new SettingsWindow(); }
    // Get event that notify application in case some settings has been changed
    // This function called from MediatorControl after mediator receives request form SettingsController indicating that user updated some required fields.
    void updateAfterSettingsChanged(){
        /* update list of encoding */
        subPaneController.setEncoding(appPreferences.getSubsEncodingList(), null);
        // In case of application failure should be better to save this immediately
        appPreferences.setLastTimeUsedSubsEncoding(subPaneController.getSelectedEncoding());
    }

    @FXML
    private void openBtn() {
        JsonStorage jsonStorage = Playlists.Read(resourceBundle);
        setAllLists(jsonStorage);
    }
    private void setAllLists(JsonStorage jsonStorage){
        if (jsonStorage != null) {
            mkvPaneController.cleanList();
            mkaPaneController.cleanList();
            subPaneController.cleanList();
            mkvPaneController.setFilesFromList(jsonStorage.getVideo());
            mkaPaneController.setFilesFromList(jsonStorage.getAudio());
            subPaneController.setFilesFromList(jsonStorage.getSubs());
            subPaneController.selectEncodingValue(jsonStorage.getSubEncoding(), appPreferences);

            currentPlaylistLocation = Playlists.getPlaylistLocation();                 // TODO: Implement listener? mmm...
            //System.out.println(currentPlaylistLocation);
            statusLbl.setText(currentPlaylistLocation);
            addRecentlyOpened(currentPlaylistLocation);
        }
    }
    @FXML
    private void saveBtn() {
        if (mkvPaneController.getElementsCount() == 0)
            ServiceWindow.getErrorNotification(resourceBundle.getString("Error"), resourceBundle.getString("ErrorUnableToSaveEmptyPlaylist"));
        else {
            JsonStorage jsonStorage = new JsonStorage(mkvPaneController.getElementsAll(), mkaPaneController.getElementsAll(), subPaneController.getElementsAll(), subPaneController.getSelectedEncoding());
            if (Playlists.SaveCurrent(resourceBundle, jsonStorage)) {
                this.currentPlaylistLocation = Playlists.getPlaylistLocation();
                this.statusLbl.setText(currentPlaylistLocation);    //TODO: update header of the application to include this?
                addRecentlyOpened(currentPlaylistLocation);
            }
        }
    }
    @FXML
    private void saveAsBtn() {
        if (mkvPaneController.getElementsCount() == 0)
            ServiceWindow.getErrorNotification(resourceBundle.getString("Error"), resourceBundle.getString("ErrorUnableToSaveEmptyPlaylist"));
        else {
            JsonStorage jsonStorage = new JsonStorage(mkvPaneController.getElementsAll(), mkaPaneController.getElementsAll(), subPaneController.getElementsAll(), subPaneController.getSelectedEncoding());
            if (Playlists.SaveAs(resourceBundle, jsonStorage)) {
                this.currentPlaylistLocation = Playlists.getPlaylistLocation();
                this.statusLbl.setText(currentPlaylistLocation);    //TODO: update header of the application to include this?
                addRecentlyOpened(currentPlaylistLocation);
            }
        }
    }
    @FXML
    private void cleanAllRecentlyOpened(){
        recentlyOpenedMenu.getItems().remove(0,recentlyOpenedMenu.getItems().size() - 2);
    }

    private void addRecentlyOpened(String playlistPath){
        ListIterator<MenuItem> iteratorItem = recentlyOpenedMenu.getItems().listIterator();
        while (iteratorItem.hasNext()) {
            MenuItem mi = iteratorItem.next();
            if (mi.getUserData() != null && mi.getUserData().equals(playlistPath)) {
                recentlyOpenedMenu.getItems().remove(mi);
                recentlyOpenedMenu.getItems().add(0, mi);
                return;
            }
        }

        MenuItem menuItem = new MenuItem();
        String fileNameOnly;

        fileNameOnly = playlistPath.substring(playlistPath.lastIndexOf(File.separator) + 1);
        menuItem.setText(fileNameOnly);

        menuItem.setUserData(playlistPath);
        menuItem.setOnAction(actionEvent -> {
            JsonStorage jsonStorage = Playlists.ReadByPath(resourceBundle, new File(playlistPath));
            setAllLists(jsonStorage);
        });
        // Limit list to 13 elements (2 in the end are separator and clear button)
        if (recentlyOpenedMenu.getItems().size() >= 11)
            recentlyOpenedMenu.getItems().remove(9, recentlyOpenedMenu.getItems().size() - 2);
        recentlyOpenedMenu.getItems().add(0, menuItem);
    }
}

