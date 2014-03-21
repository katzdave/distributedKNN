/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package knn;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * Worker class for multithreading of finding all euclidean distances
 * @author david
 */
public class EuclideanWorker implements Runnable{

  FeatureVector Element;
  FeatureVector TestData;
  PriorityBlockingQueue<CategoryDistances> Pbq;
  
  EuclideanWorker(FeatureVector element,
          FeatureVector testData,
          PriorityBlockingQueue<CategoryDistances> pbq){
    Element = element;
    TestData = testData;
    Pbq = pbq;
  }
  
  @Override
  public void run() {
    Pbq.add(new CategoryDistances(Element.Category,
            Element.GetEuclidianDistance(TestData)));
  }  
}
