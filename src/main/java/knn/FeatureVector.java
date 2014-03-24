/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package knn;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Stores the category and information about all features
 * @author David
 */
public class FeatureVector{
  static int NumFeatures = 64;
  static String Delim = ",";
  
  Feature[] Features;
  public String Category;
  
  public FeatureVector(){
    Features = new Feature[NumFeatures];
  }
  
  public FeatureVector(String s){
    Features = new Feature[NumFeatures];
    String[] strings = s.split(Delim);
    for(int i=0; i<NumFeatures; i++){
      Features[i] = new Feature(Double.parseDouble(strings[i]));
    }
    if(strings.length > NumFeatures){
      Category = strings[NumFeatures];
    }else{
      Category = null;
    }
  }
  
  public FeatureVector(BufferedImage img, String s){
    Category = s;
    Features = new Feature[NumFeatures];
    for(int i=0; i<8; i++){
      for(int j=0; j<8; j++){
        int count = 0;
        for(int k=0; k<4; k++){
          for(int h=0; h<4; h++){
            Color c = new Color(img.getRGB(j*4+k, i*4+h));
            if(c.getBlue()+c.getGreen()+c.getRed() < 350){
              count++;
            }
          }
        }
        Features[i*8 + j] = new Feature(count);
      }
    }
  }
  
  public double GetEuclidianDistance(FeatureVector other){
    double dist = 0;
    for(int i=0; i<NumFeatures; i++){
      dist += Math.pow(this.Features[i].Value - other.Features[i].Value,2);
    }
    return Math.pow(dist, .5);
  }
    
  public String toStringShort(){
    String out = "";
    for(int i=0; i<NumFeatures; i++){
      out += Features[i].toString() + Delim;
    }
    return out.substring(0, out.length()-1);
  }
  
  @Override
  public String toString(){
    String out = "";
    for(int i=0; i<NumFeatures; i++){
      out += Features[i].toString() + Delim;
    }
    return out + Category;
  }

  // @Override
  // public boolean equals(Object oth){
  //   FeatureVector other = (FeatureVector) oth;
  //   for(int i=0; i<NumFeatures; i++){
  //     if(this.Features[i] != other.Features[1]){
  //       return false;
  //     }
  //   }
  //   return true;
  // }
}
