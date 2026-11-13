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
package mplayer4anime.playlists;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.Files;
import java.util.ResourceBundle;

import static java.nio.charset.StandardCharsets.UTF_8;
import static mplayer4anime.ui.ServiceWindow.getErrorNotification;

public class Playlists {
    private static String playlistLocation;

    //TODO: Show popUp if unable to write! Or nothing to write! Or overwrite!
    //TODO: Disable 'Save' button if no files added
    public static boolean saveAs(ResourceBundle rb, JsonStorage jStorage) {
        var fileChooser = new FileChooser();
        fileChooser.setTitle(rb.getString("SelectFile"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setInitialFileName("MyPlaylist.alpr");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Playlists (*.alpr)", "*.alpr"));

        return writeFile(rb, fileChooser.showSaveDialog(null), jStorage);
    }

    public static boolean saveCurrent(ResourceBundle rb, JsonStorage jStorage) {
        if (playlistLocation == null || playlistLocation.isEmpty())
            return Playlists.saveAs(rb, jStorage);

        return writeFile(rb, new File(playlistLocation), jStorage);
    }

    /**
     * @return true for success, false for failure
     *
     */
    private static boolean writeFile(ResourceBundle rb, File playlistFile, JsonStorage jStorage) {
        if (playlistFile == null) {
            getErrorNotification(rb.getString("Error"), "Unable to save: File not selected");// TODO: translate
            return false;
        }

        if (!playlistFile.getAbsolutePath().endsWith(".alpr")) {
            playlistFile = new File(playlistFile.getAbsolutePath() + ".alpr");
        }

        try (var writer = new OutputStreamWriter(new FileOutputStream(playlistFile.getAbsolutePath()), UTF_8)) {
            var jsonObject = new GsonBuilder().setPrettyPrinting().create();
            jsonObject.toJson(jStorage, writer);
            writer.close();
            playlistLocation = playlistFile.getAbsolutePath();
            return true;
        }
        catch (java.io.FileNotFoundException e) {
            getErrorNotification(rb.getString("Error"), rb.getString("ErrorFileNotFound"));
        }
        catch (java.io.UnsupportedEncodingException e) {
            getErrorNotification(rb.getString("Error"), rb.getString("ErrorOnSaveIncorrectEncoding"));
        }
        catch (java.io.IOException e) {
            getErrorNotification(rb.getString("Error"), rb.getString("ErrorOnSaveIOProblem"));
        }
        return false;
    }

    /**
     * Interface for Opening playlists via FileChooser
     **/
    public static JsonStorage openPlaylistFileChooser(ResourceBundle rb) {
        var fileChooser = new FileChooser();
        fileChooser.setTitle(rb.getString("SelectFile"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setInitialFileName("MyPlaylist.alpr");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Playlists (*.alpr)", "*.alpr"));

        return readByPath(rb, fileChooser.showOpenDialog(null));
    }

    /**
     * Interface for Opening playlists using file itself
     */
    public static JsonStorage readByPath(ResourceBundle rb, File playlistFile) {
        if (playlistFile == null) {
            getErrorNotification(rb.getString("Error"), "Playlist file not selected"); // TODO: translate
            return null;
        }

        try (var reader = new InputStreamReader(new FileInputStream(playlistFile))) {
            var jStorage = new Gson().fromJson(reader, JsonStorage.class);
            if (jStorage == null)
                return null;
            playlistLocation = playlistFile.getAbsolutePath();
            return jStorage;
        }
        catch (FileNotFoundException e) {
            getErrorNotification(rb.getString("Error"), rb.getString("ErrorFileNotFound"));
        }
        catch (JsonSyntaxException e) {
            getErrorNotification(rb.getString("Error"), rb.getString("ErrorOnOpenIncorrectFormatOfFile"));
        }
        catch (IOException e) {
            getErrorNotification(rb.getString("Error"), rb.getString("ErrorOnOpenIOProblem"));
        }
        return null;
    }

    public static JsonStorage readByPathSilent(File playlistFile) {
        try (var reader = new InputStreamReader(Files.newInputStream(playlistFile.toPath()))) {
            var jStorage = new Gson().fromJson(reader, JsonStorage.class);
            if (jStorage == null)
                return null;
            playlistLocation = playlistFile.getAbsolutePath();
            return jStorage;
        }
        catch (Exception ignore) {
        }
        return null;
    }

    /**
     * Return path to file opened
     */
    public static String getPlaylistLocation() {
        return playlistLocation;
    }
}