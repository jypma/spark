package nl.ypmania.env;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Settings {
  private boolean muteMotion = false;
  
  private boolean muteDoors = false;
  
  private boolean muteDoorbell = false;
  
  private String alarmMode = "off";

  private boolean noAutoLightsLiving = false;
  
  private boolean noAutoLightsCarport = false;
  
  public boolean isMuteDoors() {
    return muteDoors;
  }
  
  public boolean isMuteMotion() {
    return muteMotion;
  }
  
  public boolean isMuteDoorbell() {
    return muteDoorbell;
  }
  
  public boolean isNoAutoLightsLiving() {
    return noAutoLightsLiving;
  }
  
  public boolean isNoAutoLightsCarport() {
    return noAutoLightsCarport;
  }
  
  public String getAlarmMode() {
    return alarmMode;
  }
}
