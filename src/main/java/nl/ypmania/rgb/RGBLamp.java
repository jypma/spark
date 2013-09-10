package nl.ypmania.rgb;

import nl.ypmania.env.Device;
import nl.ypmania.env.Zone;
import nl.ypmania.rf12.RF12Packet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RGBLamp extends Device {

  public RGBLamp(Zone zone, String name, int id1, int id2) {
    super(zone);
    this.id1 = id1;
    this.id2 = id2;
    this.name = name;
  }

  private static final Logger log = LoggerFactory.getLogger(RGBLamp.class);
  
  private LampColor color = new LampColor (180, 180, 180, 100);
  private LampColor nextColor = color;
  
  private final int id1, id2; // R G , L 2
  private final String name;

  @Override
  public String getType() {
    return "RGBLamp";
  }
  
  public String getName() {
    return name;
  }
  
  public LampColor getColor() {
    return color;
  }
  
  @Override
  public void receive(RF12Packet packet) {
    if (packet.getContents().size() >= 5) {
      if (packet.getContents().get(0) == id1 &&
          packet.getContents().get(1) == id2 &&
          packet.getContents().get(4) == 5) { // ping
        sendNextColor();
      }
    }
  }
  
  public synchronized void setColor (LampColor newColor) {
    if (nextColor.equals(newColor)) return;
    log.info ("Queueing change from {} to {}", color, newColor);
    boolean waiting = !nextColor.equals(color);
    nextColor = newColor;
    if (!waiting) {
      log.debug ("Scheduling a change");
      synchronized(this) {
        log.info ("Changing from {} to {}", color, nextColor);
        sendNextColor();
      }      
    }
  }

  private void sendNextColor() {
    getEnvironment().onRf868Clear(new Runnable() {
      @Override
      public void run() {
        getEnvironment().setRf868UsageEnd(100);
        RF12Packet packet = new RF12Packet(new int[] { 1,1,id1,id2,nextColor.getR(),nextColor.getG(),nextColor.getB(),nextColor.getQ(),0,0,0,0 });
        log.info("Sending {}", packet);
        getEnvironment().getNodeService().sendRF12(packet);
        color = nextColor;
      }
    });
  }
}
