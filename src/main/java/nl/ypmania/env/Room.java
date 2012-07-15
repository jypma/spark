package nl.ypmania.env;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import nl.ypmania.fs20.Dimmer;
import nl.ypmania.fs20.Switch;

@XmlRootElement(name="Room")
@XmlAccessorType(XmlAccessType.FIELD)
public class Room {
  private List<Dimmer> dimmers;
  private List<Switch> switches;
  private transient long lastMotion;

  public List<Dimmer> getDimmers() {
    return dimmers;
  }
  
  public long getLastMotion() {
    return lastMotion;
  }
  
  public List<Switch> getSwitches() {
    return switches;
  }
  
  public void setDimmers(List<Dimmer> dimmers) {
    this.dimmers = dimmers;
  }
  
  public void setLastMotion(long lastMotion) {
    this.lastMotion = lastMotion;
  }
  
  public void setSwitches(List<Switch> switches) {
    this.switches = switches;
  }
}
