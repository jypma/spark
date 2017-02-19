package nl.ypmania.env;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;

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
  private List<String> preferredProxies = new ArrayList<>();
  
  private Double temperature;
  private Double calculatedTemperature;
  
  private Double humidity;
  private Double calculatedHumidity;
  
  private volatile DateTime lastAction;
  private Environment env;
  private TimerTask tempResetTask, humResetTask;
  
  public Zone(Environment env, String name, Zone... subZones) {
    this(env, name, Arrays.<String>asList(), subZones);
  }
  
  public Zone(Environment env, String name, List<String> proxies, Zone... subZones) {
    for (String s: proxies) {
      preferredProxies.add(s);
    }
    this.env = env;
    env.addZone(this);
    this.name = name;
    for (Zone z: subZones) {
      z.setParent(this);
      this.subZones.add(z);
    }
  }
  
  protected Zone() {}
  
  @Override
  public String toString() {
    return name;
  }
  
  @XmlTransient
  public Zone getParent() {
    return parent;
  }
  
  public Iterable<String> getPreferredProxies() {
    return preferredProxies;
  }
  
  void addDevice(Device device) {
    this.devices.add(device);
  }
  
  public void event (ZoneEvent event) {
    switch (event.getType()) {
      case MOTION:
        env.getNotifyService().sendMotion(name);        
        action(event);
        possibleAlarm(event);
        break;
        
      case OPENED:
        env.getNotifyService().sendDoorOpen(name);
        action(event); 
        possibleAlarm(event);
        break;
        
      case CLOSED:
        env.getNotifyService().sendDoorClosed(name);
        action(event); 
        break;
        
      case RING:
        action(event); 
        break;
        
      case TEMPERATURE:
        scheduleResetTemp();
        setTemperature(event.getValue());
        break;
        
      case HUMIDITY:
        scheduleResetHum();
        setHumidity(event.getValue());
        break;
        
      case BUTTON:
        action(event);
        break;
        
      case MOVIE:
        action(event);
        break;
        
      case COUNT:
        // Electricity ping does not cause action
        break;
    }
  }
  
  private synchronized void scheduleResetTemp() {
    if (tempResetTask != null) {
      try {
        tempResetTask.cancel();
      } catch (IllegalStateException x) {} // Timer already cancelled is OK.
    }
    tempResetTask = new TimerTask() {
      public void run() {
        setTemperature(null);
      }
    };
    env.getTimer().schedule(tempResetTask, 3600 * 1000);
  }

  private synchronized void scheduleResetHum() {
    if (humResetTask != null) {
      try {
          humResetTask.cancel();
      } catch (IllegalStateException x) {} // Timer already cancelled is OK.          
    }
    humResetTask = new TimerTask() {
      public void run() {
        setHumidity(null);
      }
    };
    env.getTimer().schedule(humResetTask, 3600 * 1000);
  }

  private synchronized void setHumidity(Double h) {
    humidity = h;
    calculatedHumidity = null;
    calculateHumidity();
    if (parent != null) {
      parent.calculateHumidity();
    }
  }
  
  private synchronized void setTemperature (Double t) {
    temperature = t;
    calculatedTemperature = null;
    calculateTemperature();
    if (parent != null) {
      parent.calculateTemperature();
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

  private synchronized void calculateHumidity() {
    if (humidity == null) {
      double t = 0;
      int count = 0;
      for (Zone z: subZones) {
        Double zoneT = z.getHumidity();
        if (zoneT != null) {
          t += zoneT;
          count++;
        }
      }
      if (count > 0) {
        calculatedHumidity = t / count;
        if (parent != null) {
          parent.calculateHumidity();
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
  
  private void possibleAlarm(ZoneEvent event) {
    if (getEnvironment().isAlarmArmed(this))
      getEnvironment().getEmailService().sendMail(
          "Alarm in " + name, "An alarm was triggered in " + name + 
          ", because of " + event.getType() + ".");
    if (parent != null) parent.possibleAlarm(event);
  }
  
  public String getName() {
    return name;
  }
  
  public String getPath() {
    if (parent == null) {
      return getName();
    } else {
      return parent.getPath() + "." + getName();
    }
  }
  
  public Double getTemperature() {
    return temperature != null ? temperature : calculatedTemperature;
  }
  
  public Double getHumidity() {
    return humidity != null ? humidity : calculatedHumidity;
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

  public boolean noActionSinceMinutes(int minutes) {
    return lastAction != null && lastAction.plusMinutes(minutes).isBeforeNow();
  }
  
  public boolean actionSinceSeconds(int seconds) {
    return lastAction != null && lastAction.plusSeconds(seconds).isAfterNow();
  }
  
}
