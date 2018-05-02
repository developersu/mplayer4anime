package mplayer4anime;

public interface IMediatorContol {
    void registerMainController(mplayer4anime.Controller mc);
    void sentUpdates();
}
