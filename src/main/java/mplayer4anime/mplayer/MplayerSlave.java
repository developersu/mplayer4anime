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
package mplayer4anime.mplayer;

import mplayer4anime.ISlaveModeAppOrchestration;
import mplayer4anime.ui.ServiceWindow;

import java.io.*;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class MplayerSlave implements ISlaveModeAppOrchestration {
    private Process player;
    private PrintStream playerIn;
    private BufferedReader playerOutErr;

    private final ResourceBundle resourceBundle;

    public MplayerSlave(ResourceBundle resourceBundle){
        this.resourceBundle = resourceBundle;
    }

    private boolean playerSingleCommand(String command){
        if (player != null && player.isAlive()) {
            playerIn.print(command);
            playerIn.print("\n");
            playerIn.flush();
            return true;
        }
        return false;
    }
    @Override
    public void subtitlesSwitch(){
        if (! playerSingleCommand("get_sub_visibility"))
            return;

        try {
            String returnedStr;
            int returnedInt = 1;
            while ((returnedStr = playerOutErr.readLine()) != null) {
                if (returnedStr.startsWith("ANS_SUB_VISIBILITY=")) {
                    returnedInt = Integer.parseInt(returnedStr.substring("ANS_SUB_VISIBILITY=".length()));
                    break;
                }
            }
            if (returnedInt == 1)
                playerSingleCommand("sub_visibility 0");
            else
                playerSingleCommand("sub_visibility 1");
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("Can't determine whether subtitles enabled or disabled");
        }
    }
    @Override
    public void fullscreenSwitch(){
        playerSingleCommand("vo_fullscreen");
    }
    @Override
    public void mute(){
        playerSingleCommand("mute");
    }
    @Override
    public void forcePlay(String mplayerPath,
                          String VideoFile,
                          String AudioFile,
                          String SubtitlesFile,
                          String subtitlesEncoding,
                          boolean subtitlesHidden,
                          boolean isFullscreen){
        try {
            if (player != null && player.isAlive()){
                playerSingleCommand("quit");
                player.waitFor(500, TimeUnit.MILLISECONDS);
                player.destroyForcibly();
            }
            playPause(mplayerPath, VideoFile, AudioFile, SubtitlesFile, subtitlesEncoding, subtitlesHidden, isFullscreen);
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
    }
    @Override
    public boolean pause(){
        if (player == null || !player.isAlive())
            return false;
        playerIn.print("pause");
        playerIn.print("\n");
        playerIn.flush();
        return true;
    }
    @Override
    public void playPause(String mplayerPath,
                          String VideoFile,
                          String AudioFile,
                          String SubtitlesFile,
                          String subtitlesEncoding,
                          boolean subtitlesHidden,
                          boolean isFullscreen){
        if (pause())
            return;

        boolean isAudio = AudioFile != null;
        boolean isSubtitles = SubtitlesFile != null;
        boolean SubEncodingDefault = subtitlesEncoding.equals("default");

        try {
            player = new ProcessBuilder(                        // FUCKING MAGIC! DON'T CHANGE SEQUENCE
                    mplayerPath,        // It's a chance for Windows ;)
                    "-slave",
                    isAudio?"-audiofile":"",
                    isAudio?AudioFile:"",
                    "-quiet",
                    isFullscreen ? "-fs" : "",
                    VideoFile,
                    subtitlesHidden||isSubtitles?"-nosub":"",      // Turn off subtitles embedded into MKV file (and replace by localy-stored subs file if needed)
                    isSubtitles?"-sub":"",
                    isSubtitles? SubtitlesFile:"",
                    isSubtitles?SubEncodingDefault?"":"-subcp":"",                           // Use subtitles -> YES -> Check if we need encoding
                    isSubtitles?SubEncodingDefault?"": subtitlesEncoding:""
            ).start();

            PipedInputStream readFrom = new PipedInputStream(256 * 1024);
            PipedOutputStream writeTo = new PipedOutputStream(readFrom);

            playerOutErr = new BufferedReader(new InputStreamReader(readFrom));

            new LineRedirecter(player.getInputStream(), writeTo).start();
            new LineRedirecter(player.getErrorStream(), writeTo).start();

            playerIn = new PrintStream(player.getOutputStream());

            /* If user desired to disable subtitles but populated the list in the SUB pane, then load them and disable visibility.
             * It should be done this way because if we didn't pass them to mplayer during start then user won't be able to enable them later on.
             * There is another bike could be implemented such as passing input file during load but it's far more dumb idea than current implementation.
             */
            if (subtitlesHidden)
                playerSingleCommand("sub_visibility 0");
        }
        catch (IOException e) {
            ServiceWindow.getErrorNotification(resourceBundle.getString("Error"), resourceBundle.getString("ErrorUnableToStartMplayer"));
        }
    }
    @Override
    public void stop(){
        playerSingleCommand("stop");
    }
    @Override
    public void volumeUp(){
        playerSingleCommand("volume +1 0");
    }
    @Override
    public void volumeDown(){
        playerSingleCommand("volume -1 0");
    }
}
