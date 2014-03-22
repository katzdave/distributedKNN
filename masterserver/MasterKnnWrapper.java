/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package masterserver;

import connectionManager.Message;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import knn.FeatureVector;
import knn.FeatureVectorLoader;


/**
 * What the master uses to store all of the features indexed by ConsumerId
 * @author david
 */
public class MasterKnnWrapper {
  HashMap<Integer, List<FeatureVector>> TrainingData = new HashMap<>();
  HashMap<Integer, FeatureVector> TestData = new HashMap<>();
  HashMap<Integer, String> TestResult = new HashMap<>();
  BlockingQueue<Integer> disconnectedList;
  ConcurrentMap<Integer, String> consumerConnectionData;
  BlockingQueue<Message> outgoingMessages;
  
  int LastTestId; // Increment this before every test
  
  public MasterKnnWrapper(ConcurrentMap<Integer, String> consumerConnectionData,
                          BlockingQueue<Message> outgoingMessages) {
    TrainingData = new HashMap<>();
    TestData = new HashMap<>();
    TestResult = new HashMap<>();
    this.outgoingMessages = outgoingMessages;
    this.consumerConnectionData = consumerConnectionData;
    disconnectedList = new LinkedBlockingQueue<>();
    LastTestId = 0;
  }
  
  public void LoadAndDistributeTrainingDataEqually(String filename){
    FeatureVectorLoader fvl = new FeatureVectorLoader();
    List<FeatureVector> allTraining = fvl.FeatureVectorsFromTextFile(filename);
    Integer [] consumerIds = consumerConnectionData.keySet()
            .toArray(new Integer[0]);
    Integer numConsumers = consumerIds.length;
    int amt = allTraining.size()/numConsumers;
    int rem = allTraining.size()%numConsumers;
    int last = 0;
    int curr;
    
    for(int i=0; i<numConsumers; i++){
      curr = last + amt;
      if(rem > 0){
        curr++;
        rem--;
      }
      TrainingData.put(consumerIds[i], allTraining.subList(last, curr));
      curr = last;
    }
    
    List<FeatureVector> vectors;
    for(int i=0; i<numConsumers; i++){
      vectors = TrainingData.get(consumerIds[i]);
      for (FeatureVector current : vectors) {
        try {
          outgoingMessages.put(new Message(consumerIds[i], current.toString()));
        } catch (InterruptedException ex) {
          System.err.println("Interrupted sending of feature vectors");
        }
      }
    }
  }
  
  public void markAsDropped(int id) {
    disconnectedList.add(id);
  }
  
  public boolean reassignDropped() {
    Integer [] consumerIds = consumerConnectionData.keySet()
            .toArray(new Integer[0]);
    Integer currentId;
    List<FeatureVector> currentList;
    for (int i = 0; i != consumerIds.length; ++i) {
      if (!TrainingData.containsKey(consumerIds[i])) {
        if (disconnectedList.isEmpty()) {
          return false;
        } else {
          try {
            currentId = disconnectedList.take();
            currentList = TrainingData.get(currentId);
            TrainingData.put(consumerIds[i], currentList);
            TrainingData.remove(currentId);
            for (FeatureVector v : currentList)
              outgoingMessages.put(new Message(consumerIds[i], v.toString()));
          } catch (InterruptedException e) {
          }
        }
      }
    }
    return true;
  }
  
  public int AddTestVector(String featureVector){
    FeatureVector fv = new FeatureVector(featureVector);
    TestData.put(new Integer(LastTestId), fv);
    TestResult.put(new Integer(LastTestId), null);
    return LastTestId++;
  }
  
  public void AddTestedCategory(int featureVectorId, String category) {
    TestResult.put(featureVectorId, category);
  }
          
  //NOTE: Will return null if test incomplete or invalid
  //Not sure if these cases being indistinguishable is a problem
  public String GetTestResult(int id){
    if(!TestResult.containsKey(id)){
      //INVALID ID
      return null;
    }else{
      return TestResult.get(id);
    }
  }
  
  public boolean ExportCurrentResultsToTextFile(String filename){
    int[][] results = new int[10][10];
    for (int[] row : results)
      Arrays.fill(row, 0);
    for (Map.Entry<Integer, String> entry : TestResult.entrySet()) {
      Integer key = entry.getKey();
      String learnedValue = entry.getValue();
      String knownValue = TestData.get(key).Category;
      if(learnedValue != null && knownValue != null){
        results[Integer.parseInt(knownValue)][Integer.parseInt(learnedValue)]++;
      }
    }
    int totalCorrect = 0;
    int total = 0;
    for(int i=0; i<10; i++){
      for(int j=0; j<10; j++){
        if(i == j){
          totalCorrect += results[i][j];
        }
        total += results[i][j];
      }
    }
    double overallAccuracy = (double)totalCorrect * 100 / (double)total;
    
    try{
      PrintWriter out = new PrintWriter(new FileWriter(filename));
      out.println(String.format(
              "%d Total Results, %d Correct: %.2f% Percent Accuracy",
              total, totalCorrect, overallAccuracy));
      out.println();
      out.println("Confusion matrix: Rows actual, Columns predicted");
      out.println("\t0\t1\t2\t3\t4\t5\t6\t7\t8\t9\t");
      for(int i=0; i<10; i++){
        out.print(i + "\t");
        for(int j=0; j<10; j++){
          out.println(results[i][j] + "\t");
        }
        out.println();
      }
      
    }catch(IOException e){
      return false;
    }
    return true;
  }
}
