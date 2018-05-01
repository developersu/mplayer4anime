package sample;

public interface IMediatorContol {
    void registerMainController(sample.Controller mc);
    void sentUpdates();
}
