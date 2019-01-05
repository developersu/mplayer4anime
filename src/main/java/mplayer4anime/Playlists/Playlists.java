package mplayer4anime.Playlists;

import com.google.gson.*;
import javafx.stage.FileChooser;
import mplayer4anime.ServiceWindow;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class Playlists {

    private static String playlistLocation;
    //TODO: Show popUp if unable to write! Or nothing to write! Or overwrite!
    /**
     * Interface for Save As functionality
     * */
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
    /**
     * Interface for Save functionality
     * */
    public static boolean SaveCurrent(ResourceBundle resourceBundle, JsonStorage jStorage) {
        if (playlistLocation == null || playlistLocation.equals("")){
            return Playlists.SaveAs(resourceBundle, jStorage);
        }
        else {
            return writeFile(resourceBundle, new File(playlistLocation), jStorage);
        }
    }

    // Working with file itself
    private static boolean writeFile(ResourceBundle resourceBundle, File playlistFile, JsonStorage jStorage){
        // TODO: Add 'Override pop-up notification!'
        if (playlistFile != null) {
            if (!playlistFile.getAbsolutePath().endsWith(".alpr")) {
                playlistFile = new File(playlistFile.getAbsolutePath() + ".alpr");
            }
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(playlistFile.getAbsolutePath()), StandardCharsets.UTF_8))
            {
                Gson jsonObject = new GsonBuilder().setPrettyPrinting().create();
                jsonObject.toJson(jStorage, writer);
                writer.close();

                playlistLocation = playlistFile.getAbsolutePath();
                // Return success notification
                return true;

            } catch (java.io.FileNotFoundException e){
                ServiceWindow.getErrorNotification(resourceBundle.getString("Error"), resourceBundle.getString("ErrorFileNotFound"));
            } catch (java.io.UnsupportedEncodingException e){
                ServiceWindow.getErrorNotification(resourceBundle.getString("Error"), resourceBundle.getString("ErrorOnSaveIncorrectEncoding"));
            } catch (java.io.IOException e){
                ServiceWindow.getErrorNotification(resourceBundle.getString("Error"), resourceBundle.getString("ErrorOnSaveIOProblem"));
            }
            return false;
        }
        else {
            System.out.println("Unable to save: File not selected");
            return false;
        }
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
        if (playlistFile != null) {
            try (Reader reader = new InputStreamReader(new FileInputStream(playlistFile))) {
                JsonStorage jStorage = new Gson().fromJson(reader, JsonStorage.class);
                if (jStorage != null){
                    playlistLocation = playlistFile.getAbsolutePath();
                    //System.out.println("FILE:|"+playlistLocation+"|");
                    return jStorage;
                }
                else
                    return null;
            } catch (java.io.FileNotFoundException e){
                ServiceWindow.getErrorNotification(resourceBundle.getString("Error"), resourceBundle.getString("ErrorFileNotFound"));
            } catch (com.google.gson.JsonSyntaxException e){
                ServiceWindow.getErrorNotification(resourceBundle.getString("Error"), resourceBundle.getString("ErrorOnOpenIncorrectFormatOfFile"));
            } catch (java.io.IOException e){
                ServiceWindow.getErrorNotification(resourceBundle.getString("Error"), resourceBundle.getString("ErrorOnOpenIOProblem"));
            }
            return null;
        }
        else {
            System.out.println("Playlist file not selected");
            return null;
        }
    }

    /** Return path to file opened */
    public static String getPlaylistLocation(){
        return playlistLocation;
    }
}