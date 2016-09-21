package nl.ypmania.rgb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class LampDTO {
  private String name;
  private String zoneName;
  private LampColor color;

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
  public LampColor getColor() {
    return color;
  }
  public void setColor(LampColor color) {
    this.color = color;
  }
}
