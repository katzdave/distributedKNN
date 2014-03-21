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

package masterserver;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

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
    isrunning = true;
    sockets = new ConcurrentHashMap<>();
    incomingMessages = new LinkedBlockingQueue<>();
    outgoingMessages = new LinkedBlockingQueue<>();
    this.protocol = protocol;
    protocol.addMembers(isrunning, 
                        sockets, 
                        incomingMessages, 
                        outgoingMessages);
    scAcceptor = new ConnectionAcceptor(serverPort,
                                        isrunning, 
                                        sockets,
                                        incomingMessages,
                                        protocol);
    sMessenger = new ConnectionMessenger(isrunning,
                                         sockets,
                                         outgoingMessages);
  }
  
  /**
   * starts up ConnectionAcceptor
   * continuously attempts to process queued incomingMessages
   */
  public void runManager() {
    scAcceptor.start();
    sMessenger.start();
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
