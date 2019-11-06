package hangman.server;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Kacper Urbaniec
 * @version 2019-11-06
 */
public class Server extends Thread {
    private Integer port;
    private ServerSocket socket;
    private boolean listening;
    private CopyOnWriteArrayList<ServerWorker> workerList;
    private ExecutorService executorService;
    private ArrayList<String> words;
    private Random randomizer;

    public Server(Integer port) {
        this.port = port;
        this.workerList = new CopyOnWriteArrayList<>();
        this.executorService = Executors.newCachedThreadPool();
        this.randomizer = new Random();
    }

    @Override
    public void run() {
        try {
            socket = new ServerSocket(port);
            File tmp = new File(Thread.currentThread().getContextClassLoader().getResource("words.txt").toURI());
            words = (ArrayList<String>) FileUtils.readLines(tmp, "UTF-8");
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        this.listening = true;
        while (listening) {
            try {
                String word = randomWord();
                ServerWorker worker = new ServerWorker(socket.accept(), this, word, highScore(word));
                workerList.add(worker);
                executorService.execute(worker);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String randomWord() {
        int rand = randomizer.nextInt(words.size());
        return words.get(rand);
    }

    private synchronized int highScore(String word) throws IOException, URISyntaxException {
        File tmp = new File(Thread.currentThread().getContextClassLoader().getResource("highscores.txt").toURI());
        List<String> highScore = FileUtils.readLines(tmp, "UTF-8");
        boolean found = false;
        int number = 1000;
        for (String line: highScore) {
            if (line.contains(word)) {
                String raw = line.split("\\|")[1];
                number = Integer.parseInt(raw);
                found = true;
            }
        }
        if (!found) {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(tmp, true)));
            out.println(word + "|" + number);
            out.flush();
            out.close();
        }
        return number;
    }

    public synchronized void updateHighScore(String word, int number) throws IOException, URISyntaxException {
        File tmp = new File(Thread.currentThread().getContextClassLoader().getResource("highscores.txt").toURI());
        List<String> highScore = FileUtils.readLines(tmp, "UTF-8");
        for (int i = 0; i < highScore.size(); i++) {
            if (highScore.get(i).contains(word)) {
                highScore.set(i, word + "|" + number + System.getProperty("line.separator"));
            }
        }
        FileWriter writer = new FileWriter(tmp, false);
        for (String line: highScore) {
            writer.write(line);
        }
        writer.flush();
        writer.close();
    }

}
