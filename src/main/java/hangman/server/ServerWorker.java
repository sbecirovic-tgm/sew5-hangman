package hangman.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author Kacper Urbaniec
 * @version 2019-11-06
 */
public class ServerWorker implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Server callback;
    private boolean listening;

    ServerWorker(Socket socket, Server callback) {
        this.socket = socket;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
