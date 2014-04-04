/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package knn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * This class is a short lived class
 * This class will find the top K of many lists of individual box top K
 * This is what the accumulator uses
 * 
 * @author david
 */
public class CategoryFinder {
  static int K = FeatureVectorContainer.K;
  static String VectorDelim = FeatureVectorContainer.VectorDelim;
  PriorityBlockingQueue<CategoryDistances> Matches;
  
  public int ID;
  int NumConsumers;
  AtomicInteger CurrentConsumersCompleted;
  
  public CategoryFinder(int numConsumers){
    Matches = new PriorityBlockingQueue<>();
    CurrentConsumersCompleted = new AtomicInteger(0);
    NumConsumers = numConsumers;
  }
  
  public CategoryFinder(int id, int numConsumers, int k){
    Matches = new PriorityBlockingQueue<>();
    CurrentConsumersCompleted = new AtomicInteger(0);
    NumConsumers = numConsumers;
    ID = id;
    K = k;
  }
  
  public void AddListFromString(String s){
    String[] strings = s.split(VectorDelim);
    for (String string : strings) {
      Matches.add(new CategoryDistances(string));
    }
    CurrentConsumersCompleted.incrementAndGet();
  }
  
  public boolean CheckIfAllMessagesReceived(){
    return NumConsumers == CurrentConsumersCompleted.get();
  }
  
  public String GetCategory(){
    HashMap<String,Integer> counts = new HashMap<>();    
    for(int i=0; i<K; i++){
      CategoryDistances cd = Matches.poll();
      if(cd == null){
        break;
      }
      if(counts.containsKey(cd.Category)){
        Integer tmp = counts.get(cd.Category);
        counts.put(cd.Category, new Integer(tmp.intValue()+1));
      }else{
        counts.put(cd.Category, new Integer(1));
      }
    }
    String cat = "";
    int max = 0;
    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
      int value = entry.getValue().intValue();
      if(value > max){
        max = value;
        cat = entry.getKey();
      }
    }
    return cat;
  }
  
  public CategoryLikelihoodContainer GetCategoryLikelihood(){
    HashMap<String,Integer> counts = new HashMap<>();    
    for(int i=0; i<K; i++){
      CategoryDistances cd = Matches.poll();
      if(cd == null){
        break;
      }
      if(counts.containsKey(cd.Category)){
        Integer tmp = counts.get(cd.Category);
        counts.put(cd.Category, new Integer(tmp.intValue()+1));
      }else{
        counts.put(cd.Category, new Integer(1));
      }
    }
    CategoryLikelihoodContainer clc = new CategoryLikelihoodContainer();
    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
      int value = entry.getValue().intValue();
      clc.AddCategoryLikelihood(new CategoryLikelihood(
              entry.getKey(),((double)value)/K));
    }
    return clc;
  }
}
