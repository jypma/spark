package nl.ypmania.env;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Settings {
  private boolean muteMotion = false;
  
  private boolean muteDoors = false;
  
  public boolean isMuteDoors() {
    return muteDoors;
  }
  
  public boolean isMuteMotion() {
    return muteMotion;
  }
}
