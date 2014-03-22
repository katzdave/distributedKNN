package masterserver;

import connectionManager.Message;
import connectionManager.Connection;
import connectionManager.Protocol;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.io.File;
import java.util.Collection;

public class MasterProtocol extends Protocol {
  
  String DELIM = " ";
  String DELIM2 = "~";
  int leaderId = -1;
  int defaultAccumulatorId = -2;
  //Note: for sockets ConcurrentMap: -1 spot reserved for leader
  //      -2 spot default for accumulator if none is connected
  
  //contains the IP address and port number of the consumer
  //stores it as ipAddress~portNumberOfServerSocket
  final ConcurrentMap<Integer, String> consumerConnectionData;
  final ConcurrentMap<Integer, String> backupsConnectionData;
  MasterKnnWrapper knn;
  
  String featureVectorsFile;
  Integer numK;
  Integer maxConsumers;
  Integer numConsumers;
  Boolean startedKNN;
  String queryResult;
  
  String consumerString;
  Boolean consumerStringChange;
  
  //information regarding connectiong to leader
  String backupString = "b";
  List<String> serverList;
  int serverPort;
  Boolean isLeader;
  Boolean connected;
  String currentLeaderInfo;
  String leadIp;
  Integer leadPort;
  
  //accumulator information IP~Port
  Integer accumulatorId;
  String accumulatorInfo;
  
  //split strings
  String connectionData;
  String incomingString;
  String [] messagePieces;

  public MasterProtocol(int serverPort,
                        String leadIp, 
                        int leadPort,
                        int maxConsumers,
                        int numK,
                        String featureVectorsFile) 
          throws IOException, InterruptedException {
    super();
    accumulatorId = defaultAccumulatorId;
    startedKNN = false;
    consumerStringChange = true;
    this.numK = numK;
    this.numConsumers = 0;
    this.maxConsumers = maxConsumers;
    this.consumerConnectionData = new ConcurrentHashMap<>();
    this.backupsConnectionData = new ConcurrentHashMap<>();
    serverList = new Vector<> ();
    this.serverPort = serverPort;
    connected = false;
    this.leadIp = leadIp;
    if (leadIp.equals("localhost") || leadIp.equals("::1"))
      this.leadIp = "127.0.0.1";
    this.leadPort = leadPort;
    if (!new File(featureVectorsFile).exists()) {
      System.err.println("Not a valid filename");
      System.exit(1);
    }
    this.featureVectorsFile = featureVectorsFile;
    knn = new MasterKnnWrapper(consumerConnectionData, outgoingMessages);
  }
  
  public void sendMessage(int id, String message) {
    try {
      outgoingMessages.put(new Message(id, message));
    } catch (InterruptedException e) {
      System.out.println("Interrupted sending message to"+id+DELIM+message);
    }
  }
  
  @Override
  public void processManagerMessages(Message incomingMessage) {
    messagePieces = incomingMessage.message.split(DELIM);
    System.out.println("Processing message from " 
            + incomingMessage.connectedID + ": " + incomingMessage.message);
    switch(messagePieces[0].charAt(0)) {
      case 'r':
        System.out.println("Received producer request to be paired");
        handlePairClientWithConsumer(incomingMessage.connectedID,
                messagePieces[1]);
        break;
      case 'q':
        System.out.println("Received query request for id: "+ messagePieces[1]);
        queryResult = knn.GetTestResult(Integer.parseInt(messagePieces[1]));
        if (queryResult == null) {
          sendMessage(incomingMessage.connectedID, "c");
        } else {
          sendMessage(incomingMessage.connectedID, 
                      "q " + messagePieces[1] + DELIM + queryResult);
        }          
        break;
      case 'b':
        connected = true;
        backupString = incomingMessage.message;
        System.out.println("Received updated backupString: " + backupString);
        break;
      case 'l':
        if (sockets.containsKey(leaderId)) {
          try {
            sockets.get(leaderId).close();
          } catch (IOException e) {}
        }
        messagePieces = messagePieces[1].split(DELIM2);
        connectToLeader(messagePieces[0], messagePieces[1]);
        break;
      case 'd':
        knn.AddTestedCategory(
                Integer.parseInt(messagePieces[1]), messagePieces[2]);
        break;
      default:
        System.err.println("Received invalid message from clientID: "
                + incomingMessage.connectedID);
    }
  }
  
  //handles pairing request from client
  void handlePairClientWithConsumer (int connectedID, String featureVector) {
    if (numConsumers == maxConsumers) {
      while (consumerStringChange) {
        consumerString = "";
        Collection<String> consumers = consumerConnectionData.values();
        for (String consumer : consumers) {
          consumerString += (DELIM + consumer);
        }
        consumerStringChange = false;
      }
      sendMessage(connectedID,
                  "y " + knn.AddTestVector(featureVector) + consumerString);
    } else {
      sendMessage(connectedID, "n");
    }
  }
  
  //handled across multiple threads, utilizing Connection class
  @Override
  public void handleDisconnection(int connectedID) {
    sockets.remove(connectedID);
    if (!connected && connectedID == leaderId)
      return;
    if (connectedID == leaderId) {
      handleLeaderDisconnection();
    } else if (connectedID == accumulatorId) {
      accumulatorId = defaultAccumulatorId;
      System.out.println("The accumulator disconnected!");
    } else if (consumerConnectionData.containsKey(connectedID)) {
      --numConsumers;
      consumerStringChange = true;
      consumerConnectionData.remove(connectedID);
    } else if (backupsConnectionData.containsKey(connectedID) && isLeader) {
      handleBackupDisconnection(connectedID);
    } else {
      System.out.println("A producer disconnected");
    }
  }
  
