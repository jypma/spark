package nl.ypmania.rf12;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.ypmania.env.Device;
import nl.ypmania.env.Zone;
import nl.ypmania.env.ZoneEvent;

public class OutdoorSensor extends Device {
  private static final Logger log = LoggerFactory.getLogger(OutdoorSensor.class);

  private final String name;
  private final int id;

  public OutdoorSensor (Zone zone, String name, int id) {
    super(zone);
    this.name = name;
    this.id = id;
  }

  @Override
  public String getType() {
    return "OutdoorSensor";
  }

  @Override
  public void receive(RF12Packet packet) {
    List<Integer> c = packet.getContents();
    if (c.size() >= 9 && 
        c.get(0) == 'O' &&
        c.get(1) == id) {
      int soil = 0;
      boolean soilValid = false; 
      boolean soilExt = false;
      int temp = c.get(5) | (c.get(6) << 8);
      int supply = c.get(7) | (c.get(8) << 8);
      if (c.size() >= 13) {
        // has extended soil range
        soilExt = true;
        soilValid = true;
        soil = c.get(9) | (c.get(10) << 8) | (c.get(11) << 16) | (c.get(12) << 24);
        soilValid = soil < 500000;
      }
      log.info("Soil {} (valid:{}, ext:{}), temp {}, supply {}", new Object[] { soil, soilValid, soilExt, temp, supply });
      event(ZoneEvent.temperature(temp / 10.0));
      if (soilValid) getEnvironment().gauge(getZone(), name + ".soil", soil);
      getEnvironment().gauge(getZone(), name + ".temp", temp);
      getEnvironment().gauge(getZone(), name + ".supply", supply);
    }
  }
}
