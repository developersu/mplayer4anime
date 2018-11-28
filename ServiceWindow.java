package mplayer4anime;

import javafx.scene.control.Alert;
import javafx.scene.layout.Region;

public class ServiceWindow {
    /**
     * Create window with notification
     * */
    public static void getErrorNotification(String title, String body){
        Alert alertBox = new Alert(Alert.AlertType.ERROR);
        alertBox.setTitle(title);
        alertBox.setHeaderText(null);
        alertBox.setContentText(body);
        alertBox.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);        // Java bug workaround for linux
        alertBox.show();
    }
}
