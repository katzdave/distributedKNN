/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package accumulator;

import connectionManager.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
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
  int masterId = -1;
  
  String masterIp;
  int masterPort;
  int myServerPort;
  int numConsumers;
  int K;
  
  HashMap<Integer, CategoryFinder> categoryFinders;
  ExecutorService pool;
  
  /**
   * @param masterIp
   * @param masterPort
   * @param myPort
   * @param cores
   */
  public AccumulatorProtocol(String masterIp,
                             int masterPort,
                             int myServerPort,
                             int cores) {
    this.masterIp = masterIp;
    this.masterPort = masterPort;
    this.myServerPort = myServerPort;
    categoryFinders = new HashMap<>();
    pool = Executors.newFixedThreadPool(cores);
  }
  
  @Override
  public boolean processAcceptorMessages(int numConnections, 
                                         BufferedReader incomingStream, 
                                         Socket cSocket) {
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
    String[] strings = message.message.split(DELIM);
    if(strings[0].equals("a")){
      Integer id = new Integer(Integer.parseInt(strings[1]));
      if(!categoryFinders.containsKey(id)){
        categoryFinders.put(id, new CategoryFinder(id, numConsumers, K));
      }
      pool.submit(new CategoryFinderWorker(
              strings[2], categoryFinders.get(id),outgoingMessages));
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
      masterSocket = new Socket(masterIp, masterPort);
      BufferedReader masterStream = new BufferedReader(
              new InputStreamReader(masterSocket.getInputStream()));
      DataOutputStream out = 
              new DataOutputStream(masterSocket.getOutputStream());
      out.writeBytes("a " + myServerPort + "\n");
      String s = masterStream.readLine();
      String [] strings = s.split(DELIM);
      numConsumers = Integer.parseInt(strings[1]);
      K = Integer.parseInt(strings[2]);
      
      sockets.put(masterId, masterSocket);
      Connection connection= new Connection(masterId,
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
  
}
