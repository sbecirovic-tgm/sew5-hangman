package hangman.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.URISyntaxException;

/**
 * This class represents a worker that is used to receive and send messages to a hangman client.
 * @author Kacper Urbaniec
 * @version 2019-11-06
 */
public class ServerWorker implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Server callback;
    private boolean listening;
    private String original;
    private char[] word;
    private char[] progress;
    private int highScore;
    private int tries = 1;

    /**
     * Creates a new worker for the server.
     * @param socket    Socket is for communication to the client
     * @param callback  Instance of the main server thread
     * @param word      The word the client needs to guess
     * @param highScore The current highscore of the word
     */
    public ServerWorker(Socket socket, Server callback, String word, int highScore) {
        this.socket = socket;
        this.callback = callback;
        this.original = word;
        word = word.toUpperCase();
        this.word = word.toCharArray();
        this.progress = new char[word.length()];
        for (int i = 0; i < word.length(); i++) {
            progress[i] = '_';
        }
        this.highScore = highScore;
    }

    /**
     * Listens to messages from the client and determines their correctness in the context of
     * a hangman game.
     * Also send the progress or end flag to the client.
     */
    @Override
    public void run() {
        try {
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            this.out.println((11-tries) + " remaining tries. " + String.valueOf(progress));
            this.listening = true;
            while (listening) {
                String message = "";
                message = this.in.readLine().toUpperCase();
                if (message.length() == 1) {
                    turn(message.charAt(0));
                    this.tries++;
                    if (done()) {
                        this.out.println("You win!");
                        if (tries < highScore) {
                            callback.updateHighScore(original, tries);
                        }
                        shutdown();
                    }
                    if (tries > 10) {
                        this.out.println("You lose! The word was " + String.valueOf(word));
                        shutdown();
                    }
                    else {
                        this.out.println((10-tries) + " remaining tries. " + String.valueOf(progress));
                    }
                }
                else {
                    if (answer(message)) {
                        this.out.println("You win!");
                        if (tries < highScore) {
                            callback.updateHighScore(original, tries);
                        }
                        shutdown();
                    }
                    else {
                        this.out.println("You lose! The word was " + String.valueOf(word));
                        shutdown();
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            if (e instanceof SocketException) {
                shutdown();
            } else e.printStackTrace();
        }
    }

    /**
     * Checks if a character is part of the searched word. If yes, the progress is updated.
     * @param guess A character that needs to be checked if its part of the searched word
     */
    private void turn(char guess) {
        for (int i = 0; i < word.length; i++) {
            if (word[i] == guess) {
                progress[i] = word[i];
            }
        }
    }

    /**
     * Checks if the searched word was found yet.
     * @return True if yes, else False
     */
    private boolean done() {
        return answer(String.valueOf(progress));
    }

    /**
     * Checks if the searched word equals the given word.
     * @param guess The word that needs to be checked if its equal to the searched word
     * @return True if yes, else False
     */
    private boolean answer(String guess) {
        return original.toUpperCase().equals(guess);
    }

    /**
     * Gracefully shutdown worker
     */
    void shutdown() {
        listening = false;
        out.println("End!");
        out.close();
        try {
            in.close();
            socket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        callback.removeWorker(this);
    }
}
