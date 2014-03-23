package masterserver;

import connectionManager.ConnectionManager;
import connectionManager.Protocol;
import java.io.IOException;


/**
 * args[0] - serverPort
 * args[1] - leaderIP
 * args[2] - leaderPort
 * args[3] - maxConsumers
 * args[4] - numK
 * args[5] - Feature Vectors file (training data)
 * @author H
 */
public class MasterServer {
  public static void main(String [] args) {
    if (args.length != 6) {
      System.out.println("Invalid args length!");
      System.exit(1);
    }
    int serverPort = 0, leadPort = 0, maxConsumers = 0, numK = 0;
    String FeatureVectorsFile = args[5];
    try {
      serverPort = Integer.parseInt(args[0]);
      leadPort = Integer.parseInt(args[2]);
      maxConsumers = Integer.parseInt(args[3]);
      numK = Integer.parseInt(args[4]);
    } catch (NumberFormatException e) {
      System.err.println("not a valid number of masters!");
      System.exit(1);
    }
      
    String leadIP = args[1];
    Protocol masterProtocol = null;
    try {   
      try {
        masterProtocol = new MasterProtocol(serverPort, 
                    leadIP, leadPort, maxConsumers, numK, FeatureVectorsFile);
      } catch (InterruptedException e) {
        System.out.println("Exception!");
        System.exit(1);
      }
    } catch (IOException ex) {
      System.out.println("Exception!");
      System.exit(1);
    }
    ConnectionManager masterServer;
    try {
      masterServer = new ConnectionManager(serverPort, 
                                           masterProtocol);
      masterServer.runManager();
    } catch (IOException ex) {
      System.err.println("Could not start up server!");
    } catch (InterruptedException ex) {
      System.err.println("Could not start up server!");
    }
  }
}
