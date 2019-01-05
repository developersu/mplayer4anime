package mplayer4anime.IPC;

import javafx.application.Platform;
import mplayer4anime.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

class ServerSocketProvider implements Runnable{

    private ServerSocket serverSocket;
    private Controller controller;

    ServerSocketProvider(Controller mainCntrl, ServerSocket srvSock){
        this.serverSocket = srvSock;
        this.controller = mainCntrl;
    }

    @Override
    public void run() {
        Socket servSockClient;
        try{
            while (!serverSocket.isClosed()){
                servSockClient = serverSocket.accept();
                BufferedReader servInpRdr = new BufferedReader(
                        new InputStreamReader(servSockClient.getInputStream())
                        );

                String line = servInpRdr.readLine();
                // Avoid 'Not on FX application thread' error.
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        controller.setPlaylistAsArgument(line);
                    }
                });

                servSockClient.close();
            }
        }
        catch (IOException ex){
            System.out.println("Socket has been closed.");
        }
    }
}