/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package client;

import connectionManager.Message;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import knn.FeatureVector;
import knn.FeatureVectorLoader;

/**
 *
 * @author David
 */
public class ClientWorker implements Runnable{

  int id = -1;
  BlockingQueue<Message> outgoingMessages;
  String fileName;
  Boolean fileTypeFlag;
  
  public ClientWorker(BlockingQueue<Message> outgoing,
          String filename, boolean flag){
    fileName = filename;
    fileTypeFlag = flag;
    outgoingMessages = outgoing;
  }
  
  @Override
  public void run() {
    if(fileTypeFlag){
      FeatureVectorLoader fvl = new FeatureVectorLoader();
      List<FeatureVector> features = fvl.FeatureVectorsFromTextFile(fileName);
      for(FeatureVector feature : features){
        sendMessage("r " + feature.toString());
      }
    }
    
  }
  
  void sendMessage(String message) {
    try {
      outgoingMessages.put(new Message(id, message));
    } catch (InterruptedException e) {
      System.out.println("Interrupted sending message cliennt worker");
    }
  }
}
