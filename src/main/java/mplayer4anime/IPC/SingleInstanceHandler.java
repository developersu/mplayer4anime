package mplayer4anime.IPC;

import mplayer4anime.Controller;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

// TODO: Rewrite and remove. Or just remove.
public class SingleInstanceHandler implements Runnable{

    private ServerSocket servSock;

    public SingleInstanceHandler(Controller mainCntrl, String argument){
        int PORT = 65042;
        // Creating client server socket;
        try {
            servSock = new ServerSocket(PORT, 10, InetAddress.getLocalHost());
            Thread ssp = new Thread(new ServerSocketProvider(mainCntrl, servSock));
            ssp.start();
        } catch (IOException e) {
            if (argument != null){
                // Creating client socket;
                try {
                    Socket clientSocket = new Socket(InetAddress.getLocalHost(), PORT);
                    OutputStream outStream = clientSocket.getOutputStream();
                    OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream);
                    outStreamWriter.write(argument + "\n");
                    outStreamWriter.flush();
                    outStream.close();
                    clientSocket.close();
                } catch (IOException ex){
                    System.out.println("Internal issue: unable to create client socket.");
                }
            }
            else
                System.out.println("Application is already running.");
            System.exit(0);
        }
    }

    @Override
    public void run() {
        while (! Thread.currentThread().isInterrupted());
        try {
            servSock.close();
        } catch (IOException ignore) {}
    }
}