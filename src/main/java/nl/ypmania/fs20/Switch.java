package nl.ypmania.fs20;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Switch implements Receiver {
  private Set<Address> addresses = new HashSet<Address>();
  private final String name;
  private boolean on = false;
  
  public Switch (String name, Address... addresses) {
    this.name = name;
    this.addresses.addAll(Arrays.asList(addresses));
  }

  public void handle(Packet packet) {
    if (!addresses.contains(packet.getAddress())) return;
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
  }
  
  public boolean isOn() {
    return on;
  }
  
  public String getName() {
    return name;
  }
}
