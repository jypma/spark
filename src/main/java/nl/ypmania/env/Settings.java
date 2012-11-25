package nl.ypmania.env;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Settings {
  private boolean muteMotion = false;
  
  private boolean muteDoors = false;
  
  private boolean muteDoorbell = false;
  
  private String alarmMode = "OFF";
  
  public boolean isMuteDoors() {
    return muteDoors;
  }
  
  public boolean isMuteMotion() {
    return muteMotion;
  }
  
  public boolean isMuteDoorbell() {
    return muteDoorbell;
  }
  
  public String getAlarmMode() {
    return alarmMode;
  }

  public boolean shouldAlarmFor(String[] zones) {
    if (StringUtils.isBlank(alarmMode) || alarmMode.equalsIgnoreCase("off")) return false;
    if (alarmMode.equalsIgnoreCase("on")) return true;
    for (String zone: zones)
      if (alarmMode.contains(zone)) return true;
    return false;
  }
}
