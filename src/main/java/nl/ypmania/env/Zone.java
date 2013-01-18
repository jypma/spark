package nl.ypmania.env;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.joda.time.DateTime;
import org.ocpsoft.pretty.time.PrettyTime;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Zone {
  private String name;
  private Zone parent;
  private List<Zone> subZones = new ArrayList<Zone>();
  private List<Device> devices = new ArrayList<Device>();
  
  private Double temperature;
  private Double calculatedTemperature;
  private DateTime lastAction;
  private Environment env;
  
  public Zone(Environment env, String name, Zone... subZones) {
    this.env = env;
    this.name = name;
    for (Zone z: subZones) {
      z.setParent(this);
      this.subZones.add(z);
    }
  }
  
  protected Zone() {}
  
  void addDevice(Device device) {
    this.devices.add(device);
  }
  
  public void event (ZoneEvent event) {
    switch (event.getType()) {
      case MOTION:
        env.getNotifyService().sendMotion(name);        
        action(event); 
        break;
        
      case OPENED:
        env.getNotifyService().sendDoorOpen(name);
        action(event); 
        break;
        
      case CLOSED:
        env.getNotifyService().sendDoorClosed(name);
        action(event); 
        break;
        
      case RING:
        action(event); 
        break;
        
      case TEMPERATURE:
        temperature = event.getTemperature();
        calculatedTemperature = null;
        if (parent != null) {
          parent.calculateTemperature();
        }
        break;
    }
  }
  
  private synchronized void calculateTemperature() {
    if (temperature == null) {
      double t = 0;
      int count = 0;
      for (Zone z: subZones) {
        Double zoneT = z.getTemperature();
        if (zoneT != null) {
          t += zoneT;
          count++;
        }
      }
      if (count > 0) {
        calculatedTemperature = t / count;
        if (parent != null) {
          parent.calculateTemperature();
        }
      }
    }
  }

  private void setParent(Zone parent) {
    this.parent = parent;
  }

  private void action(ZoneEvent event) {
    lastAction = event.getTime();
    if (parent != null) parent.action(event);
  }
  
  public String getName() {
    return name;
  }
  
  public Double getTemperature() {
    return temperature != null ? temperature : calculatedTemperature;
  }
  
  @XmlTransient public DateTime getLastActionTime() {
    return lastAction;
  }
  
  @XmlTransient public Environment getEnvironment() {
    return env;
  }
  
  public String getLastAction() {
    return lastAction != null ? new PrettyTime().format(lastAction.toDate()) : "unknown";
  }
  
  public List<Zone> getSubZones() {
    return subZones;
  }
  
  public List<Device> getDevices() {
    return devices;
  }
}