  //first process identifying string, then send out backup list
  //handled in one thread: ConnectionAcceptor class
  @Override
  public boolean processAcceptorMessages(int numConnections, 
                                         BufferedReader incomingStream, 
                                         Socket cSocket) {
    
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
        sendMessage(numConnections, "l " + currentLeaderInfo);
        return false;
      }
    }
    
    switch(acceptorMessagePieces[0].charAt(0)) {
      case 'p':
        sendMessage(numConnections, backupString);
        break;
      case 'c':
        if (numConsumers == maxConsumers) {
          sendMessage(numConnections, "n");
          return false;
        }
        ++numConsumers;
        consumerStringChange = true;
        connectionData = 
                (cSocket.getInetAddress().getHostAddress().toString() 
                + DELIM2 + acceptorMessagePieces[1]);
        consumerConnectionData.put(numConnections, connectionData);
        //send out backup string k and numK
        sendMessage(numConnections, backupString);
        sendMessage(numConnections, "k " + numK);

        //when disconnections occur for consumers or initialization
        if (numConsumers == maxConsumers) {
          Set<Integer> notifyList = consumerConnectionData.keySet();
          if (accumulatorId == defaultAccumulatorId) {
            sendMessage(numConnections, "n");
            return false;
          }
          sendToNotifyList(notifyList, "a " + accumulatorInfo);
          if (!startedKNN) {
            startedKNN = true;
            knn.LoadAndDistributeTrainingDataEqually(featureVectorsFile);
          } else {
            knn.reassignDropped();
          }
        }
        break;
      case 'a':
        //if ID == -2, not yet connected to accumulator
        if (accumulatorId != defaultAccumulatorId) {
          sendMessage(numConnections, "n");
          return false;
        }
        
        accumulatorId = numConnections;
        sendMessage(numConnections, "t " + maxConsumers + DELIM + numK);
        accumulatorInfo = (cSocket.getInetAddress().getHostAddress().toString() 
                + DELIM2 + acceptorMessagePieces[1]);
        
        //when disconnections occur for accumulator
        if (numConsumers == maxConsumers) {
          Set<Integer> notifyList = consumerConnectionData.keySet();
          sendToNotifyList(notifyList, "a " + accumulatorInfo);
        }
        sendMessage(numConnections, backupString);
        break;
      case 's':
        connectionData = (cSocket.getInetAddress().getHostAddress().toString() 
                + DELIM2 + acceptorMessagePieces[1]);
        acceptorMessagePieces = backupString.split(DELIM);
        backupsConnectionData.put(numConnections, connectionData);
        if (!serverList.contains(connectionData)) {
          backupString += (DELIM +connectionData);
          serverList.add(connectionData);
        }
        sendToAllUpdate();
        break;
      default:
        System.err.println("Received invalid message!: "
                + acceptorMessagePieces[0]);
        return false;
    }
    return true;
  }
  
  void handleLeaderDisconnection() {
    String [] disMessagePieces= backupString.split(DELIM, 3);

    //wait until new backupString is updated since it might take time
    while (disMessagePieces.length < 2)
      disMessagePieces= backupString.split(DELIM, 3);
    currentLeaderInfo = disMessagePieces[1];
    String[] nextLeader = currentLeaderInfo.split(DELIM2);
    if (disMessagePieces.length == 3)
      backupString = "b " + disMessagePieces[2];
    else if (disMessagePieces.length == 2)
      backupString = "b";
    Boolean resolved = false;
    while (!resolved) {
      try {
        if (isMyIpPort(nextLeader[0], nextLeader[1])) {
          System.out.println("I'm the leader!");
          isLeader = true;
          String [] slistMessagePieces = backupString.split(DELIM);
          serverList.clear();
          for (int i = 1; i != slistMessagePieces.length; ++i) {
            serverList.add(slistMessagePieces[i]);
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
  }
  
  void handleBackupDisconnection(int connectedID) {
    System.out.println("A backup disconnected");
    serverList.remove(backupsConnectionData.get(connectedID));
    sockets.remove(connectedID);
    backupString = "b";
    for (int i = 0; i != serverList.size(); ++i) {
      backupString += (DELIM + serverList.get(i));
    }
    sendToAllUpdate();
  }
  
  //may be called across multiple Connection threads
  //sends to all backupString update
  synchronized void sendToAllUpdate() {
    Set<Integer> notifyList = backupsConnectionData.keySet();
    sendToNotifyList(notifyList, backupString);
    notifyList = consumerConnectionData.keySet();
    sendToNotifyList(notifyList, backupString);
    //send to accumulator too
    if (accumulatorId != defaultAccumulatorId) {
      sendMessage(accumulatorId, "m" + backupString.substring(1));
    }
  }
  
  void sendToNotifyList(Set<Integer> notifyList, String message) {
    for (Integer i : notifyList) {
        sendMessage(i, message);
    }
  }
 
  boolean isMyIpPort(String ip, String port) throws UnknownHostException {
    return (ip.equals(Inet4Address.getLocalHost().getHostAddress())
            || ip.equals(InetAddress.getLocalHost().getHostAddress())
            || ip.equals("127.0.0.1"))
            && port.equals(""+serverPort);
  }
  
  void connectToLeader(String leaderIP, String leaderPort) {
    System.out.println("Attempting to connect to leader: " 
            + leaderIP + DELIM + leaderPort);
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
      sendMessage(leaderId, "s " + serverPort);
    } catch (IOException ex) {
      System.err.println("Couldn't connect to leader!");
      System.exit(1);
    }
  }
  
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
    }
  }
}