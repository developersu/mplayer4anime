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

    /* Return subtitles priority to show
    * TRUE - Subtitles tab opens first
    *  FALSE - Subtitles tab opens as usual
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

    /** Convert strings array to singls string. Used in:
     *                                                  setSubsExtensionsList
     *                                                  setSubsCodepageList
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

    /** Handle lists of the subtitles codepage selector */
    public void setSubsCodepageList(String[] subsCodepageList){ storeSingleStringList("SUBS_CODEPAGE_LIST", subsCodepageList); }
    public String[] getSubsCodepageList(){ return preferences.get("SUBS_CODEPAGE_LIST", "default@@@utf8@@@cp1251@@@koi8-r").split("@@@"); }

    // Save & recover selected by user Subtitles format
    public void setLastTimeUsedSusExt(String selected){ preferences.put("SUBS_EXT_LAST_TIME_SELECTED", selected); }
    public String  getLastTimeUsedSubsExt(){ return preferences.get("SUBS_EXT_LAST_TIME_SELECTED", ""); }

    // Save & recover selected by user Subtitles codepage
    public void setLastTimeUsedSubsCodepage(String selected){ preferences.put("SUBS_CODEPAGE_LAST_TIME_SELECTED", selected); }
    public String  getLastTimeUsedSubsCodepage(){ return preferences.get("SUBS_CODEPAGE_LAST_TIME_SELECTED", ""); }

    // Save & recover Full Screen checkbox, if selected
    public boolean getFullScreenSelected(){
        return preferences.getBoolean("FULL_SCREEN_SELECTED", false);
    }
    public void setFullScreenSelected(boolean set){ preferences.putBoolean("FULL_SCREEN_SELECTED", set); }

    /** Lists managment */
    // Return lists itself of the latest opened folders (used only in Controller.class)
    private String getList(String whichList){
        return preferences.get(whichList, "");
    }
    // Save lists itself of the latest opened folders (used only in Controller.class)
    private void setList(String whichList, String value){
        preferences.put(whichList, value);
    }

    public String getListMKV(){ return getList("MKV"); }
    public String getListMKA(){ return getList("MKA"); }
    public String getListSUB(){ return getList("SUB"); }

    public void setListMKV(String value){setList("MKV", value);}
    public void setListMKA(String value){setList("MKA", value);}
    public void setListSUB(String value){setList("SUB", value);}
}
