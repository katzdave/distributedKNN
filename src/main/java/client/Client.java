package client;

import connectionManager.ConnectionManager;
import java.io.IOException;

public class Client {
  /**
   * args[0] - masterIp
   * args[1] - masterPort
   * args[2] - test file
   * args[3] - flag if (txt) textfile
   * @param args 
   */
  public static void main(String [] args) {
   if (args.length != 4) {
      System.out.println("Invalid number of arguments");
      System.exit(1);
    }
    Integer masterPort;
    masterPort = Integer.parseInt(args[1]);
    Boolean flag = false;
    if (args[3].equals("txt"))
      flag = true;
    ClientProtocol protocol = 
            new ClientProtocol(args[0], masterPort, args[2], flag);
    ConnectionManager consumer;
    try {
      consumer = new ConnectionManager(protocol);
      consumer.runManager();
    } catch (IOException ex) {
      System.err.println("Could not start up server!");
    } catch (InterruptedException ex) {
      System.err.println("Could not start up server!");
    }
  }
}
