/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package accumulator;

import connectionManager.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import knn.*;

/**
 *
 * @author David
 */
public class AccumulatorProtocol extends Protocol{
  static String DELIM = " ";
  static String DELIM2 = "~";
  int leaderId = -1;
  int masterId = -2;
  
  final ConcurrentMap<Integer, String> backupsConnectionData;
  
  String masterIp;
  int masterPort;
  int myServerPort;
  int numConsumers;
  int maxConsumers;
  int K;
  
  String backupMasterString;
  Boolean isConnectedToMaster;
  
  List<String> accumulatorList;
  Boolean connected;
  String backupAccumulatorString;
  Boolean isLeader;
  String currentLeaderInfo;
  String leadIp;
  Integer leadPort;
  
  HashMap<Integer, CategoryFinder> categoryFinders;
  ExecutorService pool;
  
  /**
   * @param myServerPort
   * @param leaderAccumulatorIP
   * @param leaderPort
   * @param masterIp
   * @param masterPort
   * @param cores
   */
  public AccumulatorProtocol(int myServerPort,
                             String leaderAccumulatorIP,
                             int leaderPort,
                             String masterIp,
                             int masterPort,
                             int cores) {
    backupsConnectionData = new ConcurrentHashMap<>();
    this.masterIp = masterIp;
    this.masterPort = masterPort;
    this.myServerPort = myServerPort;
    categoryFinders = new HashMap<>();
    pool = Executors.newFixedThreadPool(cores);
    
    numConsumers = 0;
    accumulatorList = new Vector<>();
    connected = false;
    leadIp = leaderAccumulatorIP;
    if (leadIp.equals("localhost") || leadIp.equals("::1"))
      this.leadIp = "127.0.0.1";
    leadPort = leaderPort;
    
  }
  
  void sendMessage(int id, String message) {
    try {
      //System.out.println(id + DELIM + message);
      outgoingMessages.put(new Message(id, message));
    } catch (InterruptedException e) {
      System.out.println("Interrupted sending message to"+id+DELIM+message);
    }
  }
  
  @Override
  public boolean processAcceptorMessages(int numConnections, 
                                         BufferedReader incomingStream, 
                                         Socket cSocket) {
    String incomingString = null;
    try { incomingString = incomingStream.readLine(); } catch (IOException e) {}
    
    if (incomingString == null) {
      System.out.println("Could not get identifying message!");
      return false;
    }
    String [] acceptorMessagePieces = incomingString.split(DELIM);
    
    if (!isLeader) {
      //for cases when leader just disconnected; this next in line to be leader
      //let disconnection handling thread make this the leader then resume
      try { Thread.sleep(1000); } catch (InterruptedException e) {}
      if (!isLeader) {
        sendMessage(numConnections, "h " + currentLeaderInfo);
        return false;
      }
    }
    
    //System.err.println("acceptor message got: "+ acceptorMessagePieces[0]);
    switch(acceptorMessagePieces[0].charAt(0)) {
      case 'c':
        if (numConsumers == maxConsumers) {
          sendMessage(numConnections, "n");        
          return false;
        } else {
          sendMessage(numConnections, "y");
        }
        break;
      case 's':
        String connectionData = 
                (cSocket.getInetAddress().getHostAddress().toString() 
                + DELIM2 + acceptorMessagePieces[1]);
        acceptorMessagePieces = backupAccumulatorString.split(DELIM);
        backupsConnectionData.put(numConnections, connectionData);
        if (!accumulatorList.contains(connectionData)) {
          backupAccumulatorString += (DELIM +connectionData);
          accumulatorList.add(connectionData);
        }
        Set<Integer> notifyList = backupsConnectionData.keySet();
        for (Integer i : notifyList)
          sendMessage(i, backupAccumulatorString);
        break;
    }
    return true;
  }
  
  /**
   * Takes a Message object and processes the encapsulated message
   * @param message 
   * the object contains connectedID and message fields
   * connectedID is the id number the sockets are keyed by
   * message is a string
   */
  @Override
  public void processManagerMessages(Message message) {
    String[] msgPieces = message.message.split(DELIM);
    //System.err.println(message.message);
    switch (msgPieces[0].charAt(0)) {
      case 'a':
        //consumer to me
        Integer id = new Integer(Integer.parseInt(msgPieces[1]));
        if(!categoryFinders.containsKey(id)){
          categoryFinders.put(id, new CategoryFinder(id, maxConsumers, K));
        }
        pool.submit(new CategoryFinderWorker(msgPieces[2], 
                categoryFinders.get(id), outgoingMessages, masterId));        
        break;
      case 'h':
        //connect to leader accumulator
        if (sockets.containsKey(leaderId)) {
          try {
            sockets.get(leaderId).close();
          } catch (IOException e) {}
        }
        msgPieces = msgPieces[1].split(DELIM2);
        connectToLeader(msgPieces[0], msgPieces[1]);        
        break;
      case 'l':
        //connect to leader master
        if (sockets.containsKey(masterId)) {
          try {
            sockets.get(masterId).close();
          } catch (IOException e) {}
        }
        msgPieces = msgPieces[1].split(DELIM2);
        masterIp = msgPieces[0];
        masterPort = Integer.parseInt(msgPieces[1]);
        connectToMaster();  
        break; 
      case 'b':
        //backupstring for accumulator
        connected = true;
        backupAccumulatorString = message.message;
        System.out.println("Received updated accumulator backupString: " 
                + backupAccumulatorString);
        break;
      case 'm':
        //master backupString
        backupMasterString = message.message;
        System.out.println("Received updated master backupString: " 
                + backupMasterString);
        break;
      case 'n':
        System.out.println("Master not accepting connection");
        System.exit(1);
        break;
      case 't':
        maxConsumers = Integer.parseInt(msgPieces[1]);
        if (maxConsumers == 0) {
          System.out.println("No consumers assigned... exiting");
          System.exit(1);
        }
        K = Integer.parseInt(msgPieces[2]);
        break;
      default:
        System.err.println("Received invalid message from clientID: "
                + message.connectedID + " " + message.message);
    }
  }

