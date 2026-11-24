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
package mplayer4anime.player;

import mplayer4anime.BackendEngine;
import mplayer4anime.IPlayer;

import java.util.ResourceBundle;

// Owns the current {@link IPlayer} instance
public class PlayerService {
    private BackendEngine currentEngine;
    private IPlayer player;
    private final ResourceBundle resourceBundle;
    private Runnable onFinishedListener;

    public PlayerService(BackendEngine engine, ResourceBundle resourceBundle) {
        this.currentEngine = engine;
        this.resourceBundle = resourceBundle;
        this.player = engine.createPlayer(resourceBundle);
    }

    public IPlayer getPlayer() {
        return player;
    }

    /**
     * Listener invoked when the loaded file finishes playing on its own. It survives an engine switch
     */
    public void setOnFinishedListener(Runnable onFinished) {
        this.onFinishedListener = onFinished;
        if (player != null)
            player.setOnFinishedListener(onFinished);
    }

    public void dispose() {
        if (player != null) {
            player.dispose();
            player = null;
        }
    }

    public void switchEngine(BackendEngine engine) {
        if (engine == currentEngine)
            return;
        currentEngine = engine;
        if (player != null)
            player.dispose();
        player = engine.createPlayer(resourceBundle);
        if (onFinishedListener != null)
            player.setOnFinishedListener(onFinishedListener);
    }
}
