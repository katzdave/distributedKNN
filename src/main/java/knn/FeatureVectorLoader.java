/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package knn;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import javax.imageio.ImageIO;

/**
 * Lets you load feature vectors from text file or images
 * @author david
 */
public class FeatureVectorLoader {
  static String DELIM = " ";
  
  public List<FeatureVector> FeatureVectorsFromTextFile(String filename){
    List<FeatureVector> vectors = new ArrayList<>();
    try {
      String sCurrentLine;
      BufferedReader br = new BufferedReader(new FileReader(filename));
      while ((sCurrentLine = br.readLine()) != null) {
        vectors.add(new FeatureVector(sCurrentLine));
      }
    } catch (IOException e) {
        System.err.println("Invalid feature vector input file");
        System.exit(1);
    }
    return vectors;
  }
  
  public List<FeatureVector> FeatureVectorsFromImageFileList(String filename){
    List<FeatureVector> vectors = new ArrayList<>();
    try {
      String sCurrentLine;
      BufferedReader br = new BufferedReader(new FileReader(filename));
      while ((sCurrentLine = br.readLine()) != null) {
        String[] split = sCurrentLine.split(DELIM);
        String cat = null;     
        BufferedImage img = null;
        try{
          img = ImageIO.read(new File(split[0]));
        }catch(IOException e){
          System.err.println("Bad image file");
          continue;
        }
        if(split.length == 2){
          cat = split[1];
        }
        vectors.add(new FeatureVector(img, cat));
      }
    } catch (IOException e) {
        System.err.println("Invalid feature vector input file");
        System.exit(1);
    }
    return vectors;    
  }

  public void ExportCurrentResultsToFile(String outFile,
                        HashMap<FeatureVector,Integer> testData,
                        HashMap<Integer,CategoryLikelihoodContainer> res){
    try(PrintWriter out = new PrintWriter(new FileWriter(outFile), true)){
      for(Map.Entry<FeatureVector,Integer> entry : testData.entrySet()){
        Integer key = entry.getValue();
        String knownValue = entry.getKey().Category;
        CategoryLikelihoodContainer clc = res.get(key);
        out.println(entry.getKey().toString() + " " + clc.toString());
      }
    } catch(IOException e){
      System.out.println("invalid file");
    }
    
    
//    int[][] results = new int[10][10];
//    double correct = 0;
//    double total = 0;
//    for (int[] row : results)
//      Arrays.fill(row, 0);
//    for (Map.Entry<FeatureVector, Integer> entry : testData.entrySet()) {
//      Integer key = entry.getValue();
//      String learnedValue = res.get(key);
//      String knownValue = entry.getKey().Category;
//      if(learnedValue.equals(knownValue)){
//        correct = correct + 1;
//      }
//      total = total + 1;
//    }
//
//    System.out.println("Percent acc = " + correct/total);

    //   if(learnedValue != null && knownValue != null){
    //     results[Integer.parseInt(knownValue)][Integer.parseInt(learnedValue)]++;
    //   }
    // }
    // int totalCorrect = 0;
    // int total = 0;
    // for(int i=0; i<10; i++){
    //   for(int j=0; j<10; j++){
    //     if(i == j){
    //       totalCorrect += results[i][j];
    //     }
    //     total += results[i][j];
    //   }
    // }
    // double overallAccuracy = (double)totalCorrect * 100 / (double)total;
    
    // try{
    //   PrintWriter out = new PrintWriter(new FileWriter(outFile), true);
    //   out.write(String.format(
    //           "%d Total Results, %d Correct: %.2f Percent Accuracy\n\n",
    //           total, totalCorrect, overallAccuracy));
    //   out.write("Confusion matrix: Rows actual, Columns predicted\n");
    //   out.write("\t0\t1\t2\t3\t4\t5\t6\t7\t8\t9\t\n");
    //   for(int i=0; i<10; i++){
    //     out.print(i + "\t");
    //     for(int j=0; j<10; j++){
    //       out.write(results[i][j] + "\t");
    //     }
    //     out.write("\n");
    //   }
    //   out.close();
    // }catch(IOException e){
    //   System.out.println("Unable to write to output file");
    // }
  }

  public void ExportCurrentResultsToFile(String outFile, String inFile,
                        HashMap<FeatureVector,Integer> testData,
                        HashMap<Integer,CategoryLikelihoodContainer> res){
//    List<FeatureVector> vectors = FeatureVectorsFromImageFileList(inFile);
//    try{
//      BufferedReader br = new BufferedReader(new FileReader(inFile));
//      PrintWriter out = new PrintWriter(new FileWriter(outFile), true);
//      int i=0;
//      for(FeatureVector vector : vectors){
//        Integer key = testData.get(vector);
//        String value = res.get(key);
//        String fullImgPath = br.readLine();
//        String[] parts = fullImgPath.split("/");
//        String imgname = parts[parts.length-1];
//        out.write("<img src=\"./numbers/" + imgname + "\">= " + value + "<br>\n");
//      }
//      out.close();
//      br.close();
//    }catch(IOException e){
//      System.err.println("Error writing to output file");
//    }
  }
}
