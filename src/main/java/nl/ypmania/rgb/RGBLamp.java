package nl.ypmania.rgb;

import nl.ypmania.env.Actuator;
import nl.ypmania.env.Zone;
import nl.ypmania.env.ZoneEvent;
import nl.ypmania.rf12.RF12Packet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RGBLamp extends Actuator {

  public RGBLamp(Zone zone, String name, int id1, int id2) {
    super(zone);
    this.id1 = id1;
    this.id2 = id2;
    this.name = name;
  }

  private static final Logger log = LoggerFactory.getLogger(RGBLamp.class);
  
  private LampColor color = new LampColor (255, 255, 255, 255);
  private LampColor nextColor = color;
  private boolean on;
  
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
  
  private static final int[] BRIGHTNESS_THRESHOLDS = new int[] { 1, 2, 3, 4, 6, 8, 11, 16, 22, 31, 44, 62, 87, 123, 173 };
  /** Returns brightness as 0 (off) .. 16 (full on) */
  public int getBrightness() {
    if (on) {
      int lum = color.getLevel();
      for (int i = 0; i < BRIGHTNESS_THRESHOLDS.length; i++) {
        if (lum <= BRIGHTNESS_THRESHOLDS[i]) return i+1;
      }
      return 16;
    } else {
      return 0;
    }
  }
  
  @Override
  public void receive(RF12Packet packet) {
    if (packet.getContents().size() >= 5) {
      if (packet.getContents().get(0) == id1 &&
          packet.getContents().get(1) == id2) {
        if (packet.getContents().get(4) == 5) { // ping
          log.debug("{} received ping.", getName());
          sendNextColor();
        } else if (packet.getContents().get(4) == 6 && packet.getContents().size() >= 8) { // current color
          getZone().event(ZoneEvent.buttonPressed());
          synchronized(this) {
            if (packet.getContents().get(5) == 0 &&
                packet.getContents().get(6) == 0 &&
                packet.getContents().get(7) == 0) { // off
              log.debug("{} has been turned off.", getName());
              on = false;
            } else {
              on = true;
              int q = (packet.getContents().size() >= 9) ? packet.getContents().get(8) : 127;
              color = new LampColor(packet.getContents().get(5), packet.getContents().get(6), packet.getContents().get(7), q);
              log.debug("{} has been turned on to {}.", getName(), color);
              nextColor = color;
            }
          }
        }
      }
          
    }
  }

  public void timedOn(final LampColor color, int duration) {
    timedOn(new Runnable() {
      public void run() {
        setColor(color);
      }
    }, duration);
  }
  
  public synchronized void setColor (LampColor newColor) {
    on = true;
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
        RF12Packet packet = new RF12Packet(1, new int[] { 1,1,id1,id2,nextColor.getR(),nextColor.getG(),nextColor.getB(),nextColor.getQ(),0,0,0,0 });
        log.info("On: sending {}", packet);
        
        getEnvironment().getRf12Service().queue(getZone(), packet);
        color = nextColor;
      }
    });
  }

  @Override
  protected Runnable getOnCommand() {
    return new Runnable() {
      public void run() {
        setBrightness(16);
      }
    };
  }

  @Override
  protected void turnOff() {
    getEnvironment().onRf868Clear(new Runnable() {
      @Override
      public void run() {
        getEnvironment().setRf868UsageEnd(100);
        RF12Packet packet = new RF12Packet(1, new int[] { 1,1,id1,id2,0,0,0,0,0,0,0,0 });
        log.info("Off: sending {}", packet);
        getEnvironment().getRf12Service().queue(getZone(), packet);
        synchronized(this) {
          on = false;          
        }
      }
    });
  }

  @Override
  public synchronized boolean isOn() {
    return on && (color.getB() > 0 || color.getR() > 0 || color.getB() > 0);
  }

  public void setBrightness(int brightness) {
    if (brightness < 0) brightness = 0;
    if (brightness >= 16) brightness = 16;
    if (brightness == 0) {
      setColor(new LampColor(0,0,0,0));
    } else if (brightness == 16) {
      setColor(new LampColor(255,255,255,255));
    } else {
      int b = BRIGHTNESS_THRESHOLDS[brightness - 1];
      setColor(new LampColor(255, 255, 255, b));
    }
  }
}
