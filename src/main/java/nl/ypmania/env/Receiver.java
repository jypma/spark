package nl.ypmania.env;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import nl.ypmania.fs20.FS20Packet;
import nl.ypmania.visonic.VisonicPacket;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class Receiver {
  private Environment environment;
  
  private UUID id = UUID.randomUUID();
  
  public void receive (FS20Packet packet) {}
  public void receive (VisonicPacket packet) {}
  
  public UUID getId() {
    return id;
  }
  
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }
  
  public Environment getEnvironment() {
    return environment;
  }
}
