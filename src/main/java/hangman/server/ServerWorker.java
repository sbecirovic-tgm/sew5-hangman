package hangman.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.Arrays;

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
    private String original;
    private char[] word;
    private char[] progress;
    private int highScore;
    private int tries = 0;

    ServerWorker(Socket socket, Server callback, String word, int highScore) {
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

    @Override
    public void run() {
        try {
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            this.out.println((10-tries) + " remaining tries. " + String.valueOf(progress));
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
                        this.listening = false;
                    }
                    if (tries > 9) {
                        this.out.println("You lose! The word was " + String.valueOf(word));
                        this.listening = false;
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
                        this.listening = false;
                    }
                    else {
                        this.out.println("You lose! The word was " + String.valueOf(word));
                        this.listening = false;
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void turn(char guess) {
        for (int i = 0; i < word.length; i++) {
            if (word[i] == guess) {
                progress[i] = word[i];
            }
        }
    }

    private boolean done() {
        return answer(String.valueOf(progress));
    }

    private boolean answer(String guess) {
        return original.toUpperCase().equals(guess);
    }
}
