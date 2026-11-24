/*
    Copyright 2018-2026 Dmitry Isaenko
     
    This file is part of mplayer4anime.

    mplayer4anime is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    mplayer4anime is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with mplayer4anime.  If not, see <https://www.gnu.org/licenses/>.
 */
package mplayer4anime.ui.landing;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.SplitMenuButton;
import mplayer4anime.AppPreferences;
import mplayer4anime.IPlayer;
import mplayer4anime.PlayRequest;
import mplayer4anime.player.PlayerService;
import mplayer4anime.ui.landing.panes.ControllerPane;
import mplayer4anime.ui.landing.panes.ControllerPaneSubtitles;

import java.net.URL;
import java.util.ResourceBundle;

public class PlayerToolbarController implements Initializable {
    @FXML
    private CheckMenuItem fullScreen;
    @FXML
    private CheckMenuItem subsHide;
    @FXML
    private Button muteBtn, playPrevTrackBtn, playNextTrackBtn, playBtn, stopBtn, volumeUpBtn, volumeDownBtn;
    @FXML
    private SplitMenuButton subsTriggerBtn, fullscreenBtn;

    private AppPreferences appPreferences;
    private PlayerService playerService;
    private ControllerPane mkvPaneController, mkaPaneController;
    private ControllerPaneSubtitles subPaneController;
    // currently  video (last one played or force-played), or null until the first file is played
    private String currentVideo;

    // Player is resolved at the moment of each action
    private IPlayer player() {
        return playerService.getPlayer();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.appPreferences = AppPreferences.get();

        fullScreen.setSelected(appPreferences.getFullScreenSelected());
        subsHide.setSelected(appPreferences.getSubtitlesHideSelected());

        stopBtn.setOnAction(_ -> {
            persistLivePosition();
            player().stop();
        });
        volumeUpBtn.setOnAction(_ -> player().volumeUp());
        volumeDownBtn.setOnAction(_ -> player().volumeDown());
        muteBtn.setOnAction(_ -> player().mute());
        playBtn.setOnAction(_ -> play());
        playPrevTrackBtn.setOnAction(_ -> playPrevTrack());
        playNextTrackBtn.setOnAction(_ -> playNextTrack());
        subsTriggerBtn.setOnAction(_ -> player().subtitlesSwitch());
        fullscreenBtn.setOnAction(_ -> player().fullscreenSwitch());
    }

    public void initializeMainUiController(PlayerService playerService,
                                           ControllerPane mkvPaneController,
                                           ControllerPane mkaPaneController,
                                           ControllerPaneSubtitles subPaneController) {
        this.playerService = playerService;
        this.mkvPaneController = mkvPaneController;
        this.mkaPaneController = mkaPaneController;
        this.subPaneController = subPaneController;

        // When a file ends on its own, start the next one, if any.
        // Registered through the service so the listener survives an engine switch.
        playerService.setOnFinishedListener(this::onPlaybackFinished);

        bindButtonToEmptyList(stopBtn);
        bindButtonToEmptyList(volumeUpBtn);
        bindButtonToEmptyList(volumeDownBtn);
        bindButtonToEmptyList(muteBtn);
        bindButtonToEmptyList(playBtn);
        bindButtonToEmptyList(subsTriggerBtn);
        bindButtonToEmptyList(fullscreenBtn);

        playPrevTrackBtn.disableProperty().bind(
                Bindings.isEmpty(mkvPaneController.getPaneFileList()).or(
                        Bindings.equal(mkvPaneController.getSelectedIndexProperty(), 0)
                ));

        playNextTrackBtn.disableProperty().bind(
                Bindings.isEmpty(mkvPaneController.getPaneFileList()).or(
                        Bindings.equal(mkvPaneController.getSelectedIndexProperty(),
                                Bindings.subtract(Bindings.size(mkvPaneController.getPaneFileList()), 1))
                ));
    }

