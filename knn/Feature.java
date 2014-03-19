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
public class Feature {
  double Value;
  
  public Feature(double value){
    Value = value;
  }
  
  @Override
  public String toString(){
    return Double.toString(Value);
  }
}
