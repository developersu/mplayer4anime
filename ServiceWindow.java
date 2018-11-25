package mplayer4anime;

import javafx.scene.control.Alert;

public class ServiceWindow {
    /**
     * Create window with notification
     * */
    public static void getErrorNotification(String title, String body){
        Alert alertBox = new Alert(Alert.AlertType.ERROR);
        alertBox.setTitle(title);
        alertBox.setHeaderText(null);
        alertBox.setContentText(body);
        alertBox.show();
    }
}