  /**
   * Handles disconnection logic
   * @param connectedID
   */
  @Override
  public void handleDisconnection(int connectedID) {
    sockets.remove(connectedID);
    if (!connected && connectedID == leaderId)
      return;
    if (connectedID == leaderId) {
      handleLeaderDisconnection();
    } else if (connectedID == masterId) {
      handleMasterDisconnection();
    } else if (backupsConnectionData.containsKey(connectedID) && isLeader) {
      handleBackupDisconnection(connectedID);
    } else {
      System.out.println("A consumer disconnected");
    }
  }
  
  void handleLeaderDisconnection() {
    String [] disMessagePieces= backupAccumulatorString.split(DELIM, 3);

    //wait until new backupString is updated since it might take time
    while (disMessagePieces.length < 2)
      disMessagePieces= backupAccumulatorString.split(DELIM, 3);
    currentLeaderInfo = disMessagePieces[1];
    String[] nextLeader = currentLeaderInfo.split(DELIM2);
    if (disMessagePieces.length == 3)
      backupAccumulatorString = "b " + disMessagePieces[2];
    else if (disMessagePieces.length == 2)
      backupAccumulatorString = "b";
    Boolean resolved = false;
    while (!resolved) {
      try {
        if (isMyIpPort(nextLeader[0], nextLeader[1])) {
          System.out.println("I'm the leader!");
          isLeader = true;
          String [] slistMessagePieces = backupAccumulatorString.split(DELIM);
          accumulatorList.clear();
          for (int i = 1; i != slistMessagePieces.length; ++i) {
            accumulatorList.add(slistMessagePieces[i]);
          }
          resolved = true;
        } else {
          connectToLeader(nextLeader[0], nextLeader[1]);
          resolved = true;
        }
      } catch (UnknownHostException e) {
        System.err.println("Resolution bug!");
      }
    }
    //connect to master only if is leader
    if (isLeader) {
      connectToMaster();
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
  
  void handleBackupDisconnection(int connectedID) {
    System.out.println("A backup disconnected");
    accumulatorList.remove(backupsConnectionData.get(connectedID));
    sockets.remove(connectedID);
    backupAccumulatorString = "b";
    for (int i = 0; i != accumulatorList.size(); ++i) {
      backupAccumulatorString += (DELIM + accumulatorList.get(i));
    }
    Set<Integer> notifyList = backupsConnectionData.keySet();
    for (Integer i : notifyList)
      sendMessage(i, backupAccumulatorString);
  }
  
  /**
   * Connects to master
   */
  @Override
  public void connect() {
    try {
      isLeader = isMyIpPort(leadIp, leadPort.toString());  
    } catch (UnknownHostException e) {
      System.err.println("Could not identify leader!");
      System.exit(1);
    }
    if (!isLeader) {
      connectToLeader(leadIp, leadPort.toString());
    } else {
      connectToMaster();
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
      sendMessage(masterId, "a " + myServerPort);
    } catch (IOException ex) {
      System.err.println("Couldn't connect to master!");
    }
  }
  
  boolean isMyIpPort(String ip, String port) throws UnknownHostException {
    return (ip.equals(Inet4Address.getLocalHost().getHostAddress())
            || ip.equals(InetAddress.getLocalHost().getHostAddress())
            || ip.equals("127.0.0.1"))
            && port.equals(""+myServerPort);
  }
  
  public void connectToLeader(String leaderIP, String leaderPort) {
    //System.out.println("Attempting to connect to leader: " + leaderIP + DELIM + leaderPort);
    Socket leaderSocket;
    try {
      leaderSocket = new Socket(leaderIP, Integer.parseInt(leaderPort));
      currentLeaderInfo = (leaderIP + DELIM2 + leaderPort);
      sockets.put(leaderId, leaderSocket);
      BufferedReader masterStream = new BufferedReader(
              new InputStreamReader(leaderSocket.getInputStream()));
      Connection connection= new Connection(leaderId,
                                            isrunning,
                                            incomingMessages,
                                            masterStream,
                                            this);
      connection.start();
      sendMessage(leaderId, "s " + myServerPort);
    } catch (IOException ex) {
      System.err.println("Couldn't connect to leader!");
      System.exit(1);
    }
  }
}
