/*
    Copyright 2018-2023 Dmitry Isaenko
     
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

import mplayer4anime.IPlayer;

import java.util.ResourceBundle;

public class MpvSlave implements IPlayer {

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
        //TODO: fix
        Thread thread = new Thread(new MpvProcess(VideoFile, AudioFile, SubtitlesFile));
        thread.start();
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
