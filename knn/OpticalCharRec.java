/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package knn;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Main Test Suite for reading the files and whatnot
 * @author david
 */
public class OpticalCharRec {
  
  public void DoStuff(){
    String trainingFilename = "optdigits.tra";
    String testFilename = "optdigits.tes";
    //String testFilename = "custom.tes";
    
    FeatureContainer fc = new FeatureContainer();
    BufferedReader br = null;
 
    try {
      String sCurrentLine;
      br = new BufferedReader(new FileReader(trainingFilename));
      while ((sCurrentLine = br.readLine()) != null) {
        FeatureVector fv = new FeatureVector();
        String[] splat = sCurrentLine.split(",");
        for(int i=0; i<FeatureVector.NumFeatures; i++){
          fv.Features[i] = new Feature(Double.parseDouble(splat[i]));
        }
        fv.Category = splat[FeatureVector.NumFeatures];
        fc.AddVector(fv);
      }
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
      try {
        if (br != null)br.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    
    int correct = 0;
    int total = 0;
    
    try {
      String sCurrentLine;
      br = new BufferedReader(new FileReader(testFilename));
      while ((sCurrentLine = br.readLine()) != null) {
        FeatureVector fv = new FeatureVector();
        String[] splat = sCurrentLine.split(",");
        for(int i=0; i<FeatureVector.NumFeatures; i++){
          fv.Features[i] = new Feature(Double.parseDouble(splat[i]));
        }
        CategoryFinder cf = new CategoryFinder();
        String s = fc.GetKnnAsString(fv);
        cf.AddListFromString(s);
        String category = cf.GetCategory();
        
        if(category.equals(splat[FeatureVector.NumFeatures])){
          correct++;
        }
        total++;
        System.out.println(correct);
        System.out.println(total);
      }
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
      try {
        if (br != null)br.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }
}
