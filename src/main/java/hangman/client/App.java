package hangman.client;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;

/**
 * Starts a client for the hangman game.
 * @author Kacper Urbaniec
 * @version 2019-11-06
 */
public class App {
    public static void main(String[] args) {
        String address = "localhost";
        int port = 8888;
        // validate args
        if (args.length == 2) {
            InetAddressValidator validator = new InetAddressValidator();
            if (args[0].equals("localhost") ||validator.isValid(args[0])) {
                address = args[0];
            } else {
                System.err.println("The given ip-address is invalid");
                System.exit(1);
            }
            if (StringUtils.isNumeric(args[1])) {
                port = Integer.parseInt(args[1]);
            } else {
                System.err.println("The given port is invalid");
                System.exit(1);
            }
        }
        else {
            System.err.println("No address or port was given!");
            System.exit(1);
        }
        // start client
        ClientWorker client = new ClientWorker(address, port);
        client.start();
    }
}
