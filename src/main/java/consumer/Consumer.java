package consumer;

import connectionManager.ConnectionManager;
import java.io.IOException;

/**
 *
 * @author H
 */
public class Consumer {
  /**
   * args[0] - masterIp
   * args[1] - masterPort
   * @param args 
   */
  public static void main(String [] args) {
   if (args.length != 2) {
      System.out.println("Invalid number of arguments");
      System.exit(1);
    }
    int numCores = Runtime.getRuntime().availableProcessors();
    Integer masterPort = Integer.parseInt(args[1]);
    ConsumerProtocol protocol = 
            new ConsumerProtocol(
                    args[0], 
                    masterPort,
                    numCores);
    ConnectionManager consumer;
    try {
      consumer = new ConnectionManager(protocol);
      consumer.runManager();
    } catch (IOException | InterruptedException ex) {
      System.err.println("Could not start up server!");
    }
  }
}
