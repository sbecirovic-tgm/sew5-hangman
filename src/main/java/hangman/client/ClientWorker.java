package hangman.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * This class represents a worker that is used to receive and send messages to the hangman server.
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
    private Thread handler;

    /**
     * Creates a new worker for the client.
     * @param host  String representation of the hostname
     * @param port  Integer value of the used port
     */
    public ClientWorker(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Initiates the socket connection and tools for communication.
     * Listens to messages from the server.
     * Also creates a separate thread for listening to user input.
     */
    @Override
    public void run() {
        try {
            this.socket = new Socket(host, port);
            this.listening = true;
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            this.sc = new Scanner(System.in);
            this.handler = new Thread(() -> {
                while (this.listening) {
                    try {
                        if (System.in.available() > 0) this.send();
                    } catch (IOException e) { e.printStackTrace(); }
                }
            });
            this.handler.start();

            while (this.listening) {
                this.received();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads messages from the server and outputs them.
     * If some keywords are found, the worker is prepared for shutdown.
     */
    private void received() {
        String message;
        try {
            message = this.in.readLine();
            System.out.println(message);
            if (message.contains("You lose!") || message.contains("You win!") || message.contains("End!")) {
                shutdown();
            }
        }
        catch (Exception e) {
            System.out.println("Server hung up unexpectedly...");
            shutdown();
        }
    }

    /**
     * Sends user inputs to the server.
     */
    private void send() {
        try {
            out.println(sc.next());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Gracefully shutdown worker
     */
    private void shutdown() {
        listening = false;
        out.close();
        handler.interrupt();
        try {
            in.close();
            socket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
