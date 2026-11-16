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
package mplayer4anime.ui.landing.panes;

import javafx.scene.control.ListCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;

import java.io.File;

import static mplayer4anime.ui.landing.panes.ControllerListCell.PreviewMode.NONE;

class ControllerListCell extends ListCell<File> {

    enum PreviewMode {
        NONE,         // default state
        DRAGGING,     // being dragged
        INSERT_ABOVE, // dragged entry will be inserted above this cell
        INSERT_BELOW  // dragged entry will be inserted below this cell
    }

    private static final String DRAGGING_STYLE = """
            -fx-opacity: 0.85;
            -fx-background-color: rgba(120, 150, 200, 0.75);""";
    private static final String INSERT_ABOVE_STYLE = """
            -fx-background-color: rgba(82, 140, 255, 0.12);
            -fx-border-width: 2 0 0 0;
            -fx-border-color: #4a88ff;""";
    private static final String INSERT_BELOW_STYLE = """
            -fx-background-color: rgba(82, 140, 255, 0.12);
            -fx-border-width: 0 0 2 0;
            -fx-border-color: #4a88ff;""";

    private final ControllerPane pane;
    private PreviewMode preview = NONE;

    ControllerListCell(ControllerPane pane) {
        this.pane = pane;

        setOnDragDetected(this::dragDetected);
        setOnMouseDragOver(this::mouseDragOver);
        setOnMouseDragReleased(this::mouseDragReleased);
    }

    @Override
    public void updateItem(File item, boolean empty) {
        super.updateItem(item, empty);

        // The cell is being reused for another entry: shed any preview state
        // left over from a drag, so it never leaks onto the new entry.
        if (preview != NONE)
            setPreview(NONE);

        if (item == null || empty) {
            setText(null);
            return;
        }
        setText(item.getName());
    }

    private void dragDetected(MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY || getIndex() < 0)
            return;

        pane.beginDrag(getIndex());
        startFullDrag();
    }

    private void mouseDragOver(MouseDragEvent e) {
        if (!pane.isDragging())
            return;

        pane.dragOver(getIndex(), e.getSceneY());
        e.consume();
    }

    private void mouseDragReleased(MouseDragEvent e) {
        if (!pane.isDragging())
            return;

        pane.dropAt(getIndex(), e.getSceneY());
        e.consume();
    }

    /**
     * @return the preview state currently applied to cell.
     */
    PreviewMode getPreview() {
        return preview;
    }

    /**
     * Set drag preview on cell.
     * @param preview the preview state to apply on cell
     */
    void setPreview(PreviewMode preview) {
        this.preview = preview;
        setOpacity(1.0);

        switch (preview) {
            case NONE -> setStyle(null);
            case DRAGGING -> setStyle(DRAGGING_STYLE);
            case INSERT_ABOVE -> setStyle(INSERT_ABOVE_STYLE);
            case INSERT_BELOW -> setStyle(INSERT_BELOW_STYLE);
        }
    }
}
