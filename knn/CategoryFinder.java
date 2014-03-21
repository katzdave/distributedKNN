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
import java.util.PriorityQueue;

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
  PriorityQueue<CategoryDistances> Matches;
  
  public CategoryFinder(){
    Matches = new PriorityQueue<>();
  }
  
  public void AddListFromString(String s){
    String[] strings = s.split(VectorDelim);
    for (String string : strings) {
      Matches.add(new CategoryDistances(string));
    }
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
}
