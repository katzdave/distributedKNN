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
public class FeatureVectorContainer {
  static int K = 5;
  static String VectorDelim = "-";
  static int NumWorkers = 4;
  String DELIM;
  
  List<FeatureVector> trainingVectors;
  
  public FeatureVectorContainer(int cores, String delim){
    trainingVectors = new ArrayList<>();
    NumWorkers = cores;
    DELIM = delim;
  }
  
  public FeatureVectorContainer() {
    trainingVectors = new ArrayList<>();
  }
  
  public void setK(int k) {
    K = k;
  }
  
  public String GetKnnAsString(String featureVector){
    //System.err.println(featureVector);
    //System.err.println(trainingVectors.size());
    FeatureVector fv = new FeatureVector(featureVector);
    PriorityBlockingQueue<CategoryDistances> pbq = new PriorityBlockingQueue<>();
    ExecutorService pool = Executors.newFixedThreadPool(NumWorkers);
    String outp = "";
    
    for(FeatureVector elem : trainingVectors){
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
    //System.out.println(outp);
    return outp.substring(0, outp.length()-1);
  }
  
  public void AddVector(FeatureVector fv) {
    trainingVectors.add(fv);
  }
  
  public void addTrainingVectors(String featureVectorBatch){
    String [] tVectors = featureVectorBatch.split(DELIM);
    System.err.println(tVectors.length);
    for (int i = 1; i != tVectors.length; i++) {
      trainingVectors.add(new FeatureVector(tVectors[i]));
    }
  }
}
