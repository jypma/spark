package nl.ypmania.env;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import nl.ypmania.fs20.FS20Packet;
import nl.ypmania.rf12.RF12Packet;
import nl.ypmania.visonic.VisonicPacket;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public abstract class Receiver {
  public void receive (FS20Packet packet) {}
  public void receive (VisonicPacket packet) {}
  public void receive (RF12Packet packet) {}
}
