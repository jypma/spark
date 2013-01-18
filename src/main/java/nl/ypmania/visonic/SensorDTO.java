package nl.ypmania.visonic;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import nl.ypmania.fs20.FS20MotionSensor;
import nl.ypmania.rf12.Doorbell;

import org.joda.time.DateTime;
import org.ocpsoft.pretty.time.PrettyTime;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SensorDTO {
  private String name;
  private String battery;
  private String lastEvent;
  
  public SensorDTO(VisonicMotionSensor d) {
    setName(d.getName());
    setLastEventFrom(d.getLastMovement());
  }

  public SensorDTO(FS20MotionSensor d) {
    setName(d.getName());
    setLastEventFrom(d.getLastMovement());
  }

  public SensorDTO(Doorbell doorbell) {
    setName("Doorbell");
    setLastEventFrom(doorbell.getLastRing());
    Double mV = doorbell.getBattery();
    if (mV != null && mV > 0) {
      battery = "" + mV + " mV";
    }
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public void setLastEvent(String lastEvent) {
    this.lastEvent = lastEvent;
  }
  
  public String getLastEvent() {
    return lastEvent;
  }
  
  public void setLastEventFrom(DateTime time) {
    if (time != null)
      lastEvent = new PrettyTime().format(time.toDate());
    else
      lastEvent = "unknown";
  }

  public String getBattery() {
    return battery;
  }
  
  public void setBattery(String battery) {
    this.battery = battery;
  }
}
