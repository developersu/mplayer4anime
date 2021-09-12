/*
    Copyright 2018-2021 Dmitry Isaenko
     
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
package mplayer4anime.mpv;

import mplayer4anime.ISlaveModeAppOrchestration;
import mplayer4anime.ServiceWindow;

import java.util.ResourceBundle;

public class MpvSlave implements ISlaveModeAppOrchestration {
    static {
        if (! MpvJniLibraryLoader.load()){
            ServiceWindow.getErrorNotification("Error",
                    "Unable to load mpv back end library. Please use mplayer instead"); // TODO: use bundle & translate
        }
    }

    native void play();

    public MpvSlave(ResourceBundle resourceBundle){

    }

    @Override
    public void subtitlesSwitch() {

    }

    @Override
    public void fullscreenSwitch() {

    }

    @Override
    public void mute() {

    }

    @Override
    public void forcePlay(String mplayerPath,
                          String VideoFile,
                          String AudioFile,
                          String SubtitlesFile,
                          String subtitlesEncoding,
                          boolean subtitlesHidden,
                          boolean isFullscreen) {

    }

    @Override
    public boolean pause() {
        return false;
    }

    @Override
    public void playPause(String mplayerPath,
                          String VideoFile,
                          String AudioFile,
                          String SubtitlesFile,
                          String subtitlesEncoding,
                          boolean subtitlesHidden,
                          boolean isFullscreen) {

    }

    @Override
    public void stop() {

    }

    @Override
    public void volumeUp() {

    }

    @Override
    public void volumeDown() {

    }
}
