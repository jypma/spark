package nl.ypmania.visonic;

import nl.ypmania.env.Receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoorSensor extends Receiver {
  private static final Logger log = LoggerFactory.getLogger(DoorSensor.class);
  
  private String name;
  private VisonicAddress address;
  private boolean open;
  
  public DoorSensor(String name, VisonicAddress address) {
    this.name = name;
    this.address = address;
    this.open = false;
  }
  
  @Override
  public void receive(VisonicPacket packet) {
    if (packet.getAddress().equals(address)) {
      open = (packet.getByte4() & 0x04) == 0;
      boolean event = (packet.getByte4() & 0x01) == 1;
      String debug = (event ? " *event* " : " ") + (open ? "*open*" : "*closed");
      log.debug (name + ": " + VisonicPacket.bits(packet.getByte4()) + "-" + VisonicPacket.bits(packet.getByte5()) + debug);
    }
  }
  
}
