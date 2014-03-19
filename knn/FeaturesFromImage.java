/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package knn;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author david
 */
public class FeaturesFromImage {
  public void DoStuff(){
    BufferedImage img = null;
    
    try{
      img = ImageIO.read(new File("5.png"));
    }catch(IOException e){
      System.err.println("Exception!");
    }
    
    String s = "";
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
        s += count + ",";
      }
    }
    s+=5;
    System.out.println(s);
  }
}
