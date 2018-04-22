package sample;

import java.util.prefs.Preferences;

// Rule application settings
public class AppPreferences {

    private Preferences preferences;

    public AppPreferences(){
        preferences = Preferences.userRoot().node("mplayer4anime");
    }

    public void setPath(String path){
        preferences.put("PATH", path);
    }

    public String getPath(){
        return preferences.get("PATH", "mplayer");
    }
}
