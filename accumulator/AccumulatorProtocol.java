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
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
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
  
  String MasterIP;
  int MasterPort;
  int MyPort;
  int NumConsumers;
  int K;
  
  HashMap<Integer, CategoryFinder> CategoryFinders;
  ExecutorService pool;
  
   /**
   * 
   * @param numConnections
   * the ID number assigned to the computer that recently connected
   * numConnections is the key for the sockets ConcurrentMap
   * @param incomingStream
   * Input stream for socket, can be used to get an identifying message
   * @param cSocket
   * socket can be used to get useful information such as IP address
   * @return 
   * returns true if the connection should be established
   * false otherwise
   */
  @Override
  public boolean processAcceptorMessages(int numConnections, 
                                  BufferedReader incomingStream, 
                                  Socket cSocket) {
    throw new UnsupportedOperationException("Not supported yet.");
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
    String[] strings = message.message.split(DELIM);
    if(strings[0].equals("a")){
      Integer id = new Integer(Integer.parseInt(strings[1]));
      if(!CategoryFinders.containsKey(id)){
        CategoryFinders.put(id, new CategoryFinder(id, NumConsumers, K));
      }
      pool.submit(new CategoryFinderWorker(
              strings[2], CategoryFinders.get(id),outgoingMessages));
    }
  }

  /**
   * Handles disconnection logic
   * @param connectedID
   */
  @Override
  public void handleDisconnection(int connectedID) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Connects to master
   */
  @Override
  public void connect() {
    System.out.println("Attempting to connect to master");
    Socket masterSocket;
    try {
      masterSocket = new Socket(MasterIP, MasterPort);
      BufferedReader masterStream = new BufferedReader(
              new InputStreamReader(masterSocket.getInputStream()));
      PrintWriter pr = new PrintWriter(masterSocket.getOutputStream());
      pr.println("a " + MyPort);
      String s = masterStream.readLine();
      String [] strings = s.split(DELIM);
      NumConsumers = Integer.parseInt(strings[1]);
      K = Integer.parseInt(strings[2]);
      
      sockets.put(-1, masterSocket);
      Connection connection= new Connection(-1,
                                            isrunning,
                                            incomingMessages,
                                            masterStream,
                                            this);
      connection.start();
    } catch (IOException ex) {
      System.err.println("Couldn't connect to leader!");
      System.exit(1);
    }
  }
  
  /**
   * Functionality to initialize other classes encapsulated within protocol
   * Protocol will already have access to running, sockets, incomingMessages, and outgoingMessages
   * @param masterIp
   * @param masterPort
   * @param myPort
   * @param cores
   */
  public void initialize(String masterIp, int masterPort, int myPort, int cores) {
    MasterIP = masterIp;
    MasterPort = masterPort;
    MyPort = myPort;
    CategoryFinders = new HashMap<>();
    pool = Executors.newFixedThreadPool(cores);
  }
}
