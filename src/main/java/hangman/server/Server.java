package hangman.server;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class represents the main thread of the server.
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

    /**
     * Creates a new server object.
     * @param port  Integer value of the used port
     */
    public Server(Integer port) {
        this.port = port;
        this.workerList = new CopyOnWriteArrayList<>();
        this.executorService = Executors.newCachedThreadPool();
        this.randomizer = new Random();
    }

    /**
     * Listens on upcoming connection requests and creates new ServerWorkers for them.
     * Also gives every worker a random word and the associated highscore.
     */
    @Override
    public void run() {
        try {
            new ServerHandler(this).start();
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
                if (e instanceof SocketException) {
                    System.out.println("Socket closed");
                }
                else e.printStackTrace();
            }
        }
    }

    /**
     * Returns a random word of the word list.
     * @return  A random word
     */
    private String randomWord() {
        int rand = randomizer.nextInt(words.size());
        return words.get(rand);
    }

    /**
     * Queries the associated highscore to a given word. If not highscore is found, 999 will be returned.
     * @param word The word of which the highscore is looked for
     * @return  The current highscore or 999
     * @throws IOException
     * @throws URISyntaxException
     */
    private synchronized int highScore(String word) throws IOException, URISyntaxException {
        File tmp = new File(Thread.currentThread().getContextClassLoader().getResource("highscores.txt").toURI());
        List<String> highScore = FileUtils.readLines(tmp, "UTF-8");
        boolean found = false;
        int number = 999;
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

    /**
     * Updates the highscore of a word.
     * @param word      The word, which highscore is updated
     * @param number    The new highscore
     * @throws IOException
     * @throws URISyntaxException
     */
    synchronized void updateHighScore(String word, int number) throws IOException, URISyntaxException {
        File tmp = new File(Thread.currentThread().getContextClassLoader().getResource("highscores.txt").toURI());
        List<String> highScore = FileUtils.readLines(tmp, "UTF-8");
        for (int i = 0; i < highScore.size(); i++) {
            if (highScore.get(i).contains(word)) {
                highScore.set(i, word + "|" + number);
            }
        }
        FileWriter writer = new FileWriter(tmp, false);
        for (String line: highScore) {
            writer.write(line + System.getProperty("line.separator"));
        }
        writer.flush();
        writer.close();
    }

    /**
     * Removes a ServerWorker safely from the workerlist.
     * @param worker Worker that needs to be removed
     */
    void removeWorker(ServerWorker worker) {
        workerList.remove(worker);
    }

    /**
     * Gracefully shutdown worker.
     */
    private void shutdown() {
        listening = false;
        try {
            socket.close();
        } catch (IOException e) {
             e.printStackTrace();
        }
        for (ServerWorker worker: workerList) {
            worker.shutdown();
        }
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            System.err.println("Could not properly stop server.");
            System.err.println("Reason: ");
            e.printStackTrace();
        }
    }

    /**
     * This class is used to execute commands on the server.
     */
    private class ServerHandler extends Thread {
        private Server server;
        private boolean listening;
        private Scanner in;

        /**
         * Creates new handler.
         * @param server Server that is handled
         */
        ServerHandler(Server server) {
            this.server = server;
            this.listening = true;
            this.in = new Scanner(System.in);
        }

        /**
         * Waits for admin input.
         * With `ls` the current workerlist can be queried.
         * With `shutdown` the whole server can be gracefully shutdown.
         */
        @Override
        public void run() {
            while (listening) {
                String cmd = in.next();
                switch (cmd) {
                    case "ls":
                        System.out.println("Active sessions: " + server.workerList.size());
                        break;
                    case "shutdown":
                        server.shutdown();
                        listening = false;
                        System.out.println("Shutting down server...");
                        break;
                }
            }
        }
    }

}
