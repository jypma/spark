package nl.ypmania.rf12;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ValveDTO {
  private String zoneName;
  private String name;
  private Boolean target1;
  private Boolean target2;
  private Boolean target3;
  private int count;
  private Boolean test;
  private int on;
  
  public ValveDTO() {
  }
  
  public ValveDTO(GardenValve s) {
    zoneName = s.getZone().getName();
    name = s.getId();
    target1 = s.isTarget1();
    target2 = s.isTarget2();
    target3 = s.isTarget3();
    count = s.getCount();
    on = s.getOn();
    test = s.isTest();
  }
  public int getOn() {
    return on;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getZoneName() {
    return zoneName;
  }
  public void setZoneName(String zoneName) {
    this.zoneName = zoneName;
  }
  public int getCount() {
    return count;
  }

  public Boolean getTarget1() {
    return target1;
  }
  public Boolean getTarget2() {
    return target2;
  }
  public Boolean getTarget3() {
    return target3;
  }
  public boolean isTest() {
    return test != null && test;
  }
}
