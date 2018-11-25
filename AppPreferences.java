package mplayer4anime;

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

    /** Return subtitles priority to show
    * @return
     * TRUE - Subtitles tab opens first
     * FALSE - Subtitles tab opens as usual
    */
    public boolean getSubtilesFirst(){
        return preferences.getBoolean("SUBS_TAB_FIRST", false);
    }

    public void setSubtilesFirst(boolean set){
        preferences.putBoolean("SUBS_TAB_FIRST", set);
    }

    // Set option, that tells that we have to save/restore lists on startup
    public void setLoadListsOnStart(boolean set){
        preferences.putBoolean("LOAD_LISTS_ON_START", set);
    }
    // Returns settings for the save/restore lists option
    public boolean getLoadListsOnStart(){
        return preferences.getBoolean("LOAD_LISTS_ON_START", false);   // Don't populate lists by-default
    }

    /** Convert strings array to singls string.
     * Used in:
     * setSubsExtensionsList
     * setSubsEncodingList
     */
    private void storeSingleStringList(String whichList, String[] strArr){
        StringBuilder collect = new StringBuilder();
        for (String e : strArr) {
            collect.append(e);
            collect.append("@@@");  // If there is some idiot who will use @@@ in file extension I'll find him.
        }
        String strToStore = collect.toString();
        preferences.put(whichList, strToStore);
    }

     /** Handle lists of the subtitles extensions selector */
    public void setSubsExtensionsList(String[] subsList){ storeSingleStringList("SUBS_EXTENSIONS_LIST", subsList); }
    public String[] getSubsExtensionsList(){ return preferences.get("SUBS_EXTENSIONS_LIST", ".ass@@@.crt@@@").split("@@@"); }

    /** Handle lists of the subtitles encodings selector */
    public void setSubsEncodingList(String[] subsEncodingList){ storeSingleStringList("SUBS_ENCODINGS_LIST", subsEncodingList); }
    public String[] getSubsEncodingList(){ return preferences.get("SUBS_ENCODINGS_LIST", "default@@@utf8@@@cp1251@@@koi8-r").split("@@@"); }

    /** Save & recover selected by user Subtitles format */
    public void setLastTimeUsedSusExt(String selected){ preferences.put("SUBS_EXT_LAST_TIME_SELECTED", selected); }
    public String  getLastTimeUsedSubsExt(){ return preferences.get("SUBS_EXT_LAST_TIME_SELECTED", ""); }

    /** Save & recover selected by user Subtitles encoding */
    public void setLastTimeUsedSubsEncoding(String selected){ preferences.put("SUBS_ENCODING_LAST_TIME_SELECTED", selected); }
    public String getLastTimeUsedSubsEncoding(){ return preferences.get("SUBS_ENCODING_LAST_TIME_SELECTED", ""); }

    /** Save & recover Full Screen checkbox, if selected */
    public boolean getFullScreenSelected(){
        return preferences.getBoolean("FULL_SCREEN_SELECTED", false);
    }
    public void setFullScreenSelected(boolean set){ preferences.putBoolean("FULL_SCREEN_SELECTED", set); }

    /** Save & recover Subtitles checkbox, if selected */
    public boolean getSubtitlesHideSelected(){
        return preferences.getBoolean("FULL_SUBTITLES_HIDE_SELECTED", false);
    }
    public void setSubtitlesHideSelected(boolean set){ preferences.putBoolean("FULL_SUBTITLES_HIDE_SELECTED", set); }

    /** Return recently opened elements */
    public String[] getRecentPlaylists(){
        String[] recentPlaylists = new String[10];
        for (int i=0; i<10; i++)
            recentPlaylists[i] = preferences.get("RECENT_PLS_" + i, "");
        return recentPlaylists;
    }
    /** Store recently opened elements */
    public void setRecentPlaylists(String[] recentPlaylists){
        if (recentPlaylists != null && recentPlaylists.length > 0) {
            int i;
            for (i = 0; i < recentPlaylists.length && !(i > 10); i++)
                if (recentPlaylists[i] != null && !recentPlaylists[i].isEmpty())
                    preferences.put("RECENT_PLS_" + i, recentPlaylists[i]);
                else
                    preferences.put("RECENT_PLS_" + i, "");
            for (;i<10;i++)                                             // Not needed. Logic may handle recieved String to be less or greater then String[10], but it never happened.
                preferences.put("RECENT_PLS_" + i, "");
        }
    }

}
