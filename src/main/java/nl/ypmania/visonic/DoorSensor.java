package nl.ypmania.visonic;

import nl.ypmania.env.Device;
import nl.ypmania.env.Zone;
import nl.ypmania.env.ZoneEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoorSensor extends Device {
  private static final Logger log = LoggerFactory.getLogger(DoorSensor.class);
  
  private String name;
  private VisonicAddress address;
  private boolean open = false;
  private boolean lowBattery = false;
  private boolean tamper = false;
  
  public DoorSensor(Zone zone, String name, VisonicAddress address) {
    super(zone);
    this.name = name;
    this.address = address;
  }
  
  @Override
  public void receive(VisonicPacket packet) {
    if (packet.getAddress().equals(address)) {
      open = (packet.getByte4() & 0x04) == 0;
      lowBattery = (packet.getByte4() & 0x02) == 1;
      tamper = (packet.getByte4() & 0x08) == 1;
      boolean event = (packet.getByte4() & 0x01) == 1;
      String debug = (event ? " *event* " : " ") + (lowBattery ? " *low battery* " : "") + (tamper ? " *tamper* " : "") + (open ? "*open*" : "*closed*");
      log.debug (name + ": " + VisonicPacket.bits(packet.getByte4()) + "-" + VisonicPacket.bits(packet.getByte5()) + debug);
      if (event) {
        if (open) {
          event(ZoneEvent.opened());
          opened();
        } else {
          event(ZoneEvent.closed());
          closed();
        }
      }
    }
  }
  
  public String getName() {
    return name;
  }
  
  protected void opened() {}
  protected void closed() {}
  
  @Override
  public String getType() {
    return "DoorSensor";
  }
}
