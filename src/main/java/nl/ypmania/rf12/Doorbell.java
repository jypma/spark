package nl.ypmania.rf12;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.ypmania.env.Receiver;

public class Doorbell extends Receiver {
  private static final Logger log = LoggerFactory.getLogger(Doorbell.class);
  
  private int id1, id2;
  private long lastRing = 0;
  
  public Doorbell (char id1, char id2) {
    this.id1 = (int) id1;
    this.id2 = (int) id2;
  }
  
  @Override
  public void receive(RF12Packet packet) {
    long now = System.currentTimeMillis();
    if (now - lastRing < 1000) return;
    if (packet.getContents().size() > 1 && 
        packet.getContents().get(0) == id1 && 
        packet.getContents().get(1) == id2) {
      int v = 0;
      if (packet.getContents().size() >= 6) {
        v = packet.getContents().get(5) * 256 + v * packet.getContents().get(4);
      }
      int i = v / 100;
      String f = "" + (v % 100);
      while (f.length() < 3) f = "0" + f;
      log.info("Doorbell is ringing, capacitor charged to {}.{}0mV.", i, f);
      lastRing = now;
      ring(v * 10);
    }
  }
  
  protected void ring(int mV) {}
}
