package hangman.server;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Kacper Urbaniec
 * @version 2019-11-06
 */
public class App {
    public static void main(String[] args) {
        Integer port = 8888;

        if (args.length == 1) {
            if (StringUtils.isNumeric(args[1])) {
                port = Integer.parseInt(args[1]);
            } else {
                System.err.println("The given port is invalid");
                System.exit(1);
            }
        }
        else {
            System.err.println("No port was given!");
            System.exit(1);
        }

        Server server = new Server(port);
        server.start();
    }
}
