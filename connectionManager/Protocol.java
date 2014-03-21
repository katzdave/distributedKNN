/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package masterserver;

import java.io.BufferedReader;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author H
 */
public abstract class Protocol {
  
  Boolean isrunning;
  ConcurrentMap<Integer, Socket> sockets;
  BlockingQueue<Message> incomingMessages;
  BlockingQueue<Message> outgoingMessages;
  
  boolean processAcceptorMessages(int numConnections, 
                                  BufferedReader incomingStream, 
                                  Socket cSocket) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  void processManagerMessages(Message message) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  void handleDisconnection(int connectedID) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  void addMembers(Boolean running, 
                  ConcurrentMap<Integer, Socket> sockets, 
                  BlockingQueue<Message> incomingMessages, 
                  BlockingQueue<Message> outgoingMessages) {
    this.isrunning = running;
    this.sockets = sockets;
    this.incomingMessages = incomingMessages;
    this.outgoingMessages = outgoingMessages;
  }
  
}
