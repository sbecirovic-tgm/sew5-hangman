package hangman.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author Kacper Urbaniec
 * @version 2019-11-06
 */
public class ClientWorker extends Thread {
    private String host;
    private Integer port;
    private Socket socket;
    private boolean listening;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner sc;

    public ClientWorker(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            this.socket = new Socket(host, port);
            this.listening = true;
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            this.sc = new Scanner(System.in);
            while (listening) {
                this.received();
                this.send();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void received() {

    }

    private void send() {

    }
}
