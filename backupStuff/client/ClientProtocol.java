package src.main.java.client;

import connectionManager.Connection;
import connectionManager.Message;
import connectionManager.Protocol;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import knn.FeatureVector;

/**
 *
 * @author H
 */
public class ClientProtocol extends Protocol {

  String DELIM = " ";
  String DELIM2 = "~";
  int masterId = -1;
  
  String masterIp;
  Integer masterPort;
  String testFile;
  Boolean fileTypeFlag; //true = regular textFile, false = listImages
  
  String backupMasterString;
  
  HashMap<Integer, FeatureVector> TestData;
  HashMap<Integer, String> TestResult;
  int amtData = 0;
  int amtResult = 0;
  
  public ClientProtocol(String masterIp, int masterPort,
          String testFile, Boolean fileTypeFlag) {
    this.masterIp = masterIp;
    this.masterPort = masterPort;
    this.testFile = testFile;
    this.fileTypeFlag = fileTypeFlag;
    
  }
  
  void sendMessage(int id, String message) {
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
    switch (msgPieces[0].charAt(0)) {
      case 'e':
        TestData.put(Integer.parseInt(msgPieces[1]),
                new FeatureVector(msgPieces[2]));
        amtData++;
        break;
      case 'q':
        TestResult.put(Integer.parseInt(msgPieces[1]), msgPieces[2]);
        amtResult++;
        if(amtData == amtResult){
          System.out.println("Whipdedoo");
        }
        break;
      default:
        System.err.println("Received invalid message from "
                + message.connectedID+DELIM+message.message);
    }
  }
  
  @Override
  public void handleDisconnection(int connectedID) {
    sockets.remove(connectedID);
    if (connectedID == masterId) {
      handleMasterDisconnection();
    } else {
      System.out.println("bug!!!");
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
        backupMasterString = "m " + disMessagePieces[2];
      else if (disMessagePieces.length == 2)
        backupMasterString = "m";
      connectToMaster();
      if (sockets.containsKey(masterId))
        resolved = true;
    }
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
      sendMessage(masterId, "p");
      (new Thread(new ClientWorker(outgoingMessages,testFile,fileTypeFlag))).start();
    } catch (IOException ex) {
      System.err.println("Couldn't connect to master!");
    }
  }
  
  @Override
  public void connect() {
    connectToMaster();
  }
  
}