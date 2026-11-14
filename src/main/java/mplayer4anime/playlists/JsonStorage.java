package mplayer4anime.playlists;

import com.google.gson.annotations.SerializedName;

public class JsonStorage {
    @SerializedName("Ver")
    private final String version;
    @SerializedName("Video")
    private final String[] video;
    @SerializedName("Audio")
    private final String[] audio;
    @SerializedName("Subtitles")
    private final String[] subtitles;
    @SerializedName("SubEncoding")
    private final String subEncoding;

    public JsonStorage(String[] videoArray,
                       String[] audioArray,
                       String[] subsArray,
                       String subEncoding) {
        version = "1";
        video = videoArray;
        audio = audioArray;
        subtitles = subsArray;

        if (subsArray != null && subsArray.length != 0)
            this.subEncoding = subEncoding;
        else
            this.subEncoding = "";
    }

    public String getVersion() {
        return version;
    }

    public String[] getVideo() {
        return video;
    }

    public String[] getAudio() {
        return audio;
    }

    public String[] getSubs() {
        return subtitles;
    }

    public String getSubEncoding() {
        return subEncoding;
    }
}
