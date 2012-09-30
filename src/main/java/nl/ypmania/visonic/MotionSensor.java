package nl.ypmania.visonic;

import nl.ypmania.env.Receiver;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MotionSensor extends Receiver {
  private static final Logger log = LoggerFactory.getLogger(MotionSensor.class);
  
  private String name;
  private VisonicAddress address;
  private boolean movement;
  private boolean lowBattery = false;
  private boolean tamper = false;
  private DateTime lastMovement;
  
  public MotionSensor(String name, VisonicAddress address) {
    this.name = name;
    this.address = address;
  }
  
  @Override
  public void receive(VisonicPacket packet) {
    if (packet.getAddress().equals(address)) {
      movement = (packet.getByte4() & 0x01) == 1;
      lowBattery = (packet.getByte4() & 0x02) == 1;
      tamper = (packet.getByte4() & 0x08) == 1;
      boolean ping = (packet.getByte4() & 0x04) == 1;
      String debug = (movement ? " *movement* " : " ") + (lowBattery ? " *low battery* " : "") + (tamper ? " *tamper* " : "") + (ping ? "*ping*" : "");
      log.debug (name + ": " + VisonicPacket.bits(packet.getByte4()) + "-" + VisonicPacket.bits(packet.getByte5()) + debug);
      if (movement) {
        lastMovement = DateTime.now();
        getEnvironment().getGrowlService().sendMotion(this);
      }
    }
  }
  
  public DateTime getLastMovement() {
    return lastMovement;
  }
  
  public String getName() {
    return name;
  }
  
}
