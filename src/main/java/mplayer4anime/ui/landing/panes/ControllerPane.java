/*
    Copyright 2018-2026 Dmitry Isaenko

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
package mplayer4anime.ui.landing.panes;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.css.PseudoClass;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import mplayer4anime.AppPreferences;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import static javafx.scene.input.MouseEvent.*;
import static mplayer4anime.ui.landing.panes.ControllerListCell.PreviewMode.*;

public class ControllerPane implements Initializable {
    private static String folderToOpen = System.getProperty("user.home");

    private ResourceBundle resourceBundle;
    private AppPreferences preferences;

    private final ObservableList<File> paneFileList = FXCollections.observableArrayList();
    private PaneType paneType;

    // --- Reorder-drag state
    // Index of the entry being dragged; -1 when not dragging
    private int dragFromIndex = -1;
    // Cell of the entry being dragged
    private ControllerListCell sourceCell;
    // Cell currently showing the insertion marker
    private ControllerListCell markerCell;
    // Notified when the user adds files to this pane manually
    // (via the 'open directory' or 'open files' buttons)
    private Consumer<File[]> onFilesAdded;

    @FXML
    private ListView<File> paneListView;
    @FXML
    private Label paneLbl;
    @FXML
    private Button paneBtnOpenDir;
    @FXML
    private Button paneBtnOpenFiles;
    @FXML
    private Button paneBtnClear;
    @FXML
    private Button paneBtnUp;
    @FXML
    private Button paneBtnDown;
    @FXML
    private Button paneBtnDelete;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;

        paneListView.setCellFactory(_ -> new ControllerListCell(this));
        paneListView.setItems(paneFileList);
        preferences = AppPreferences.get();

        // Show background image only when pane is blank. Fixes issue with background fragments leaking on filled pane
        var populated = PseudoClass.getPseudoClass("populated");
        var isPopulated = Bindings.createBooleanBinding(
                () -> !paneFileList.isEmpty(), paneFileList);
        isPopulated.addListener((_, _, value) ->
                paneListView.pseudoClassStateChanged(populated, value));
        paneListView.pseudoClassStateChanged(populated, isPopulated.get());

        paneBtnOpenDir.setOnAction(_ -> selectDir());
        paneBtnOpenFiles.setOnAction(_ -> selectFile());
        paneBtnClear.setOnAction(_ -> clear());
        paneBtnUp.setOnAction(_ -> up());
        paneBtnDown.setOnAction(_ -> down());
        paneBtnDelete.setOnAction(_ -> delete());
        paneListView.setOnKeyPressed(this::keyPressed);

        // Fallback handlers for drops in the empty area below the last entry
        // (the cells handle drops over themselves and consume the events)
        paneListView.setOnMouseDragOver(_ -> {
            if (isDragging())
                dragOverEmpty();
        });
        paneListView.setOnMouseDragReleased(_ -> {
            if (isDragging())
                dropAtEnd();
        });

        // While the drag is in progress and the pointer leaves the list area
        // (button row, another pane, ...), hide the insertion marker; the
        // dragged entry stays marked and the drag continues when the cursor
        // comes back.
        paneListView.sceneProperty().addListener((_, _, scene) -> {
            if (scene == null)
                return;
            scene.addEventFilter(MOUSE_DRAGGED, e -> {
                if (!isDragging() || isInsideList(e))
                    return; // over the list — cells handle the preview
                clearMarker();
            });

            // The pointer left the window: no further mouse events arrive
            // until it re-enters — hide the insertion marker right away.
            scene.addEventFilter(MOUSE_EXITED, _ -> {
                if (isDragging())
                    clearMarker();
            });

            // If the button is released outside the list area (over the
            // button row, another pane, or outside the window entirely), no
            // MOUSE_DRAG_RELEASED reaches the list view — cancel the drag
            // and clear the preview.
            //
            // The check is by pointer position, not by event target: when the
            // release happens outside the scene, the scene dispatches it to
            // the pressed node (the drag source), so the target would always
            // be inside the list while the pointer is not.
            scene.addEventFilter(MOUSE_RELEASED, e -> {
                if (!isDragging() || isInsideList(e))
                    return;
                resetDrag();
            });

            // If the pointer leaves the window, no events are dispatched at
            // all. If the button was released while it was outside, the
            // release is never delivered and the drag is orphaned; the first
            // event on re-entry reveals it:
            // - MOUSE_ENTERED without the button pressed,
            // - any MOUSE_MOVED (a pressed-button move would be
            //   MOUSE_DRAGGED).
            scene.addEventFilter(MOUSE_ENTERED, e -> {
                if (isDragging() && !e.isPrimaryButtonDown())
                    resetDrag();
            });
            scene.addEventFilter(MOUSE_MOVED, _ -> {
                if (isDragging())
                    resetDrag();
            });
        });
    }

    public void setOnFilesAdded(Consumer<File[]> consumer) {
        onFilesAdded = consumer;
    }

    public ObservableList<File> getPaneFileList() {
        return paneFileList;
    }

    public void setPaneType(PaneType type) {
        paneType = type;
        paneLbl.setText(str(paneType.getResourceKey()));
    }

    public String getSelectedAbsolutePath() {
        var item = paneListView.getSelectionModel().getSelectedItem();
        return item == null ?
                null :
                item.getAbsolutePath();
    }

    public int getSelected() {
        return paneListView.getSelectionModel().getSelectedIndex();
    }

    public void setSelected(int index) {
        paneListView.getSelectionModel().select(index);
    }

    public int getElementsSize() {
        return paneFileList.size();
    }

    public String[] getElements() {
        return paneFileList.stream()
                .map(File::getAbsolutePath)
                .toArray(String[]::new);
    }

    public ReadOnlyIntegerProperty getSelectedIndexProperty() {
        return paneListView.getSelectionModel().selectedIndexProperty();
    }

    // Set files using lists. Used once playlist applies
    public void setFiles(String[] fileLocations) {
        clear();

        if (fileLocations == null || fileLocations.length == 0)
            return;

        show(Arrays.stream(fileLocations)
                .map(File::new)
                .toArray(File[]::new), false);
    }

    private void selectDir() {
        var filesExtensions = preferences.getExtensionsForPane(paneType);

        var dirChooser = new DirectoryChooser();
        dirChooser.setTitle(str("SelectDirectoryTitle"));
        dirChooser.setInitialDirectory(new File(folderToOpen));

        var directoryReceived = dirChooser.showDialog(paneListView.getScene().getWindow());
        if (directoryReceived == null)
            return;

        var files = directoryReceived.listFiles((_, name) ->
                Arrays.stream(filesExtensions).anyMatch(ext -> name.endsWith(ext.substring(1))));

        show(files, true);
    }

    private void selectFile() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle(str("SelectFile"));
        fileChooser.setInitialDirectory(new File(folderToOpen));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(paneType.getDisplayName(), preferences.getExtensionsForPane(paneType)));

        var files = fileChooser.showOpenMultipleDialog(paneListView.getScene().getWindow());
        if (files == null)
            return;

        show(files.toArray(File[]::new), true);
    }

    private void show(File[] files, boolean addedByUser) {
        if (files == null || files.length == 0)
            return;

        Arrays.sort(files);
        // The folder used for MKV is reused in any other pane and vise versa
        folderToOpen = files[0].getParent();

        paneFileList.addAll(files);
        paneListView.getSelectionModel().select(0);

        // Notify the parent controller about video files added by the user,
        // so it can look for external audio/subtitle files next to them
        if (addedByUser && paneType == PaneType.VIDEO && onFilesAdded != null)
            onFilesAdded.accept(files);
    }

    private void clear() {
        paneFileList.clear();
    }

    private void up() {
        var index = paneListView.getSelectionModel().getSelectedIndex();
        if (index > 0)
            move(index, index - 1);
    }

    private void down() {
        var index = paneListView.getSelectionModel().getSelectedIndex();
        if (index + 1 < paneFileList.size())
            move(index, index + 1);
    }

    private void move(int from, int to) {
        paneFileList.add(to, paneFileList.remove(from));
        paneListView.getSelectionModel().select(to);
    }

    private void delete() {
        paneFileList.remove(paneListView.getSelectionModel().getSelectedItem());
    }

    private void keyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE)
            delete();
    }

    private String str(String key) {
        return resourceBundle.getString(key);
    }

    // --- Reorder drag & drop (called by ControllerListCell)

    /**
     * Called by the cell when a drag on its entry starts.
     */
    void beginDrag(int index) {
        dragFromIndex = index;
        sourceCell = cellByIndex(index);
        if (sourceCell != null)
            sourceCell.setPreview(DRAGGING);
    }

    /**
     * @return whether a drag is currently in progress in this pane.
     */
    boolean isDragging() {
        return dragFromIndex >= 0;
    }

    /**
     * Called by the cell while the cursor is over its entry.
     *
     * @param index        the index of the entry under the cursor
     * @param cursorSceneY the cursor Y coordinate in scene coordinates
     */
    void dragOver(int index, double cursorSceneY) {
        showInsertionMarker(insertionPosition(index, cursorSceneY));
    }

    /**
     * Called while the cursor is in the empty area below the last entry.
     */
    void dragOverEmpty() {
        showInsertionMarker(paneFileList.size());
    }

    /**
     * Called by the cell when the drag is released over its entry.
     *
     * @param index        the index of the entry under the cursor
     * @param cursorSceneY the cursor Y coordinate in scene coordinates
     */
    void dropAt(int index, double cursorSceneY) {
        finishDrag(insertionPosition(index, cursorSceneY));
    }

    /**
     * Called when the drag is released in the empty area below the last entry.
     */
    void dropAtEnd() {
        finishDrag(paneFileList.size());
    }

    private void finishDrag(int insertion) {
        var from = dragFromIndex;
        clearPreviews();
        dragFromIndex = -1;

        if (from < 0 || from >= paneFileList.size() ||
                insertion < 0 || insertion > paneFileList.size())
            return;

        // The insertion gap is defined with the dragged entry still in the
        // list, so moving it down by one slot keeps the same gap
        var to = insertion > from ?
                insertion - 1 :
                insertion;
        if (to == from)
            return;

        paneFileList.add(to, paneFileList.remove(from));
        paneListView.getSelectionModel().clearAndSelect(to);
    }

    /**
     * @return the insertion gap (0..size) the cursor points at:
     * <br/>upper half of entry → "insert before it"
     * <br/>lower half of entry → "insert after it"
     */
    private int insertionPosition(int index, double cursorSceneY) {
        var size = paneFileList.size();
        if (index < 0 || index >= size)
            return size;
        return inUpperHalf(cellByIndex(index), cursorSceneY) ? index : index + 1;
    }

    private boolean inUpperHalf(ControllerListCell cell, double cursorSceneY) {
        if (cell == null)
            return false;
        var bounds = cell.getBoundsInLocal();
        var top = cell.localToScene(0, bounds.getMinY());
        var bottom = cell.localToScene(0, bounds.getMaxY());
        return cursorSceneY < (top.getY() + bottom.getY()) / 2;
    }

    /**
     * Shows the live drop preview: a line at the gap where the dragged entry
     * will be inserted (on the cell just below the gap, or below the last
     * cell when the gap is at the end of the list).
     */
    private void showInsertionMarker(int gap) {
        clearMarker();

        var size = paneFileList.size();
        if (size == 0)
            return;

        gap = Math.clamp(gap, 0, size);
        markerCell = cellByIndex(gap < size ? gap : size - 1);
        if (markerCell != null)
            markerCell.setPreview(gap < size ? INSERT_ABOVE : INSERT_BELOW);
    }

    /**
     * Removes the insertion marker, keeping the dragged entry marked.
     */
    private void clearMarker() {
        if (markerCell == null || markerCell.getPreview() == NONE)
            return;
        if (markerCell == sourceCell) // marker drawn on source cell → restore its dragging look
            markerCell.setPreview(DRAGGING);
        else
            markerCell.setPreview(NONE);
        markerCell = null;
    }

    /**
     * Cancels the drag in progress: clears all preview state and leaves the
     * list unchanged.
     */
    private void resetDrag() {
        clearPreviews();
        dragFromIndex = -1;
    }

    /**
     * Removes the marker and the dragging mark of the dragged entry.
     */
    private void clearPreviews() {
        clearMarker();
        if (sourceCell != null) {
            if (sourceCell.getPreview() != NONE)
                sourceCell.setPreview(NONE);
            sourceCell = null;
        }
    }

    /**
     * @return the currently visible cell with the given index, or null.
     */
    private ControllerListCell cellByIndex(int index) {
        for (var node : paneListView.lookupAll(".list-cell")) {
            if (node instanceof ControllerListCell cell && cell.getIndex() == index)
                return cell;
        }
        return null;
    }

    /**
     * @return whether the pointer position carried by the event is inside
     * the list view's bounds in scene coordinates.
     */
    private boolean isInsideList(MouseEvent e) {
        var bounds = paneListView.getBoundsInLocal();
        var topLeft = paneListView.localToScene(bounds.getMinX(), bounds.getMinY());
        var bottomRight = paneListView.localToScene(bounds.getMaxX(), bounds.getMaxY());
        return e.getSceneX() >= Math.min(topLeft.getX(), bottomRight.getX()) &&
               e.getSceneX() <= Math.max(topLeft.getX(), bottomRight.getX()) &&
               e.getSceneY() >= Math.min(topLeft.getY(), bottomRight.getY()) &&
               e.getSceneY() <= Math.max(topLeft.getY(), bottomRight.getY());
    }
}
