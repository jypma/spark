package nl.ypmania.fs20;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class Receiver {
  private UUID id = UUID.randomUUID();
  
  public abstract void receive (Packet packet);
  
  public UUID getId() {
    return id;
  }
}
