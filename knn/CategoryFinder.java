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
 * 
 * @author david
 */
public class CategoryFinder {
  static int K = FeatureContainer.K;
  static String VectorDelim = FeatureContainer.VectorDelim;
  PriorityQueue<FeatureVector> Matches;
  
  public CategoryFinder(){
    Matches = new PriorityQueue<>();
  }
  
  public void AddListFromString(String s){
    String[] strings = s.split(VectorDelim);
    for (String string : strings) {
      Matches.add(new FeatureVector(string));
    }
  }
  
  public String GetCategory(){
    HashMap<String,Integer> counts = new HashMap<>();    
    for(int i=0; i<K; i++){
      FeatureVector fv = Matches.poll();
      if(fv == null){
        break;
      }
      if(counts.containsKey(fv.Category)){
        Integer tmp = counts.get(fv.Category);
        counts.put(fv.Category, new Integer(tmp.intValue()+1));
      }else{
        counts.put(fv.Category, new Integer(1));
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
