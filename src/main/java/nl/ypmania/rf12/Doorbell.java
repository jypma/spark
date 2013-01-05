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
    if (packet.getContents().size() >= 5 && 
        packet.getContents().get(0) == id1 && 
        packet.getContents().get(1) == id2 &&
        packet.getContents().get(4) == 1) {
      sendAck();
      
      long now = System.currentTimeMillis();
      if (now - lastRing < 1000) return;
      int v = 0;
      if (packet.getContents().size() >= 7) {
        v = packet.getContents().get(6) * 256 + packet.getContents().get(5);
      }
      int i = v / 100;
      String f = "" + (v % 100);
      while (f.length() < 2) f = "0" + f;
      log.info("Doorbell is ringing, capacitor charged to {}.{}0mV.", i, f);
      lastRing = now;
      ring(v * 10);
    }
  }
  
  private void sendAck() {
    int[] contents = new int[5];
    contents[0] = 'S';
    contents[1] = 'P';
    contents[2] = 'D';
    contents[3] = 'B';
    contents[4] = 2;
    getEnvironment().getRf12Service().queue(new RF12Packet(contents));
  }

  protected void ring(int mV) {}
}
