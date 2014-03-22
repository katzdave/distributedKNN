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
  
  //Note: for sockets ConcurrentMap: -1 spot reserved for leader
  
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
  String leadIP;
  Integer leadPort;
  
  //aggregator information IP~Port
  Integer aggregatorID;
  String aggregatorInfo;
  
  //split strings
  String connectionData;
  String incomingString;
  String [] messagePieces;
  String [] acceptorMessagePieces;
  String [] disMessagePieces;
  String [] slistMessagePieces;

  public MasterProtocol(int serverPort,
                        String leadIP, 
                        int leadPort,
                        int maxConsumers,
                        int numK,
                        String featureVectorsFile) 
          throws IOException, InterruptedException {
    aggregatorID = -2;
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
    this.leadIP = leadIP;
    if (leadIP.equals("localhost") || leadIP.equals("::1"))
      this.leadIP = "127.0.0.1";
    this.leadPort = leadPort;
    if (!new File(featureVectorsFile).exists()) {
      System.err.println("Not a valid filename");
      System.exit(1);
    }
    this.featureVectorsFile = featureVectorsFile;
  }
  
  @Override
  public void initialize() {
    knn = new MasterKnnWrapper(consumerConnectionData, outgoingMessages);
  }
  
  @Override
  public void processManagerMessages(Message incomingMessage) {
    messagePieces = incomingMessage.message.split(" ");
    System.out.println("Processing message from " 
            + incomingMessage.connectedID + ": " + incomingMessage.message);
    switch(messagePieces[0].charAt(0)) {
      case 'r':
        System.out.println("Received producer request to be paired");
        handlePairClientWithConsumer(incomingMessage.connectedID,
                messagePieces[1]);
        break;
      case 'q':
        System.out.println("Received query request for id: " + messagePieces[1]);
        queryResult = knn.GetTestResult(Integer.parseInt(messagePieces[1]));
        try {
          if (queryResult == null) {
            outgoingMessages.put(new Message(incomingMessage.connectedID, "c"));
          } else {
           outgoingMessages.put(new Message(incomingMessage.connectedID,
                    "q " + messagePieces[1] + " " + queryResult));
         }          
        } catch (InterruptedException e) {
          System.out.println("Interrupted Query request");
        }
        break;
      case 'b':
        connected = true;
        backupString = incomingMessage.message;
        System.out.println("Received updated backupString: " + backupString);
        break;
      case 'l':
        if (sockets.containsKey(-1)) {
          try {
            sockets.get(-1).close();
          } catch (IOException e) {}
        }
        messagePieces = messagePieces[1].split("~");
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
    try {
      if (numConsumers == maxConsumers) {
        while (consumerStringChange) {
          consumerString = "";
          Collection<String> consumers = consumerConnectionData.values();
          for (String consumer : consumers) {
            consumerString += (" " + consumer);
          }
          consumerStringChange = false;
        }
        outgoingMessages.put(new Message(connectedID,
                "y " + knn.AddTestVector(featureVector) + consumerString));
      } else {
        outgoingMessages.put(new Message(connectedID, "n"));
      }
    } catch (InterruptedException e) {
      System.out.println("Interrupted Query request");
    }
      
  }
  
  //handled across multiple threads, utilizing Connection class
  @Override
  public void handleDisconnection(int connectedID) {
    sockets.remove(connectedID);
    if (!connected && connectedID == -1)
      return;
    if (connectedID == -1) {
      handleLeaderDisconnection();
    } else if (connectedID == aggregatorID) {
      aggregatorID = -2;
      System.out.println("The aggregator disconnected!");
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
    
    try {
      incomingString = incomingStream.readLine();
    } catch (IOException e) {}
    if (incomingString == null) {
      System.out.println("Could not get identifying message!");
      return false;
    }
    
    acceptorMessagePieces = incomingString.split(" ");
    if (!isLeader) {
      //for cases when leader just disconnected; this next in line to be leader
      //let disconnection handling thread make this the leader then resume
      if (acceptorMessagePieces[0].equals("s")) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
      }
      if (!isLeader) {
        try {
          outgoingMessages.put(new Message(numConnections, 
                 "l " + currentLeaderInfo));
        } catch (InterruptedException ex) {}
        return false;
      }
    }
    
    switch(acceptorMessagePieces[0].charAt(0)) {
      case 'p':
        try {
          outgoingMessages.put(new Message(numConnections, backupString));
        } catch (InterruptedException ex) {
          System.err.println("interrupted adding message to queue");
        }
        break;
      case 'c':
        if (numConsumers == maxConsumers) {
          try {
            outgoingMessages.put(new Message(numConnections, "n"));
          } catch (InterruptedException ex) {
            System.err.println("interrupted adding message to queue");
          }
          return false;
        }
        ++numConsumers;
        consumerStringChange = true;
        connectionData = (cSocket.getInetAddress().getHostAddress().toString() 
                + "~" + acceptorMessagePieces[1]);
        consumerConnectionData.put(numConnections, connectionData);
        try {
          //send out backup string k and numK
          outgoingMessages.put(new Message(numConnections, backupString));
          outgoingMessages.put(new Message(numConnections, "k " + numK));
          //if numConsumers  == maxConsumers
        } catch (InterruptedException ex) {
          System.err.println("interrupted adding message to queue");
        }
        //when disconnections occur for consumers or initialization
        if (numConsumers == maxConsumers) {
          Set<Integer> notifyList = consumerConnectionData.keySet();
          if (aggregatorID == -2) {
            try {
              outgoingMessages.put(new Message(numConnections, "n"));
            } catch (InterruptedException ex) {
              System.err.println("interrupted adding message to queue");
            }
            return false;
          }
          sendToNotifyList(notifyList, "a " + aggregatorInfo);
          if (!startedKNN) {
            startedKNN = true;
            knn.LoadAndDistributeTrainingDataEqually(featureVectorsFile);
          } else {
            knn.reassignDropped();
          }
        }
        break;
      case 'a':
        //if ID == -2, not yet connected to aggregator
        if (aggregatorID != -2) {
          try {
            outgoingMessages.put(new Message(numConnections, "n"));
          } catch (InterruptedException ex) {
            System.err.println("interrupted adding message to queue");
          }
          return false;
        }
        aggregatorID = numConnections;
        try {
          outgoingMessages.put(new Message(numConnections, 
                  "t " + maxConsumers + " " + numK));
        } catch (InterruptedException ex) {
          System.err.println("interrupted adding message to queue");
        }
        aggregatorInfo = (cSocket.getInetAddress().getHostAddress().toString() 
                + "~" + acceptorMessagePieces[1]);
        //when disconnections occur for aggregator
        if (numConsumers == maxConsumers) {
          Set<Integer> notifyList = consumerConnectionData.keySet();
          sendToNotifyList(notifyList, "a " + aggregatorInfo);
        }
        break;
      case 's':
        connectionData = (cSocket.getInetAddress().getHostAddress().toString() 
                + "~" + acceptorMessagePieces[1]);
        acceptorMessagePieces = backupString.split(" ");
        backupsConnectionData.put(numConnections, connectionData);
        if (!serverList.contains(connectionData)) {
          backupString += (" " +connectionData);
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
    disMessagePieces= backupString.split(" ", 3);

    //wait until new backupString is updated since it might take time
    while (disMessagePieces.length < 2)
      disMessagePieces= backupString.split(" ", 3);
    currentLeaderInfo = disMessagePieces[1];
    String[] nextLeader = currentLeaderInfo.split("~");
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
          slistMessagePieces = backupString.split(" ");
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
      backupString += (" " + serverList.get(i));
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
    //send to aggregator too
    if (sockets.containsKey(-2)) {
      try {
        outgoingMessages.put(new Message(-2, "m" + backupString.substring(1)));
      } catch (InterruptedException ex) {
        System.err.println("Didn't send message!");
      }
    }
  }
  
  void sendToNotifyList(Set<Integer> notifyList, String message) {
    for (Integer i : notifyList) {
      try {
        outgoingMessages.put(new Message(i, message));
      } catch (InterruptedException ex) {
        System.err.println("Didn't send message!");
      }
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
            + leaderIP + " " + leaderPort);
    Socket leaderSocket;
    try {
      leaderSocket = new Socket(leaderIP, Integer.parseInt(leaderPort));
      currentLeaderInfo = (leaderIP + "~" + leaderPort);
      sockets.put(-1, leaderSocket);
      BufferedReader masterStream = new BufferedReader(
              new InputStreamReader(leaderSocket.getInputStream()));
      Connection connection= new Connection(-1,
                                            isrunning,
                                            incomingMessages,
                                            masterStream,
                                            this);
      connection.start();
      try {
        outgoingMessages.put(new Message(-1, "s " + serverPort));
      } catch (InterruptedException e) {
      }
    } catch (IOException ex) {
      System.err.println("Couldn't connect to leader!");
      System.exit(1);
    }
  }
  
  @Override
  public void connect() {
    try {
      isLeader = isMyIpPort(leadIP, leadPort.toString());  
    } catch (UnknownHostException e) {
      System.err.println("Could not identify leader!");
      System.exit(1);
    }
    if (!isLeader) {
      connectToLeader(leadIP, leadPort.toString());
    }
  }
}