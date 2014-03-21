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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Long lived.  Run on each consumer.
 * @author David
 */
public class FeatureContainer {
  static int K = 10;
  static String VectorDelim = "-";
  static int NumWorkers = 4;
  
  List<FeatureVector> Vectors;
  
  public FeatureContainer(){
    Vectors = new ArrayList<>();
  }
    
  public String GetKnnAsString(FeatureVector fv){
    PriorityBlockingQueue<CategoryDistances> pbq = new PriorityBlockingQueue<>();
    ExecutorService pool = Executors.newFixedThreadPool(NumWorkers);
    String outp = "";
    
    for(FeatureVector elem : Vectors){
      pool.execute(new EuclideanWorker(elem, fv, pbq));
    }
    pool.shutdown();
    while(!pool.isTerminated()){
      ;
    }
    
    for(int i=0; i<K; i++){
      CategoryDistances cd = pbq.poll();
      if(cd == null){
        break;
      }
      outp += cd.toString() + VectorDelim;
    }
    
    return outp.substring(0, outp.length()-1);
  }
  
  public void AddVector(FeatureVector fv){
    Vectors.add(fv);
  }
}
