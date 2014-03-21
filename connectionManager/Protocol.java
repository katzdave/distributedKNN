package connectionManager;

import java.io.BufferedReader;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author H
 */
public abstract class Protocol {
  
  public Boolean isrunning;
  public ConcurrentMap<Integer, Socket> sockets;
  public BlockingQueue<Message> incomingMessages;
  public BlockingQueue<Message> outgoingMessages;
  
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
  public void processManagerMessages(Message message) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Handles disconnection logic
   * @param connectedID
   */
  public void handleDisconnection(int connectedID) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * should not be overridden
   * passes references of important members to protocol
   * so the protocol will have access to sending and receiving messages
   * @param running
   * @param sockets
   * @param incomingMessages
   * @param outgoingMessages 
   */
  void addMembers(Boolean running, 
                  ConcurrentMap<Integer, Socket> sockets, 
                  BlockingQueue<Message> incomingMessages, 
                  BlockingQueue<Message> outgoingMessages) {
    this.isrunning = running;
    this.sockets = sockets;
    this.incomingMessages = incomingMessages;
    this.outgoingMessages = outgoingMessages;
  }

  /**
   * Functionality for connecting to a network
   * By default does nothing
   */
  public void connect() {
    
  }
  
  /**
   * Functionality to initialize other classes encapsulated within protocol
   * Protocol will already have access to running, sockets, incomingMessages, and outgoingMessages
   */
  public void initialize() {

  }
  
}
