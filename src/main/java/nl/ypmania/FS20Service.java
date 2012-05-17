package nl.ypmania;

import nl.ypmania.fs20.Address;
import nl.ypmania.fs20.Environment;
import nl.ypmania.fs20.Switch;

import org.springframework.stereotype.Component;

@Component
public class FS20Service {
  private final Environment environment;
  
  public FS20Service() {
    int HOUSE = 12341234;
    Address MASTER = new Address(HOUSE, 4444);
    Address LIGHTS = new Address(HOUSE, 4411);
    Address LIVING_ROOM = new Address(HOUSE, 1144);
    environment = new Environment(
      new Switch("Corner lamp", MASTER, LIGHTS, LIVING_ROOM, new Address(HOUSE, 1234))  
    );
  }
}
