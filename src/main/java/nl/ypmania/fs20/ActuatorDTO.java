package nl.ypmania.fs20;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import nl.ypmania.env.Switch;
import nl.ypmania.rgb.RGBLamp;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ActuatorDTO {
  private String name;
  private String zoneName;
  private Boolean on;
  private Integer brightness;
  private String type;
  
  public ActuatorDTO() {}
  
  public ActuatorDTO(Switch s) {
    type = "switch";
    name = s.getName();
    zoneName = s.getZone().getName();
    on = s.isOn();
    brightness = on ? 16 : 0;
  }
  
  public ActuatorDTO(Dimmer d) {
    type = "dimmer";
    name = d.getName();
    zoneName = d.getZone().getName();
    on = d.getBrightness() > 0;
    brightness = d.getBrightness();
  }
  
  public ActuatorDTO(RGBLamp d) {
    type = "rgblamp";
    name = d.getName();
    zoneName = d.getZone().getName();
    on = d.getBrightness() > 0;
    brightness = d.getBrightness();
  }

  public String getZoneName() {
    return zoneName;
  }
  public void setZoneName(String zoneName) {
    this.zoneName = zoneName;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public Boolean getOn() {
    return on;
  }
  public void setOn(Boolean on) {
    this.on = on;
  }
  public Integer getBrightness() {
    return brightness;
  }
  public void setBrightness(Integer brightness) {
    this.brightness = brightness;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

}
