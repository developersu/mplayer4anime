package mplayer4anime.Playlists;

public class JsonStorage {
    private final String Ver;
    private final String[] Video;
    private final String[] Audio;
    private final String[] Subtitles;
    private final String SubEncoding;

    public String getVer() {return Ver; }
    public String[] getVideo() {return Video; }
    public String[] getAudio() {return Audio; }
    public String[] getSubs() {return Subtitles; }
    public String getSubEncoding() {return SubEncoding; }

    public JsonStorage(String[] videoArr, String[] audioArr, String[] subsArr, String subEncoding){
        Ver = "1";
        Video = videoArr;
        Audio = audioArr;
        Subtitles = subsArr;

        if (subsArr != null && subsArr.length != 0)
            this.SubEncoding = subEncoding;
        else
            this.SubEncoding = "";
    }

}
