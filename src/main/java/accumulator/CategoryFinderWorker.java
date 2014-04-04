/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package accumulator;

import connectionManager.Message;
import java.util.concurrent.BlockingQueue;
import knn.CategoryFinder;
import knn.CategoryLikelihoodContainer;

/**
 *
 * @author David
 */
public class CategoryFinderWorker implements Runnable{
  static int MasterId = -2;
  
  String Message;
  CategoryFinder MyCategoryFinder;
  BlockingQueue<Message> Outgoing;

  CategoryFinderWorker(String message,
          CategoryFinder cf,
          BlockingQueue<Message> outgoing,
          Integer masterId){
    Message = message;
    MyCategoryFinder = cf;
    Outgoing = outgoing;
    MasterId = masterId;
  }
  
//  @Override
//  public void run() {
//    MyCategoryFinder.AddListFromString(Message);
//    if(MyCategoryFinder.CheckIfAllMessagesReceived()){
//      String category = MyCategoryFinder.GetCategory();
//      String toSend = "d " + MyCategoryFinder.ID + " " + category;
//      try{
//        Outgoing.put(new Message(MasterId, toSend));
//        //System.err.println("sending to master: " + toSend);
//      }catch(InterruptedException e){
//        System.err.println("Failed to send message to master");
//      }
//    }
//  }
  
  @Override
  public void run() {
    MyCategoryFinder.AddListFromString(Message);
    if(MyCategoryFinder.CheckIfAllMessagesReceived()){
      CategoryLikelihoodContainer clc = MyCategoryFinder.GetCategoryLikelihood();
      String toSend = "d " + MyCategoryFinder.ID + " " + clc.toString();
      try{
        Outgoing.put(new Message(MasterId, toSend));
        //System.err.println("sending to master: " + toSend);
      }catch(InterruptedException e){
        System.err.println("Failed to send message to master");
      }
    }
  }
}