    private void bindButtonToEmptyList(ButtonBase button) {
        button.disableProperty().bind(Bindings.isEmpty(mkvPaneController.getPaneFileList()));
    }

    private void play() {
        var selectedVideo = mkvPaneController.getSelectedAbsolutePath();
        if (selectedVideo == null)
            return;

        // Remember where the live playback stands, so that a Stop (or an app restart) can be resumed from that spot.
        persistLivePosition();

        // A different file was selected: start it, instead of resuming the old file still loaded in the player.
        if (currentVideo != null && !selectedVideo.equals(currentVideo)) {
            forcePlay(request());
        }
        else {
            // The player ends up handling the selected file (started fresh or toggled from pause), so remember it
            currentVideo = selectedVideo;
            player().playPause(request());
        }
    }

    private void playPrevTrack() {
        var index = mkvPaneController.getSelected() - 1;
        if (index < 0)
            return;

        mkvPaneController.setSelected(index);

        index = mkaPaneController.getSelected();
        if (index > 0)
            mkaPaneController.setSelected(index - 1);

        index = subPaneController.getSelected();
        if (index > 0)
            subPaneController.setSelected(index - 1);

        forcePlay(request());
    }

    private void playNextTrack() {
        var index = mkvPaneController.getSelected() + 1;
        if (index >= mkvPaneController.getElementsSize())
            return;

        mkvPaneController.setSelected(index);

        index = mkaPaneController.getSelected() + 1;
        if (index < mkaPaneController.getElementsSize())
            mkaPaneController.setSelected(index);

        index = subPaneController.getSelected() + 1;
        if (index < subPaneController.getElementsSize())
            subPaneController.setSelected(index);

        forcePlay(request());
    }

    // Invoked by the player (on a thread other than the FX one) when the loaded file played to the end:
    // start the next track, if any.
    private void onPlaybackFinished() {
        try {
            Platform.runLater(() -> {
                // The file played to the end, so any position saved for it during playback is stale:
                // replaying the file must start from the beginning.
                if (currentVideo != null)
                    appPreferences.setLatestPlayedVideo(currentVideo, 0);
                if (!appPreferences.getPlayNextOnEnd())
                    return;
                // Player loads a file right away when the user switches tracks manually; an unreadable time-pos
                // means the session is truly empty, i.e. the finished file was not replaced.
                if (player().getTimePos() >= 0)
                    return;
                playNextTrack();
            });
        }
        catch (IllegalStateException _) {
            // the FX toolkit is already shutting down
        }
    }

    private void forcePlay(PlayRequest request) {
        currentVideo = request.video();
        player().forcePlay(request);
    }

    // mplayer reports 0 after Stop and storing 0 would wipe out the real resume point.
    void persistLivePosition() {
        if (currentVideo == null)
            return;
        var position = player().getTimePos();
        if (position > 0)
            appPreferences.setLatestPlayedVideo(currentVideo, position);
    }

    // Everything the player needs to (re)start the selected track.
    private PlayRequest request() {
        return new PlayRequest(
                mkvPaneController.getSelectedAbsolutePath(),
                mkaPaneController.getSelectedAbsolutePath(),
                subPaneController.getSelectedAbsolutePath(),
                subPaneController.getEncoding(),
                subsHide.isSelected(),
                fullScreen.isSelected(),
                resumePosition()
        );
    }

    // The stored position applies to the very file it was recorded for; any other video starts from the beginning.
    private double resumePosition() {
        var selectedVideo = mkvPaneController.getSelectedAbsolutePath();
        return selectedVideo != null && selectedVideo.equals(appPreferences.getLatestPlayedVideo())
                ? Math.max(appPreferences.getLatestPlayedVideoPosition(), 0)
                : 0;
    }

    void shutdown() {
        appPreferences.setFullScreenSelected(fullScreen.isSelected());
        appPreferences.setSubtitlesHideSelected(subsHide.isSelected());
    }
}
