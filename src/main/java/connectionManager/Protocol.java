package connectionManager;

import java.io.BufferedReader;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author H
 */
public abstract class Protocol {
  
  public Boolean isrunning;
  public ConcurrentMap<Integer, Socket> sockets;
  public BlockingQueue<Message> incomingMessages;
  public BlockingQueue<Message> outgoingMessages;
  
  //must call super(); in derived class! or else it will not work
  public Protocol() {
    isrunning = true;
    sockets = new ConcurrentHashMap<>();
    incomingMessages = new LinkedBlockingQueue<>();
    outgoingMessages = new LinkedBlockingQueue<>();
  }

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
   * by default return true
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
   * Functionality for connecting to a network
   * By default does nothing
   */
  public void connect() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
}
