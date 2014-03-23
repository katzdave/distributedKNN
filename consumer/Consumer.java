package consumer;

import connectionManager.ConnectionManager;
import java.io.IOException;

/**
 *
 * @author H
 */
public class Consumer {
  /**
   * args[0] - myServerPort
   * args[1] - masterIp
   * args[2] - masterPort
   * @param args 
   */
  public static void main(String [] args) {
   if (args.length != 3) {
      System.out.println("Invalid number of arguments");
      System.exit(1);
    }
    int numCores = Runtime.getRuntime().availableProcessors();
    Integer myServerPort, masterPort;
    myServerPort = Integer.parseInt(args[0]);
    masterPort = Integer.parseInt(args[2]);
    ConsumerProtocol protocol = 
            new ConsumerProtocol(
                    myServerPort, 
                    args[1], 
                    masterPort);
    ConnectionManager consumer;
    try {
      consumer = new ConnectionManager(myServerPort, 
                                          protocol);
      consumer.runManager();
    } catch (IOException ex) {
      System.err.println("Could not start up server!");
    } catch (InterruptedException ex) {
      System.err.println("Could not start up server!");
    }
  }
}
