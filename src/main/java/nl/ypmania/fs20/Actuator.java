package nl.ypmania.fs20;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import nl.ypmania.env.Receiver;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class Actuator extends Receiver {
  private FS20Address primaryAddress;
  private Set<FS20Address> addresses = new HashSet<FS20Address>();
  private String name;
  private TimerTask offTask = null;
  
  protected Actuator() {}
  
  public FS20Address getPrimaryAddress() {
    return primaryAddress;
  }
  
  public Actuator (String name, FS20Address primaryAddress, FS20Address... otherAddresses) {
    this.name = name;
    this.primaryAddress = primaryAddress;
    this.addresses.add(primaryAddress);
    this.addresses.addAll(Arrays.asList(otherAddresses));
  }
  
  public synchronized void timedOn (long durationSeconds) {
    dispatch(new FS20Packet (primaryAddress, getOnCommand()));
    if (offTask != null) {
      offTask.cancel();
      offTask = null;
    }
    offTask = new TimerTask() {
      @Override
      public void run() {
        dispatch(new FS20Packet (primaryAddress, Command.OFF));
      }
    };
    getEnvironment().getTimer().schedule(offTask, durationSeconds * 1000);
  }
  
  protected void dispatch (FS20Packet packet) {
    getEnvironment().getFs20Service().queueFS20(packet);
  }  
  
  public void onFull() {
    dispatch(new FS20Packet (primaryAddress, getOnCommand()));    
  }
  
  public void off() {
    dispatch(new FS20Packet (primaryAddress, Command.OFF));    
  }  
  
  protected Command getOnCommand() {
    return Command.ON_FULL;
  }

  public String getName() {
    return name;
  }
  
  public Set<FS20Address> getAddresses() {
    return addresses;
  }
}
