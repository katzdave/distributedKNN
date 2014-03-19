/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package knn;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
/**
 * Long lived.  Run on each consumer.
 * @author David
 */
public class FeatureContainer {
  static int K = 10;
  static String VectorDelim = "-";
  
  List<FeatureVector> Vectors;
  List<FeatureVector> Knearest;
  
  public FeatureContainer(){
    Vectors = new ArrayList<>();
    Knearest = new ArrayList<>();
  }
  
  public void GetKnn(FeatureVector fv){
    PriorityQueue<FeatureVector> pq = new PriorityQueue<>();
    Knearest.clear();
    for(int i=0; i<Vectors.size(); i++){
      Vectors.get(i).GetEuclidianDistance(fv);
      pq.add(Vectors.get(i));
    }
    
    for(int i=0; i<K; i++){
      FeatureVector feat = pq.poll();
      if(feat == null){
        break;
      }
      Knearest.add(feat);
    }
  }
  
  public String KnearestToString(){
    String s = "";
    for(int i=0; i<Knearest.size(); i++){
      s += Knearest.get(i).toStringLong() + VectorDelim;
    }
    return s.substring(0, s.length()-1);
    //return s.replaceAll(VectorDelim + "$", "");
  }
  
  public void AddVector(FeatureVector fv){
    Vectors.add(fv);
  }
}
