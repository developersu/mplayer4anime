package mplayer4anime;

import java.io.*;

public class LineRedirecter extends Thread {
    private InputStream inStr;
    private OutputStream outStr;

    LineRedirecter(InputStream in, OutputStream out){
        this.inStr = in;
        this.outStr = out;
    }

    public void run(){
        try {
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inStr));
            PrintStream printStream = new PrintStream(outStr);
            String playerOutput;

            while ((playerOutput = bufferReader.readLine()) != null) {
                printStream.println(playerOutput);
            }

        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
