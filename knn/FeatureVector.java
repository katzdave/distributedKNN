/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package knn;

/**
 *
 * @author David
 */
public class FeatureVector{
  static int NumFeatures = 64;
  static String Delim = "_";
  static String Delim2 = "!";
  
  Feature[] Features;
  String Category;
  double CurrDistance;
  
  public FeatureVector(){
    Features = new Feature[NumFeatures];
  }
  
  public FeatureVector(String s){
    Features = new Feature[NumFeatures];
    String[] strings = s.split(Delim2);
    if(strings.length == 1){
      String[] strings2 = strings[0].split(Delim);
      if(strings2.length != NumFeatures){
        System.err.println("Invalid string 1");
        System.exit(1);
      }else{
        for(int i=0; i<NumFeatures; i++){
          Features[i] = new Feature(Double.parseDouble(strings2[i]));
        }
      }
    }else if(strings.length == 2){
      Category = strings[0];
      String[] strings2 = strings[1].split(Delim);
      if(strings2.length != NumFeatures + 1){
        System.err.println("Invalid string 2");
        System.exit(1);
      }else{
        for(int i=0; i<NumFeatures; i++){
          Features[i] = new Feature(Double.parseDouble(strings2[i]));
        }
      }
    }else{
      System.err.println("Invalid string 3");
      System.exit(1);
    }
  }
  
  public double GetEuclidianDistance(FeatureVector other){
    double dist = 0;
    for(int i=0; i<NumFeatures; i++){
      dist += Math.pow(this.Features[i].Value - other.Features[i].Value,2);
    }
    dist = Math.pow(dist, .5);
    CurrDistance = dist;
    return dist;
  }
  
  @Override
  public String toString(){
    String out = "";
    for(int i=0; i<NumFeatures; i++){
      out += Features[i].toString() + Delim;
    }
    return out.substring(0, out.length()-1);
    //return out.replaceAll(Delim + "$", "");
  }
  
  // This one includes the current distance
  public String toStringLong(){
    String out = Category + Delim2;
    for(int i=0; i<NumFeatures; i++){
      out += Features[i].toString() + Delim;
    }
    return out.substring(0, out.length()-1);
    //return out.replaceAll(Delim + "$", "");
  }
}
