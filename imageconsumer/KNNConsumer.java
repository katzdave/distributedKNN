/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package imageconsumer;

import imageprocessing.*;

import java.util.List;
import java.util.LinkedList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.DataOutputStream;

import java.net.ServerSocket;
import java.net.Socket;
import javax.imageio.ImageIO;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author Eli
 */
public class KNNConsumer extends Consumer {

  public static String DELIM = " ";
  public static String READY = "2";
    
  public KNNConsumer(Socket clientSocket, 
                  MasterWrapper masterSocket,
                  ExecutorService executor) {
    super(clientSocket,masterSocket,executor);
    
  }

  @Override
  void processMessage(String msg) {
    switch (msg.charAt(0)) {
      case 'i': //tester
        storeVector();
        break;
      case '#': //leader
        appointLeader();
        break;
      case 'v':
        appointWorker();
        break;
      default:
        System.err.println("<Consumer> received bad message");
        try {
          clientSocket.close();
        } catch (IOException ioe) {
          System.err.println("<Consumer> Could not close socket");
        }
        updateBusyStatus();
        return;
    }
  }
  
  void storeVector() {
    
  }
  
  void appointWorker() {
    
  }
  
  void appointLeader() {
    
  }
}
