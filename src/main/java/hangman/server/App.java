package hangman.server;

import org.apache.commons.lang3.StringUtils;

/**
 * Starts the server for the hangman game
 * @author Kacper Urbaniec
 * @version 2019-11-06
 */
public class App {
    public static void main(String[] args) {
        int port = 8888;
        // validate arg
        if (args.length == 1) {
            if (StringUtils.isNumeric(args[0])) {
                port = Integer.parseInt(args[0]);
            } else {
                System.err.println("The given port is invalid");
                System.exit(1);
            }
        }
        else {
            System.err.println("No port was given!");
            System.exit(1);
        }
        // start server
        Server server = new Server(port);
        server.start();
    }
}
