/*
    Copyright 2026 Dmitry Isaenko

    This file is part of mplayer4anime.

    mplayer4anime is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    mplayer4anime is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with mplayer4anime.  If not, see <https://www.gnu.org/licenses/>.
 */
package mplayer4anime.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import mplayer4anime.media.MediaCollection;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import static javafx.stage.Modality.APPLICATION_MODAL;

/**
 * Asks the user which of the found collections of external media files
 * (audio tracks or subtitles) should be added to the application.
 * The dialog content is defined in an FXML file
 * (/MediaSelection/CollectionSelectionLayout.fxml).
 */
public class CollectionSelectionDialog extends Dialog<MediaCollection> {
    private final ResourceBundle resourceBundle;
    // Folder paths common prefix
    private final String commonPrefix;

    public CollectionSelectionDialog(Stage owner,
                                     ResourceBundle resourceBundle,
                                     String mediaKindKey,
                                     List<MediaCollection> collections) {
        this.resourceBundle = resourceBundle;
        this.commonPrefix = commonPathPrefix(collections.stream()
                .map(c -> c.directory().getAbsolutePath())
                .toList());

        var title = resourceBundle.getString("autoImport_ChooseTitle")
                .formatted(resourceBundle.getString(mediaKindKey));

        if (owner != null) {
            initOwner(owner);
            initModality(APPLICATION_MODAL);
        }

        // The window title is set when the dialog is shown
        setOnShown(_ -> {
            var window = getDialogPane().getScene().getWindow();
            if (window instanceof Stage stage)
                stage.setTitle(title);
        });

        setHeaderText(title);

        // OK/Cancel buttons of the standard dialog pane button bar
        var okType = new ButtonType(resourceBundle.getString("OkBtn"),
                ButtonBar.ButtonData.OK_DONE);
        var cancelType = new ButtonType(resourceBundle.getString("CancelBtn"),
                ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(okType, cancelType);

        try {
            var loader = new FXMLLoader(
                    getClass().getResource("/MediaSelection/CollectionSelectionLayout.fxml"));
            loader.setResources(resourceBundle);
            Parent root = loader.load();

            var controller = (CollectionSelectionDialogController) loader.getController();
            controller.setMessage(String.format(
                    resourceBundle.getString("autoImport_ChooseBody"), resourceBundle.getString(mediaKindKey)));
            controller.setDialog(this);
            controller.setCollections(collections);

            setResultConverter(buttonType -> buttonType == okType
                    ? controller.selectedCollection()
                    : null);
            getDialogPane().setContent(root);
        }
        catch (IOException ioException) {
            throw new UncheckedIOException(ioException);
        }
    }

    /**
     * A display entry for a single collection in the dialog list.
     */
    String entryFor(MediaCollection collection) {
        var dir = shortenPath(collection.directory().getAbsolutePath(), commonPrefix);
        var count = collection.files().length;
        var label = collection.label();
        if (label != null && !label.isEmpty())
            return String.format(resourceBundle.getString("autoImport_CollectionEntryLabeled"),
                    dir, label, count);
        return String.format(resourceBundle.getString("autoImport_CollectionEntry"),
                dir, count);
    }

    /**
     * The longest common ancestor directory (compared by whole path
     * segments) of the given paths, or an empty string when the paths have
     * no common part.
     */
    static String commonPathPrefix(List<String> paths) {
        if (paths.size() < 2)
            return "";

        var separator = File.separator;
        var all = paths.stream()
                .map(p -> p.split(Pattern.quote(separator)))
                .toList();
        var first = all.getFirst();
        var common = first.length;
        for (var rest : all.subList(1, all.size())) {
            var count = 1; // the leading empty segment of an absolute path
            while (count < first.length && count < rest.length
                    && first[count].equals(rest[count]))
                count++;
            if (count < common)
                common = count;
            if (common <= 1)
                break;
        }
        if (common <= 1)
            return "";
        // The segment at index 0 is empty, which adds the leading separator
        return String.join(separator, Arrays.copyOfRange(first, 0, common));
    }

    /**
     * Replaces the part of the path that is common to all the collections
     * with "..." to keep the dialog entries short.
     */
    static String shortenPath(String path, String commonPrefix) {
        if (commonPrefix.isEmpty())
            return path;
        if (path.equals(commonPrefix))
            return "...";
        return "..." + path.substring(commonPrefix.length());
    }
}
