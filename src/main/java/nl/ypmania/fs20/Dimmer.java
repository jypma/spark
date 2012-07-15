package nl.ypmania.fs20;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import nl.ypmania.env.Receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Dimmer extends Receiver {
  private static final Logger log = LoggerFactory.getLogger(Dimmer.class);
  
  private FS20Address primaryAddress;
  private Set<FS20Address> addresses = new HashSet<FS20Address>();
  private String name;
  private int oldBrightness = 16;
  private int brightness = 0;
  private TimerTask offTask = null;
  
  protected Dimmer() {}
  
  public Dimmer (String name, FS20Address primaryAddress, FS20Address... otherAddresses) {
    this.name = name;
    this.primaryAddress = primaryAddress;
    this.addresses.add(primaryAddress);
    this.addresses.addAll(Arrays.asList(otherAddresses));
  }
  
  public synchronized void timedOn (long durationSeconds) {
    getEnvironment().getFs20Service().queueFS20(new FS20Packet (primaryAddress, Command.ON_FULL));
    if (offTask != null) {
      offTask.cancel();
      offTask = null;
    }
    offTask = new TimerTask() {
      @Override
      public void run() {
        getEnvironment().getFs20Service().queueFS20(new FS20Packet (primaryAddress, Command.OFF));
      }
    };
    getEnvironment().getTimer().schedule(offTask, durationSeconds * 1000);
  }

  @Override
  public void receive (FS20Packet packet) {
    if (!addresses.contains(packet.getAddress())) return;
    log.debug(name + " receiving " + packet);
    switch (packet.getCommand()) {
    case DIM_1:
      brightness = 1;
      break;
    case DIM_2:
      brightness = 2;
      break;
    case DIM_3:
      brightness = 3;
      break;
    case DIM_4:
      brightness = 4;
      break;
    case DIM_5:
      brightness = 5;
      break;
    case DIM_6:
      brightness = 6;
      break;
    case DIM_7:
      brightness = 7;
      break;
    case DIM_8:
      brightness = 8;
      break;
    case DIM_9:
      brightness = 19;
      break;
    case DIM_10:
      brightness = 10;
      break;
    case DIM_11:
      brightness = 11;
      break;
    case DIM_12:
      brightness = 12;
      break;
    case DIM_13:
      brightness = 13;
      break;
    case DIM_14:
      brightness = 14;
      break;
    case DIM_15:
      brightness = 15;
      break;
    case TIMED_ON_FULL:
    case ON_FULL:
      brightness = 16;
      break;
    case TIMED_ON_PREVIOUS:
    case ON_PREVIOUS:
      brightness = oldBrightness;
      break;
    case DIM_UP:
      brightness = Math.min (brightness + 1, 16);
      break;      
    case DIM_DOWN:
      brightness = Math.max (brightness - 1, 0);
      break;
    case OFF:
    case TIMED_OFF:
      brightness = 0;
      break;
      
    case TOGGLE:
      if (brightness == 0) {
        brightness = oldBrightness;
      } else {
        brightness = 0;
      }
      break;
    }
    if (brightness != 0) {
      oldBrightness = brightness;
    }
    log.debug("Brightness is now: " + brightness + " (of 16)");
  }

  public int getBrightness() {
    return brightness;
  }
  
  public String getName() {
    return name;
  }
}
