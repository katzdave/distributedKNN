package consumer;

import connectionManager.Connection;
import connectionManager.Message;
import connectionManager.Protocol;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import knn.FeatureVectorContainer;

/**
 *
 * @author H
 */
public class ConsumerProtocol extends Protocol {
  
  String DELIM = " ";
  String DELIM2 = "~";
  int masterId = -1;
  int accumulatorId = -2;
  
  String masterIp;
  Integer masterPort;
  Boolean connectedToMaster;
  
  String accumulatorIp;
  Integer accumulatorPort;
  
  String backupMasterString;
  FeatureVectorContainer knn;
  
  public ConsumerProtocol(String masterIp, 
                          int masterPort,
                          int numCores) {
    this.masterIp = masterIp;
    this.masterPort = masterPort;
    knn = new FeatureVectorContainer(numCores, DELIM);
    connectedToMaster = false;
  }
  
  void sendMessage(int id, String message) {
    //System.out.println(id + DELIM + message);
    try {
      outgoingMessages.put(new Message(id, message));
    } catch (InterruptedException e) {
      System.out.println("Interrupted sending message to"+id+DELIM+message);
    }
  }
  
  @Override
  public boolean processAcceptorMessages(int numConnections, 
                                  BufferedReader incomingStream, 
                                  Socket cSocket) {
    return true;
  }

  @Override
  public void processManagerMessages(Message message) {
    String[] msgPieces = message.message.split(DELIM);
    System.out.println("Got message " + message.message);
    switch (msgPieces[0].charAt(0)) {
      case 'b':
        backupMasterString = message.message;
        break;
      case 'k':
        knn.setK(Integer.parseInt(msgPieces[1]));
        break;
      case 'a':
        //System.out.println("got a " + message.message);
        msgPieces = msgPieces[1].split(DELIM2);
        accumulatorIp = msgPieces[0];
        accumulatorPort = Integer.parseInt(msgPieces[1]);
        connectToAccumulator();
        break;
      case 'l':
        msgPieces = msgPieces[1].split(DELIM2);
        masterIp = msgPieces[0];
        masterPort = Integer.parseInt(msgPieces[1]);
        connectToMaster();
        break;
      case 't':
        //training vectors
        //System.out.println("got!");
        knn.addTrainingVectors(message.message);
        break;
      case 'n':
        connectedToMaster = false;
        try {
          sockets.get(masterId).close();
        } catch (IOException e) {}
        sockets.remove(masterId);
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        if (!sockets.containsKey(masterId))
          connectToMaster();
        break;
      case 'p':
        if (sockets.containsKey(accumulatorId) && knn.isTrained()) {
          sendMessage(accumulatorId, 
                "a"+DELIM+msgPieces[1]+DELIM+knn.GetKnnAsString(msgPieces[2]));
        } else {
          try {
            incomingMessages.put(
                    new Message(message.connectedID, message.message));
          } catch (InterruptedException e) {  
          }
        }
        break;
      case 'y':
        System.out.println("Connected to accumulator at " 
                + accumulatorIp+DELIM+accumulatorPort);
        break;
      default:
        System.err.println("Received invalid message from clientID: "
                + message.connectedID+DELIM+message.message);
    }
  }

  @Override
  public void handleDisconnection(int connectedID) {
    sockets.remove(connectedID);
    if (connectedID == masterId && connectedToMaster) {
      handleMasterDisconnection();
    } else if (connectedID == accumulatorId) {
      handleAccumulatorDisconnection();
    } else if (!connectedToMaster) {
    } else {
      System.out.println("Bug!");
    }
  }
  
  void handleMasterDisconnection() {
    Boolean resolved = false;
    while (!resolved) {
      String [] disMessagePieces= backupMasterString.split(DELIM, 3);
      if (disMessagePieces.length == 1) {
        System.out.println("No more masters!");
        System.exit(1);
      }
      String nextMasterInfo = disMessagePieces[1];
      String[] nextMaster = nextMasterInfo.split(DELIM2);
      masterIp = nextMaster[0];
      masterPort = Integer.parseInt(nextMaster[1]);
      if (disMessagePieces.length == 3)
        backupMasterString = "b " + disMessagePieces[2];
      else if (disMessagePieces.length == 2)
        backupMasterString = "b";
      connectToMaster();
      if (sockets.containsKey(masterId))
        resolved = true;
    }
  }
  
  void handleAccumulatorDisconnection() {
    System.out.println("Accumulator Disconnected");
  }
  
  @Override
  public void connect() {
    connectToMaster();
  }
  
  public void connectToMaster() {
    System.out.println("Attempting to connect to master");
    Socket masterSocket;
    try {
      masterSocket = new Socket(masterIp, masterPort);
      BufferedReader masterStream = new BufferedReader(
              new InputStreamReader(masterSocket.getInputStream()));
      sockets.put(masterId, masterSocket);
      Connection connection= new Connection(masterId,
                                            isrunning,
                                            incomingMessages,
                                            masterStream,
                                            this);
      connection.start();
      sendMessage(masterId, "c");
      connectedToMaster = true;
    } catch (IOException ex) {
      System.err.println("Couldn't connect to master!");
    }
  }
  
  public void connectToAccumulator() {
    System.out.println("Attempting to connect to accumulator");
    Socket accSocket;
    try {
      accSocket = new Socket(accumulatorIp, accumulatorPort);
      BufferedReader masterStream = new BufferedReader(
              new InputStreamReader(accSocket.getInputStream()));
      sockets.put(accumulatorId, accSocket);
      Connection connection= new Connection(accumulatorId,
                                            isrunning,
                                            incomingMessages,
                                            masterStream,
                                            this);
      connection.start();
      sendMessage(accumulatorId, "c");
    } catch (IOException ex) {
      System.err.println("Couldn't connect to Accumulator!");
    }
  }
  
}
