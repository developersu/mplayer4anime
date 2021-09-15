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
package mplayer4anime.playlists;

import com.google.gson.*;
import javafx.stage.FileChooser;
import mplayer4anime.ui.ServiceWindow;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class Playlists {
    private static String playlistLocation;

    //TODO: Show popUp if unable to write! Or nothing to write! Or overwrite!
    //TODO: Disable 'Save' button if no files added
    public static boolean SaveAs(ResourceBundle resourceBundle, JsonStorage jStorage){
        File playlistFile;
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle(resourceBundle.getString("SelectFile"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setInitialFileName("MyPlaylist.alpr");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Playlists (*.alpr)", "*.alpr"));
        playlistFile = fileChooser.showSaveDialog(null);

        return writeFile(resourceBundle, playlistFile, jStorage);
    }

    public static boolean SaveCurrent(ResourceBundle resourceBundle, JsonStorage jStorage) {
        if (playlistLocation == null || playlistLocation.equals("")){
            return Playlists.SaveAs(resourceBundle, jStorage);
        }
        return writeFile(resourceBundle, new File(playlistLocation), jStorage);
    }

    /**
     * @return true for success, false for failure
     * */
    private static boolean writeFile(ResourceBundle resourceBundle, File playlistFile, JsonStorage jStorage){
        if (playlistFile == null) {
            ServiceWindow.getErrorNotification(resourceBundle.getString("Error"),
                    "Unable to save: File not selected");// TODO: translate
            return false;
        }

        if (!playlistFile.getAbsolutePath().endsWith(".alpr")) {
            playlistFile = new File(playlistFile.getAbsolutePath() + ".alpr");
        }

        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(playlistFile.getAbsolutePath()), StandardCharsets.UTF_8))
        {
            Gson jsonObject = new GsonBuilder().setPrettyPrinting().create();
            jsonObject.toJson(jStorage, writer);
            writer.close();

            playlistLocation = playlistFile.getAbsolutePath();

            return true;

        } catch (java.io.FileNotFoundException e){
            ServiceWindow.getErrorNotification(resourceBundle.getString("Error"),
                    resourceBundle.getString("ErrorFileNotFound"));
        } catch (java.io.UnsupportedEncodingException e){
            ServiceWindow.getErrorNotification(resourceBundle.getString("Error"),
                    resourceBundle.getString("ErrorOnSaveIncorrectEncoding"));
        } catch (java.io.IOException e){
            ServiceWindow.getErrorNotification(resourceBundle.getString("Error"),
                    resourceBundle.getString("ErrorOnSaveIOProblem"));
        }
        return false;
    }
    /**
     * Interface for Opening playlists via FileChooser
     * */
    public static JsonStorage Read(ResourceBundle resourceBundle){
        File playlistFile;
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle(resourceBundle.getString("SelectFile"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setInitialFileName("MyPlaylist.alpr");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Playlists (*.alpr)", "*.alpr"));
        playlistFile = fileChooser.showOpenDialog(null);

        return ReadByPath(resourceBundle, playlistFile);
    }
    /**
     * Interface for Opening playlists using file itself
     * */
    public static JsonStorage ReadByPath(ResourceBundle resourceBundle, File playlistFile){
        if (playlistFile == null) {
            ServiceWindow.getErrorNotification(resourceBundle.getString("Error"),
                    "Playlist file not selected");// TODO: translate
            return null;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(playlistFile))) {
            JsonStorage jStorage = new Gson().fromJson(reader, JsonStorage.class);
            if (jStorage != null){
                playlistLocation = playlistFile.getAbsolutePath();
                return jStorage;
            }
        } catch (java.io.FileNotFoundException e){
            ServiceWindow.getErrorNotification(resourceBundle.getString("Error"),
                    resourceBundle.getString("ErrorFileNotFound"));
        } catch (com.google.gson.JsonSyntaxException e){
            ServiceWindow.getErrorNotification(resourceBundle.getString("Error"),
                    resourceBundle.getString("ErrorOnOpenIncorrectFormatOfFile"));
        } catch (java.io.IOException e){
            ServiceWindow.getErrorNotification(resourceBundle.getString("Error"),
                    resourceBundle.getString("ErrorOnOpenIOProblem"));
        }
        return null;
    }

    /** Return path to file opened */
    public static String getPlaylistLocation(){
        return playlistLocation;
    }
}