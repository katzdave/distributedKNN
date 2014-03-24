/*
 * structure of ConnectionManager:
 *
 *                   ConnectionManager
 *                    /   |   \
 *   ConnectionMessenger  |   ServerLogic (done by main thread)
 *                 ConnectionAcceptor
 *                        |
 * spawns multiple threads (one for each connection)
 *
 */

package connectionManager;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Harrison
 * This is the master server class that pairs up connections
 */
public class ConnectionManager {
  Boolean isrunning;
  final ConcurrentMap<Integer, Socket> sockets;
  final BlockingQueue<Message> incomingMessages;
  final BlockingQueue<Message> outgoingMessages;
  final Protocol protocol;
  ConnectionAcceptor scAcceptor;
  ConnectionMessenger sMessenger;
  
  /**
   * @param serverPort 
   * port that server socket listens on for incoming connections
   * @param protocol
   * protocol that the connection manager uses
   * @throws IOException
   * @throws InterruptedException 
   */
  public ConnectionManager(int serverPort, Protocol protocol) 
          throws IOException, InterruptedException {
    this.protocol = protocol;
    isrunning = protocol.isrunning;
    sockets = protocol.sockets;
    incomingMessages = protocol.incomingMessages;
    outgoingMessages = protocol.outgoingMessages;
    scAcceptor = new ConnectionAcceptor(serverPort,
                                        isrunning, 
                                        sockets,
                                        incomingMessages,
                                        protocol);
    sMessenger = new ConnectionMessenger(isrunning,
                                         sockets,
                                         outgoingMessages);
  }
  
  public ConnectionManager(Protocol protocol) 
          throws IOException, InterruptedException {
    this.protocol = protocol;
    isrunning = protocol.isrunning;
    sockets = protocol.sockets;
    incomingMessages = protocol.incomingMessages;
    outgoingMessages = protocol.outgoingMessages;
    sMessenger = new ConnectionMessenger(isrunning,
                                         sockets,
                                         outgoingMessages);
    scAcceptor = null;
  }
  
  /**
   * starts up ConnectionAcceptor
   * continuously attempts to process queued incomingMessages
   */
  public void runManager() {
    if (scAcceptor != null) {
      scAcceptor.start();
    }
    sMessenger.start();
    protocol.connect();
    System.out.println("Connection Manager running");
    Message message;
    while (isrunning) {
      message = null;
      try {
        message = incomingMessages.take();
      } catch (InterruptedException e) {
      }
      if (message != null) {
        protocol.processManagerMessages(message);
      }
    }
  }
}
