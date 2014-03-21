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
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Lets you load feature vectors from text file or images
 * @author david
 */
public class FeatureVectorLoader {
  static String DELIM = " ";
  
  List<FeatureVector> FeatureVectorsFromTextFile(String filename){
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
  
  List<FeatureVector> FeatureVectorsFromImageFileList(String filename){
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
}
