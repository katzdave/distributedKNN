package master;

import connectionManager.*;
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

public class MasterProtocol extends Protocol {
  
  //Note: for sockets ConcurrentMap: -1 spot reserved for leader
  
  //contains the IP address and port number of the consumer
  //stores it as ipAddress~portNumberOfServerSocket
  final ConcurrentMap<Integer, String> consumerConnectionData;
  final ConcurrentMap<Integer, String> backupsConnectionData;
  
  String backupString = "b";
  List<String> serverList;
  Set<Integer> notifyList;
  int serverPort;
  Boolean isLeader;
  Boolean connected;
  String currentLeaderInfo;
  String leadIP;
  Integer leadPort;
  
  String connectionData;
  String incomingString;
  String [] messagePieces;
  String [] acceptorMessagePieces;
  String [] disMessagePieces;
  String [] slistMessagePieces;

  public MasterProtocol(int serverPort,
                        String leadIP, 
                        int leadPort) 
          throws IOException, InterruptedException {
    this.consumerConnectionData = new ConcurrentHashMap<>();
    this.backupsConnectionData = new ConcurrentHashMap<>();
    serverList = new Vector<> ();
    this.serverPort = serverPort;
    connected = false;
    this.leadIP = leadIP;
    this.leadPort = leadPort;
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
  void connect() {
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
  
  @Override
  public void processManagerMessages(Message incomingMessage) {
    messagePieces = incomingMessage.message.split(" ");
    System.out.println("Processing message from " 
            + incomingMessage.connectedID + ": " + incomingMessage.message);
    switch(messagePieces[0].charAt(0)) {
      case 'r':
        System.out.println("Received producer request to be paired");
        handlePairClientWithConsumer(incomingMessage.connectedID);
        break;
      case 'q':
        //query logic
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
      default:
        System.err.println("Received invalid message from clientID: "
                + incomingMessage.connectedID);
    }
  }
  
  //handles pairing request from client
  void handlePairClientWithConsumer (int connectedID) {
    //generate an id and iterate through keyset of consumer data
  }
  
  void connectToNextLeader(Boolean resolved, String[] nextLeader) {
    try {
      if (isMyIpPort(nextLeader[0], nextLeader[1])) {
        System.out.println("I'm the leader!");
        isLeader = true;
        updateServerList();
        resolved = true;
      } else {
        connectToLeader(nextLeader[0], nextLeader[1]);
        resolved = true;
      }
      //System.out.println("new backupString: " + backupString);
    } catch (UnknownHostException e) {
      System.err.println("Resolution bug!");
    }
  }
  
  //handled across multiple threads, utilizing Connection class
  @Override
  public void handleDisconnection(int connectedID) {
    sockets.remove(connectedID);
    if (!connected && connectedID == -1)
      return;
    if (connectedID == -1) {
      disMessagePieces= backupString.split(" ", 3);

      //wait until new backupString is updated
      while (disMessagePieces.length < 2)
        disMessagePieces= backupString.split(" ", 3);
      currentLeaderInfo = disMessagePieces[1];
      String[] nextLeader = currentLeaderInfo.split("~");
      if (disMessagePieces.length == 3)
        backupString = "b " + disMessagePieces[2];
      else if (disMessagePieces.length == 2)
        backupString = "b";
      Boolean resolved = false;
      while (!resolved)
        connectToNextLeader(resolved, nextLeader);
      
    } else if (consumerConnectionData.containsKey(connectedID)) {
      consumerConnectionData.remove(connectedID);
      
    } else if (backupsConnectionData.containsKey(connectedID) && isLeader) {
      System.out.println("A backup disconnected");
      serverList.remove(backupsConnectionData.get(connectedID));
      sockets.remove(connectedID);
      updateBackupString();
      sendToAllUpdate();
    } else {
      System.err.println("A producer disconnected");
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
          incomingMessages.put(new Message(numConnections, "r"));
        } catch (InterruptedException ex) {
          System.err.println("interrupted adding message to queue");
        }
        break;
      case 'c': 
        connectionData = (cSocket.getInetAddress().getHostAddress().toString() 
                + "~" + acceptorMessagePieces[1]);
        consumerConnectionData.put(numConnections, connectionData);
        try {
          outgoingMessages.put(new Message(numConnections, backupString));
          //if numConsumers  == maxConsumers
          //partition files and send out
        } catch (InterruptedException ex) {
          System.err.println("interrupted adding message to queue");
        }
        break;
      case 'a':
        //aggregator ID message
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
        System.err.println("Received invalid message!: " + acceptorMessagePieces[0]);
        return false;
    }
    return true;
  }
  
  //may be called across multiple Connection threads
  synchronized void updateBackupString() {
    backupString = "b";
    for (int i = 0; i != serverList.size(); ++i) {
      backupString += (" " + serverList.get(i));
    }
  }
  
  //may be called across multiple Connection threads
  synchronized void sendToAllUpdate() {
    notifyList = backupsConnectionData.keySet();
    sendToNotifyList();
    notifyList = consumerConnectionData.keySet();
    sendToNotifyList();
  }
  
  void sendToNotifyList() {
    for (Integer i : notifyList) {
      try {
        outgoingMessages.put(new Message(i, backupString));
      } catch (InterruptedException ex) {
        System.err.println("Didn't send message!");
      }
    }
  }
  
  void updateServerList() {
    slistMessagePieces = backupString.split(" ");
    serverList.clear();
    for (int i = 1; i != slistMessagePieces.length; ++i) {
      serverList.add(slistMessagePieces[i]);
    }
  }

}