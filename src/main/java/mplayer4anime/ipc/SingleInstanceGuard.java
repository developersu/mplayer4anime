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
package mplayer4anime.ipc;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Enforces single-instance behavior for the application.
 *
 * <p>The first launched instance binds a TCP endpoint on a fixed loopback port.
 * A later instance that cannot bind the port connects to the running one and
 * forwards its command-line argument (the playlist path, if any). The running
 * instance receives it and reacts: it loads the playlist, or brings its window
 * to the front when no argument was passed.</p>
 *
 * <p>Message protocol over one connection, two lines:</p>
 * <ol>
 *   <li>protocol marker {@value #PROTOCOL_MARKER}</li>
 *   <li>the playlist path, or an empty line when nothing provided</li>
 * </ol>
 *
 * <p>This class only reports what happened; it never terminates the process.
 * The caller decides the application lifecycle based on the {@link StartResult}.</p>
 */
public final class SingleInstanceGuard implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(SingleInstanceGuard.class);

    // Outcome of trying to become the primary instance.
    public enum StartResult {
        PRIMARY,   // Current process owns the IPC
        SECONDARY, // Another instance is already running → forward argument & exit
        ERROR      // Could not bind the endpoint; could not reach a running instance
    }

    static final String PROTOCOL_MARKER = "M4A1";
    private static final int PORT = 42042;
    private static final int CONNECT_TIMEOUT_MS = 2_000;

    private ServerSocket serverSocket;
    private Thread acceptThread;

    /**
     * Tries to become the primary instance.
     *
     * @param argumentToForward  this launch's argument; forwarded to the running instance
     *                           when this one is secondary; may be {@code null}
     * @param onArgumentReceived invoked on the FX thread when the running instance
     *                           receives a non-empty playlist argument
     * @param onFocusRequest     invoked on the FX thread when the second instance
     *                           was launched without an argument; the primary
     *                           instance should raise its window
     * @return the outcome of the attempt
     */
    public StartResult start(String argumentToForward,
                             Consumer<String> onArgumentReceived,
                             Runnable onFocusRequest) {
        try {
            serverSocket = new ServerSocket(PORT, 10, InetAddress.getLoopbackAddress());
        }
        catch (IOException e) {
            log.info("Port {} is occupied; assuming another instance is running", PORT);
            return forwardToRunningInstance(argumentToForward);
        }

        acceptThread = new Thread(
                new AcceptLoop(serverSocket, onArgumentReceived, onFocusRequest),
                "ipc-single-instance");
        acceptThread.setDaemon(true);
        acceptThread.start();
        return StartResult.PRIMARY;
    }

    @Override
    public void close() {
        try {
            serverSocket.close();
        }
        catch (NullPointerException _) {
        }
        catch (IOException e) {
            log.info("Failed to close IPC server socket", e);
        }
        if (acceptThread != null) {
            // Closing the socket unblocks accept(); interrupt only covers the case
            // where the thread was delayed before reaching it.
            acceptThread.interrupt();
        }
    }

    /**
     * Sends the argument (or an empty message to request focus) to the running instance.
     *
     * @return {@link StartResult#SECONDARY} if the message was delivered,
     * {@link StartResult#ERROR} if no running instance could be reached.
     */
    private static StartResult forwardToRunningInstance(String argument) {
        try (var socket = new Socket()) {
            socket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), PORT), CONNECT_TIMEOUT_MS);
            socket.setSoTimeout(CONNECT_TIMEOUT_MS);
            try (var writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                writer.println(PROTOCOL_MARKER);
                writer.println(argument == null ? "" : argument);
            }
            return StartResult.SECONDARY;
        }
        catch (IOException e) {
            log.warn("Unable to reach the running instance on port " + PORT, e);
            return StartResult.ERROR;
        }
    }

    /**
     * Accept loop of the primary instance. Processes one two-line message per connection.
     */
    private record AcceptLoop(ServerSocket serverSocket,
                              Consumer<String> onArgumentReceived,
                              Runnable onFocusRequest) implements Runnable {
        @Override
        public void run() {
            while (!serverSocket.isClosed()) {
                try (var client = serverSocket.accept()) {
                    handleClient(client);
                }
                catch (IOException e) {
                    if (!serverSocket.isClosed()) {
                        log.info("IPC client connection error", e);
                    }
                }
            }
        }

        private void handleClient(Socket client) throws IOException {
            var reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            var marker = reader.readLine();
            if (PROTOCOL_MARKER.equals(marker)) {
                var argument = reader.readLine();
                var playlist = argument == null ? "" : argument;
                // Switch to the FX thread to avoid 'Not on FX application thread' errors.
                Platform.runLater(() -> {
                    if (playlist.isEmpty())
                        onFocusRequest.run();
                    else
                        onArgumentReceived.accept(playlist);
                });
            }
            else {
                log.warn("Ignoring IPC connection with unknown protocol: {}", marker);
            }
        }
    }
}
