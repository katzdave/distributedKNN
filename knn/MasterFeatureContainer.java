/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package knn;

import java.util.HashMap;
import java.util.List;

/**
 * What the master uses to store all of the features indexed by ConsumerId
 * @author david
 */
public class MasterFeatureContainer {
  HashMap<Integer, List<FeatureVector>> TrainingData = new HashMap<>();
  int NumConsumers = 0; // Increment this each time a consumer connects?
  
  public void LoadAndDistributeTrainingDataEqually(String filename){
    FeatureVectorLoader fvl = new FeatureVectorLoader();
    List<FeatureVector> allTraining = fvl.FeatureVectorsFromTextFile(filename);
    int amt = allTraining.size()/NumConsumers;
    int rem = allTraining.size()%NumConsumers;
    int last = 0;
    int curr = 0;
    
    for(int i=0; i<NumConsumers; i++){
      curr = last + amt;
      if(rem > 0){
        curr++;
        rem--;
      }
      TrainingData.put(new Integer(i), allTraining.subList(last, curr));
      curr = last;
    }
  }  
}
