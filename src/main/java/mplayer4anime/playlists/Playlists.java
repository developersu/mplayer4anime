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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.ResourceBundle;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static mplayer4anime.ui.ServiceWindow.getErrorNotification;

public class Playlists {
    private static final Logger log = LoggerFactory.getLogger(Playlists.class);

    private static final Gson GSON = new Gson();
    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

    private String playlistLocation;
    private final ResourceBundle resourceBundle;

    public Playlists(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    // TODO: Consider adding option to be able to create playlists with relative paths
    public boolean saveAs(JsonStorage jStorage) {
        var fileChooser = new FileChooser();
        fileChooser.setTitle(str("SelectFile"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setInitialFileName("MyPlaylist.alpr");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Playlists (*.alpr)", "*.alpr"));
        return write(fileChooser.showSaveDialog(null), jStorage);
    }

    public boolean save(JsonStorage jStorage) {
        if (playlistLocation == null || playlistLocation.isEmpty())
            return saveAs(jStorage);

        return write(new File(playlistLocation), jStorage);
    }

    /**
     * @return true on success
     */
    private boolean write(File playlistFile, JsonStorage jStorage) {
        if (playlistFile == null)
            return false;

        if (!playlistFile.getAbsolutePath().endsWith(".alpr"))
            playlistFile = new File(playlistFile.getAbsolutePath() + ".alpr");

        File tmpFile = null;
        try {
            tmpFile = File.createTempFile(".alpr-", ".tmp", playlistFile.getParentFile());
            try (var writer = new OutputStreamWriter(Files.newOutputStream(tmpFile.toPath()), UTF_8)) {
                PRETTY_GSON.toJson(jStorage, writer);
            }
            Files.move(tmpFile.toPath(), playlistFile.toPath(), REPLACE_EXISTING, ATOMIC_MOVE);
            playlistLocation = playlistFile.getAbsolutePath();
            return true;
        }
        catch (java.io.FileNotFoundException e) {
            getErrorNotification(str("Error"), str("ErrorFileNotFound"));
        }
        catch (java.io.IOException e) {
            getErrorNotification(str("Error"), str("ErrorOnSaveIOProblem"));
        }
        finally {
            if (tmpFile != null && tmpFile.exists())
                tmpFile.delete();
        }
        return false;
    }

    public JsonStorage openFileChooser() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle(str("SelectFile"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setInitialFileName("MyPlaylist.alpr");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Playlists (*.alpr)", "*.alpr"));
        return read(fileChooser.showOpenDialog(null));
    }

    /**
     * Open playlist using GUI
     */
    public JsonStorage read(File playlistFile) {
        if (playlistFile == null)
            return null;
        try {
            return parse(playlistFile);
        }
        catch (FileNotFoundException e) {
            getErrorNotification(str("Error"), str("ErrorFileNotFound"));
        }
        catch (JsonSyntaxException e) {
            getErrorNotification(str("Error"), str("ErrorOnOpenIncorrectFormatOfFile"));
        }
        catch (IOException e) {
            getErrorNotification(str("Error"), str("ErrorOnOpenIOProblem"));
        }
        return null;
    }

    public JsonStorage readQuiet(File playlistFile) {
        if (playlistFile == null)
            return null;
        try {
            return parse(playlistFile);
        }
        catch (Exception e) {
            log.warn("Read file failure", e);
        }
        return null;
    }

    private JsonStorage parse(File playlistFile) throws IOException, JsonSyntaxException {
        try (var reader = new InputStreamReader(Files.newInputStream(playlistFile.toPath()))) {
            var jStorage = GSON.fromJson(reader, JsonStorage.class);
            if (jStorage == null)
                return null;
            playlistLocation = playlistFile.getAbsolutePath();
            return jStorage;
        }
    }

    private String str(String key) {
        return resourceBundle.getString(key);
    }

    public String getPlaylistLocation() {
        return playlistLocation;
    }
}