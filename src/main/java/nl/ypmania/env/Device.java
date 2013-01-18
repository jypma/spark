package nl.ypmania.env;

import javax.xml.bind.annotation.XmlTransient;

public abstract class Device extends Receiver {
  private final Zone zone;

  public Device(Zone zone) {
    this.zone = zone;
    zone.addDevice(this);
  }
  
  protected void event (ZoneEvent event) {
    zone.event(event);
  }
  
  @XmlTransient public Zone getZone() {
    return zone;
  }
  
  @XmlTransient public Environment getEnvironment() {
    return zone.getEnvironment();
  }
  
  public abstract String getType();
}
