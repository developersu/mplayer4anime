package mplayer4anime.ipc;

import mplayer4anime.ui.landing.LandingController;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

// TODO: Rewrite and remove. Or just remove.
public class SingleInstanceHandler{
    private ServerSocket servSocket;
    private final LandingController mainLandingController;
    private final String argument;

    private final int PORT = 65042;

    public SingleInstanceHandler(LandingController mainLandingController, String argument){
        this.mainLandingController = mainLandingController;
        this.argument = argument;

       if (firstInstanceScenario())
           secondInstanceScenario();
    }

    /**
     * Creating client server socket;
     * @return true of failure
     * */
    private boolean firstInstanceScenario(){
        try {
            servSocket = new ServerSocket(PORT, 10, InetAddress.getLocalHost());
            Thread ssp = new Thread(new ServerSocketProvider(mainLandingController, servSocket));
            ssp.start();
            return false;
        }
        catch (IOException ignored){
            return true;
        }
    }
    /**
     * If socked occupied then send arguments to the running instance
     * */
    private void secondInstanceScenario(){
        try {
            if (argument != null){
                // Creating client socket;
                Socket clientSocket = new Socket(InetAddress.getLocalHost(), PORT);
                OutputStream outStream = clientSocket.getOutputStream();
                OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream);
                outStreamWriter.write(argument + "\n");
                outStreamWriter.flush();
                outStream.close();
                clientSocket.close();
            }
            else
                System.out.println("Application is already running.");
        }
        catch (IOException ex){
            ex.printStackTrace();
            System.out.println("Internal issue: unable to create client socket.");
        }

        System.exit(0);
    }

    public void finishWork(){
        try {
            servSocket.close();
        } catch (IOException ignore) {}
    }
}