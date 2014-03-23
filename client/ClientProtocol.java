package client;

import connectionManager.Protocol;

/**
 *
 * @author H
 */
public class ClientProtocol extends Protocol {
  
  String masterIp;
  Integer masterPort;
  
  public ClientProtocol(String masterIp, int masterPort) {
    this.masterIp = masterIp;
    this.masterPort = masterPort;
  }
  
}