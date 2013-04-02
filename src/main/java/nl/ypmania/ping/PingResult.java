package nl.ypmania.ping;

public class PingResult {
  private boolean reachable;
  private double time;
  
  public PingResult(boolean reachable, double time) {
    this.reachable = reachable;
    this.time = time;
  }
  
  public boolean isReachable() {
    return reachable;
  }
  
  public double getTime() {
    return time;
  }

  public static PingResult unreachable() {
    return new PingResult(false, -1);
  }

  public static PingResult reachable(double time) {
    return new PingResult(true, time);
  }
}