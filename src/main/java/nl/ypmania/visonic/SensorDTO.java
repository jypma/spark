package nl.ypmania.visonic;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.DateTime;
import org.ocpsoft.pretty.time.PrettyTime;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SensorDTO {
  private String name;
  private String lastEvent;
  
  public SensorDTO(MotionSensor d) {
    setName(d.getName());
    setLastEventFrom(d.getLastMovement());
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

}
