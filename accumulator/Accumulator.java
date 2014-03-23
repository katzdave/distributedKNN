package accumulator;

/**
 *
 * @author H
 */
public class Accumulator {
  
  /**
   * 
   * @param args
   * args[0] - serverPort
   * args[1] - leaderAccumulatorIP
   * args[2] - leaderPort
   * args[3] - masterIp
   * args[4] - masterPort
   * 
   */
  public static void main(String [] args) {
    if (args.length != 5) {
      System.out.println("Invalid number of arguments");
      System.exit(1);
    }
    int numCores = Runtime.getRuntime().availableProcessors();
    Integer myServerPort, leaderPort, masterPort;
    myServerPort = Integer.parseInt(args[0]);
    leaderPort = Integer.parseInt(args[2]);
    masterPort = Integer.parseInt(args[4]);
    AccumulatorProtocol protocol = 
            new AccumulatorProtocol(
                    myServerPort, 
                    args[1], 
                    leaderPort, 
                    args[3], 
                    masterPort, 
                    numCores);
  }
}
