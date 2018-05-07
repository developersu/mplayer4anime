package mplayer4anime;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import mplayer4anime.About.AboutWindow;
import mplayer4anime.Settings.SettingsWindow;
import mplayer4anime.appPanes.ControllerMKA;
import mplayer4anime.appPanes.ControllerMKV;
import mplayer4anime.appPanes.ControllerSUB;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private ControllerMKV mkvPaneController;
    @FXML
    private ControllerSUB subPaneController;
    @FXML
    private ControllerMKA mkaPaneController;

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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Register this controller in mediator
        MediatorControl.getInstance().registerMainController(this);

        resourceBundle = rb;

        // Set default extension of the subtitles files:
        subPaneController.subtExtList = FXCollections.observableArrayList( appPreferences.getSubsExtensionsList() ); // Receive list from storage
        subPaneController.subtExt.setItems(subPaneController.subtExtList);
        if (appPreferences.getLastTimeUsedSubsExt().isEmpty())      // not sure that it's possible
            subPaneController.subtExt.setValue(subPaneController.subtExtList.get(0));
        else
            subPaneController.subtExt.setValue(appPreferences.getLastTimeUsedSubsExt());

        // Set default list of codepages of the subtitles files:
        subPaneController.subtCodepageList = FXCollections.observableArrayList(appPreferences.getSubsCodepageList());
        subPaneController.subtCodepage.setItems(subPaneController.subtCodepageList);
        if (appPreferences.getLastTimeUsedSubsCodepage().isEmpty())
            subPaneController.subtCodepage.setValue(subPaneController.subtCodepageList.get(0));
        else
            subPaneController.subtCodepage.setValue(appPreferences.getLastTimeUsedSubsCodepage());

        // If subtitles should be opened first, per user's settings let's show it first
        if (appPreferences.getSubtilesFirst()){
            tabPane.getSelectionModel().select(1);  // 0 is mka 1 is subs
        }

        /* Populating settings from the previous run /*/
        // Populating lists
        if (appPreferences.getLoadListsOnStart()){         // TODO: probably should be dedicated method in abstract class defined
            if (!appPreferences.getListMKV().isEmpty()){
                mkvPaneController.getFilesFromFolder(new File(appPreferences.getListMKV()), ".mkv");
            }
            if (!appPreferences.getListMKA().isEmpty()){
                mkaPaneController.getFilesFromFolder(new File(appPreferences.getListMKA()), ".mka");
            }
            if (!appPreferences.getListSUB().isEmpty()){
                subPaneController.getFilesFromFolder(new File(appPreferences.getListSUB()), appPreferences.getLastTimeUsedSubsExt());
            }
        }
        fullScreen.setSelected(appPreferences.getFullScreenSelected());
        subsHide.setSelected(appPreferences.getSubtitlesHideSelected());
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

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
    private void subsTriggerBtn(){
        playerSingleCommand("get_sub_visibility");
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
        }
        catch (IOException e) {
            System.out.println("Can't determine if subtitles enabled/disabled");
        }

        if (returnedInt == 1)
            playerSingleCommand("sub_visibility 0");
        else
            playerSingleCommand("sub_visibility 1");

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
        index = mkvPaneController.paneListView.getSelectionModel().getSelectedIndex();
        if (index > 0) {
            mkvPaneController.paneListView.getSelectionModel().select(index-1);
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
        index = mkvPaneController.paneListView.getSelectionModel().getSelectedIndex();
        if (index+1 < mkvPaneController.paneFileList.size() ) {
            mkvPaneController.paneListView.getSelectionModel().select(index+1);
            playBtn();
        }
    }

    private Process player;
    private PrintStream playerIn;
    private BufferedReader playerOutErr;

    @FXML
    private void playBtn(){
        if (mkvPaneController.paneListView.getSelectionModel().getSelectedItem() != null) {
            boolean Audio = !mkaPaneController.paneFileList.isEmpty() && mkvPaneController.paneListView.getSelectionModel().getSelectedIndex() < mkaPaneController.paneFileList.size();
            boolean Subtitles = !subPaneController.paneFileList.isEmpty() && mkvPaneController.paneListView.getSelectionModel().getSelectedIndex() < subPaneController.paneFileList.size();
            boolean SubCodepageDefault = subPaneController.subtCodepage.getValue().equals("default");

            try {
                if (player == null || !player.isAlive()) {
                    player = new ProcessBuilder(                                                    // FUCKING MAGIC! DON'T CHANGE SEQUENCE
                            appPreferences.getPath(),                                       // It's a chance for Windows ;)
                            "-slave",
                            Audio?"-audiofile":"",
                            Audio? mkaPaneController.paneFileList.get(mkvPaneController.paneListView.getSelectionModel().getSelectedIndex()).toPath().toString():"",
                            "-quiet",
                            fullScreen.isSelected() ? "-fs" : "",
                            mkvPaneController.paneFileList.get(mkvPaneController.paneListView.getSelectionModel().getSelectedIndex()).toPath().toString(),
                            subsHide.isSelected()||Subtitles?"-nosub":"",      // Turn off subtitles embedded into MKV file (and replace by localy-stored subs file if needed)
                            Subtitles?"-sub":"",
                            Subtitles? subPaneController.paneFileList.get(mkvPaneController.paneListView.getSelectionModel().getSelectedIndex()).toPath().toString():"",
                            Subtitles?SubCodepageDefault?"":"-subcp":"",                           // Use subtitles -> YES -> Check if we need codepage
                            Subtitles?SubCodepageDefault?"": subPaneController.subtCodepage.getValue():""
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

    // Will be used to store lists previously opened.
    // Linkage established by ohHidden in Main.java class
    public void shutdown(){
        // If we should save/restore lists ...

        if (appPreferences.getLoadListsOnStart()) {
            if (mkvPaneController.paneFileList.isEmpty())
                appPreferences.setListMKV("");
            else
                appPreferences.setListMKV(mkvPaneController.paneFileList.get(0).getParent());

            if (mkaPaneController.paneFileList.isEmpty())
                appPreferences.setListMKA("");
            else
                appPreferences.setListMKA(mkaPaneController.paneFileList.get(0).getParent());

            if (subPaneController.paneFileList.isEmpty())
                appPreferences.setListSUB("");
            else
                appPreferences.setListSUB(subPaneController.paneFileList.get(0).getParent());
        }
        appPreferences.setLastTimeUsedSusExt(subPaneController.subtExt.getValue());
        appPreferences.setLastTimeUsedSubsCodepage(subPaneController.subtCodepage.getValue());
        appPreferences.setFullScreenSelected(fullScreen.isSelected());
        appPreferences.setSubtitlesHideSelected(subsHide.isSelected());

        Platform.exit();
    }


    @FXML
    private void infoBnt(){ new AboutWindow(this.hostServices); } // TODO: fix this shit with hostSerivces that doesn't work @ linux

    /**             SETTINGS HANDLE          */
    @FXML
    private void settingsBtn(){ new SettingsWindow(); }
    // Get event that notify application in case some settings has been changed
    // This function called from MediatorControl after mediator receives request form SettingsController indicating that user updated some required fields.
    public void updateAfterSettingsChanged(){
        /** update list of extensions */
        // Clear and update list
        String extensionPrevSelected = subPaneController.subtExt.getValue();
        subPaneController.subtExtList.clear();
        subPaneController.subtExtList.setAll(appPreferences.getSubsExtensionsList());
        // Try to restore previously selected element
        if (subPaneController.subtExtList.contains(extensionPrevSelected))
            subPaneController.subtExt.setValue(extensionPrevSelected);
        else
            subPaneController.subtExt.setValue(subPaneController.subtExtList.get(0));
        // In case of application failure should be better to save this immediately
        appPreferences.setLastTimeUsedSusExt(subPaneController.subtExt.getValue());

        /** update list of codepage */
        String codepagePrevSelected = subPaneController.subtCodepage.getValue();
        subPaneController.subtCodepageList.clear();
        subPaneController.subtCodepageList.setAll(appPreferences.getSubsCodepageList());
        // Try to restore previously selected element
        if (subPaneController.subtCodepageList.contains(codepagePrevSelected))
            subPaneController.subtCodepage.setValue(codepagePrevSelected);
        else
            subPaneController.subtCodepage.setValue(subPaneController.subtCodepageList.get(0));
        // In case of application failure should be better to save this immediately
        appPreferences.setLastTimeUsedSubsCodepage(subPaneController.subtCodepage.getValue());
    }

}
