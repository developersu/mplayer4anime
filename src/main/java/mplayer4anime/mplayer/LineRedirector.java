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
package mplayer4anime.mplayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * A pure pump: forwards lines from the player's stdout/stderr into the
 * shared output stream. It never closes that stream — the owner
 * ({@link MplayerSlave}) closes it exactly once, in {@code dispose()},
 * so the stream is not broken while a command reply is being read.
 */
public class LineRedirector extends Thread {
    private static final Logger log = LoggerFactory.getLogger(LineRedirector.class);

    private final InputStream inStream;
    private final OutputStream outStream;

    LineRedirector(InputStream inStream, OutputStream outStream) {
        this.inStream = inStream;
        this.outStream = outStream;
    }

    public void run() {
        try (var bufferReader = new BufferedReader(new InputStreamReader(inStream))) {
            // Flush after every line: the reader side (MplayerSlave) waits
            // for specific ANS_* lines and must not block until the
            // PrintStream's internal buffer happens to fill up.
            var printStream = new PrintStream(outStream, true);
            String playerOutput;
            while ((playerOutput = bufferReader.readLine()) != null) {
                printStream.println(playerOutput);
            }
        }
        catch (IOException e) {
            log.warn("Error redirecting player output stream", e);
        }
    }
}
