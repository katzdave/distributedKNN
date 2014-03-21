/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package knn;

/**
 *
 * @author david
 */
public class CategoryDistances implements Comparable<CategoryDistances>{
  static String DELIM = "_";
  String Category;
  double Distance;
  
  public CategoryDistances(String category, double distance){
    Category = category;
    Distance = distance;
  }
  
  public CategoryDistances(String everything){
    String [] splat = everything.split(DELIM);
    Category = splat[0];
    Distance = Double.parseDouble(splat[1]);
  }
  
  @Override
  public int compareTo(CategoryDistances other){
    if(this.Distance - other.Distance > 0)
      return 1;
    else if(this.Distance - other.Distance < 0)
      return -1;
    return 0;
  }
  
  @Override
  public String toString(){
    return Category + DELIM + Distance;
  }
}
