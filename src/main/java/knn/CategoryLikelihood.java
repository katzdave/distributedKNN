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
public class CategoryLikelihood {
  static String DELIM = "_";
  String Category;
  double Likelihood;
  
  public CategoryLikelihood(String category, double likelihood){
    Category = category;
    Likelihood = likelihood;
  }
  
  public CategoryLikelihood(String everything){
    String [] splat = everything.split(DELIM);
    Category = splat[0];
    Likelihood = Double.parseDouble(splat[1]);
  }
  
  @Override
  public String toString(){
    return Category + DELIM + Likelihood;
  }
}
