/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package knn;

import java.util.Random;
import java.util.List;

/**
 * Main class test suite
 * @author David
 */
public class Knn {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
//    FeatureContainer fc = new FeatureContainer();
//    Random rnd = new Random();
//    
//    for(int i=0; i<10; i++){
//      FeatureVector fv = new FeatureVector();
//      fv.Features[0] = new Feature(rnd.nextDouble());
//      fv.Features[1] = new Feature(rnd.nextDouble());
//      fv.Features[2] = new Feature(rnd.nextDouble());
//      if(i < 5){
//        fv.Category = "cat";
//      }
//      else{
//        fv.Category = "dog";
//      }
//      fc.AddVector(fv);
//    }
//    FeatureVector fv = new FeatureVector();
//    fv.Features[0] = new Feature(rnd.nextDouble());
//    fv.Features[1] = new Feature(rnd.nextDouble());
//    fv.Features[2] = new Feature(rnd.nextDouble());
//    
//    fc.GetKnn(fv);
//    String s = fc.KnearestToString();
//    CategoryFinder cf = new CategoryFinder();
//    cf.AddListFromString(s);
//    System.out.println(cf.GetCategory());
    
    
    OpticalCharRec ocr = new OpticalCharRec();
    ocr.DoStuff();
  }
}
