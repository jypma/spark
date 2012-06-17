package nl.ypmania.visonic;

import nl.ypmania.env.Receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MotionSensor extends Receiver {
  private static final Logger log = LoggerFactory.getLogger(MotionSensor.class);
  
  private String name;
  private VisonicAddress address;
  private boolean movement;
  
  public MotionSensor(String name, VisonicAddress address) {
    this.name = name;
    this.address = address;
  }
  
  @Override
  public void receive(VisonicPacket packet) {
    if (packet.getAddress().equals(address)) {
      movement = (packet.getByte4() & 0x01) == 1;
      boolean ping = (packet.getByte4() & 0x04) == 1;
      String debug = (movement ? " *movement* " : " ") + (ping ? "*ping*" : "");
      log.debug (name + ": " + VisonicPacket.bits(packet.getByte4()) + "-" + VisonicPacket.bits(packet.getByte5()) + debug);
    }
  }
  
}
