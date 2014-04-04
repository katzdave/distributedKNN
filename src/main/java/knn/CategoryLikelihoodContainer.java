/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package knn;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author David
 */
public class CategoryLikelihoodContainer {
  static String DELIM = "'";
  List<CategoryLikelihood> CategoryLikelihoods;
  
  public CategoryLikelihoodContainer(){
    CategoryLikelihoods = new ArrayList<>();
  }
  
  public CategoryLikelihoodContainer(String input){
    CategoryLikelihoods = new ArrayList<>();
    String[] splat = input.split(DELIM);
    for(String s : splat){
      CategoryLikelihoods.add(new CategoryLikelihood(s));
    }
  }
  
  public void AddCategoryLikelihood(CategoryLikelihood cl){
    CategoryLikelihoods.add(cl);
  }
  
  @Override
  public String toString(){
    String outp = "";
    for(CategoryLikelihood cl : CategoryLikelihoods){
      outp += cl.toString() + DELIM;
    }
    return outp.substring(0, outp.length()-1);
  }
}
