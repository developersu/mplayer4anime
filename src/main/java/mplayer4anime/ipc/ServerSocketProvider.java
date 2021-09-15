/*
    Copyright 2018-2021 Dmitry Isaenko

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
package mplayer4anime.ipc;

import javafx.application.Platform;
import mplayer4anime.ui.landing.LandingController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

class ServerSocketProvider implements Runnable{

    private final ServerSocket serverSocket;
    private final LandingController landingController;

    ServerSocketProvider(LandingController mainLandingController, ServerSocket srvSock){
        this.serverSocket = srvSock;
        this.landingController = mainLandingController;
    }

    @Override
    public void run() {
        try{
            Socket servSockClient;
            while (!serverSocket.isClosed()){
                servSockClient = serverSocket.accept();
                BufferedReader servInpRdr = new BufferedReader(
                        new InputStreamReader(servSockClient.getInputStream()));

                String line = servInpRdr.readLine();
                // Avoid 'Not on FX application thread' error.
                Platform.runLater(() -> landingController.setPlaylistAsArgument(line));

                servSockClient.close();
            }
        }
        catch (IOException ignore){}
    }
}