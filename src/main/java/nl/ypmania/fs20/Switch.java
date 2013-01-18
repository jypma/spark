package nl.ypmania.fs20;

import nl.ypmania.env.Zone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Switch extends Actuator {
  private static final Logger log = LoggerFactory.getLogger(Switch.class);
  
  private boolean on = false;
  
  protected Switch() {}
  
  public Switch (Zone zone, String name, FS20Address primaryAddress, FS20Address... otherAddresses) {
    super (zone, name, primaryAddress, otherAddresses);
  }
  
  @Override
  protected Command getOnCommand() {
    return Command.ON_PREVIOUS;
  }

  @Override
  public void receive(FS20Packet packet) {
    if (!getAddresses().contains(packet.getAddress())) return;
    log.debug(getName() + " receiving " + packet);
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
  
  @Override
  public String getType() {
    return "Switch";
  }
}
