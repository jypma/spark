package nl.ypmania.fs20;

import nl.ypmania.env.Device;
import nl.ypmania.env.Zone;
import nl.ypmania.env.ZoneEvent;

import org.joda.time.DateTime;
import org.ocpsoft.pretty.time.PrettyTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlTransient;

public class FS20MotionSensor extends Device {
  private static final Logger log = LoggerFactory.getLogger(FS20MotionSensor.class);
  
  private String name;
  private FS20Address address;
  private DateTime lastMovement;
  
  public FS20MotionSensor(Zone zone, String name, FS20Address address) {
    super(zone);
    this.name = name;
    this.address = address;
  }
  
  @Override
  public void receive(FS20Packet packet) {
    if (!packet.getAddress().equals(address)) return;
    if (packet.getCommand() == Command.OFF || 
        packet.getCommand() == Command.RESET || 
        packet.getCommand() == Command.SEND_STATUS) return;
    if (lastMovement != null && lastMovement.plusSeconds(1).isAfterNow()) return;
    
    log.debug("Motion on {}", name);
    event(ZoneEvent.motion());
    lastMovement = DateTime.now();
    motion();
    getEnvironment().increment(getZone(), name + ".movement");
  }
  
  public String getName() {
    return name;
  }
  
  public DateTime getLastMovement() {
    return lastMovement;
  }
  
  @XmlTransient public String getLastMovementPretty() {
    return lastMovement == null ? "unknown" : new PrettyTime().format(lastMovement.toDate());
  }
  
  protected void motion() {
    
  }
  
  @Override
  public String getType() {
    return "FS20MotionSensor";
  }
}
