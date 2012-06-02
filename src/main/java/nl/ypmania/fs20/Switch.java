package nl.ypmania.fs20;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Switch extends Receiver {
  private static final Logger log = LoggerFactory.getLogger(Switch.class);
  
  private Set<Address> addresses = new HashSet<Address>();
  private String name;
  private boolean on = false;
  
  protected Switch() {}
  
  public Switch (String name, Address... addresses) {
    this.name = name;
    this.addresses.addAll(Arrays.asList(addresses));
  }

  @Override
  public void receive(Packet packet) {
    if (!addresses.contains(packet.getAddress())) return;
    log.debug(name + " receiving " + packet);
    switch (packet.getCommand()) {
    case DIM_1:
    case DIM_2:
    case DIM_3:
    case DIM_4:
    case DIM_5:
    case DIM_6:
    case DIM_7:
    case DIM_8:
    case DIM_9:
    case DIM_10:
    case DIM_11:
    case DIM_12:
    case DIM_13:
    case DIM_14:
    case DIM_15:
    case ON_FULL:
    case ON_PREVIOUS:
    case TIMED_ON_FULL:
    case TIMED_ON_PREVIOUS:
    case DIM_UP:
      on = true;
      break;
      
    case OFF:
    case TIMED_OFF:
    case DIM_DOWN:
      on = false;
      break;
      
    case TOGGLE:
      on = !on;
      break;
    }
    log.debug("State is now: " + on);
  }
  
  public boolean isOn() {
    return on;
  }
  
  public String getName() {
    return name;
  }
}
