package nl.ypmania.fs20;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import nl.ypmania.env.Zone;

public abstract class FS20Actuator extends nl.ypmania.env.Actuator {
  private FS20Address primaryAddress;
  private Set<FS20Address> addresses = new HashSet<FS20Address>();
  private String name;
  private Zone receiverZone;
  
  protected FS20Actuator() { super(null); }
  
  @XmlTransient public FS20Address getPrimaryAddress() {
    return primaryAddress;
  }
  
  public FS20Actuator (Zone zone, Zone receiverZone, String name, FS20Address primaryAddress, FS20Address... otherAddresses) {
    super(zone);
    this.receiverZone = receiverZone;
    this.name = name;
    this.primaryAddress = primaryAddress;
    this.addresses.add(primaryAddress);
    this.addresses.addAll(Arrays.asList(otherAddresses));
  }
  
  @Override
  public String toString() {
    return name + ":" + primaryAddress;
  }
  
  public FS20Actuator (Zone zone, String name, FS20Address primaryAddress, FS20Address... otherAddresses) {
    this(zone, zone, name, primaryAddress, otherAddresses);
  }
  
  public void timedOn (Command onCommand, long durationSeconds) {
    timedOnMillis (onCommand, durationSeconds * 1000);
  }
  
  public abstract boolean isOn();

  public synchronized void timedOnMillis (final Command onCommand, long durationMillis) {
    timedOnMillis(new Runnable() {
      public void run() {
        dispatch(new FS20Packet (primaryAddress, onCommand));
      }
    }, durationMillis);
  }
  
  protected void dispatch (final FS20Packet packet) {
    getEnvironment().getFs20Service().queueFS20(receiverZone, packet);      
  }
  
  @Override
  protected void turnOff() {
    dispatch(new FS20Packet (primaryAddress, Command.OFF));
  }
  
  @Override
  protected Runnable getOnCommand() {
    return new Runnable() {
      public void run() {
        dispatch(new FS20Packet (primaryAddress, Command.ON_FULL));
      }
    };
  }

  public String getName() {
    return name;
  }
  
  @XmlTransient public Set<FS20Address> getAddresses() {
    return addresses;
  }
}
