package mplayer4anime;

public class MediatorControl implements IMediatorContol{
    private Controller mainController;

    @Override
    public void registerMainController(Controller mc) {
        mainController = mc;
    }

    @Override
    public void sentUpdates() {
        mainController.updateAfterSettingsChanged();
    }

    private MediatorControl() {}

    public static MediatorControl getInstance(){
        return MediatorControlHold.INSTANCE;
    }

    private static class MediatorControlHold {
        private static final MediatorControl INSTANCE = new MediatorControl();
    }
}
