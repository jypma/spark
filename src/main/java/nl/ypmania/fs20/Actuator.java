package nl.ypmania.fs20;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;

import javax.xml.bind.annotation.XmlTransient;

import nl.ypmania.env.Device;
import nl.ypmania.env.Zone;

public abstract class Actuator extends Device {
  private FS20Address primaryAddress;
  private Set<FS20Address> addresses = new HashSet<FS20Address>();
  private String name;
  private TimerTask offTask = null;
  private long offTaskTime;
  
  protected Actuator() { super(null); }
  
  public synchronized Integer getTimedOnMinutesLeft() {
    if (offTask != null) {
      long minutes = (offTaskTime - System.currentTimeMillis()) / (1000 * 60);
      if (minutes <= 0) return null; else return (int)minutes; 
    } else return null;
  }
  
  @XmlTransient public FS20Address getPrimaryAddress() {
    return primaryAddress;
  }
  
  public Actuator (Zone zone, String name, FS20Address primaryAddress, FS20Address... otherAddresses) {
    super(zone);
    this.name = name;
    this.primaryAddress = primaryAddress;
    this.addresses.add(primaryAddress);
    this.addresses.addAll(Arrays.asList(otherAddresses));
  }
  
  public void timedOn (long durationSeconds) {
    timedOn (getOnCommand(), durationSeconds);
  }
  
  public void timedOn (Command onCommand, long durationSeconds) {
    timedOnMillis (onCommand, durationSeconds * 1000);
  }
  
  public void timedOnMillis (long durationMillis) {
    timedOnMillis (getOnCommand(), durationMillis);
  }
  
  public synchronized boolean isTimedOn() {
    return isOn() && offTask != null;
  }
  
  public abstract boolean isOn();

  public synchronized void timedOnMillis (Command onCommand, long durationMillis) {
    dispatch(new FS20Packet (primaryAddress, onCommand));
    if (offTaskTime > System.currentTimeMillis() + durationMillis) return;
    cancelOff();
    offTask = new TimerTask() {
      @Override
      public void run() {
        dispatch(new FS20Packet (primaryAddress, Command.OFF));
        synchronized(this) {
          offTaskTime = 0;          
        }
      }
    };
    offTaskTime = System.currentTimeMillis() + durationMillis;
    getEnvironment().getTimer().schedule(offTask, durationMillis);
  }
  
  protected void dispatch (final FS20Packet packet) {
    getEnvironment().getFs20Service().queueFS20(packet);
    /*
    getEnvironment().getTimer().schedule(new TimerTask(){
      public void run() {
        getEnvironment().getFs20Service().queueFS20(packet);
        getEnvironment().getFs20Service().queueFS20(packet);
      }}, 500);
      */
  }
  
  protected synchronized void cancelOff() {
    if (offTask != null) {
      offTask.cancel();
      offTask = null;
    }    
  }
  
  public void onFull() {
    cancelOff();
    dispatch(new FS20Packet (primaryAddress, getOnCommand()));
  }
  
  public void off() {
    cancelOff();
    dispatch(new FS20Packet (primaryAddress, Command.OFF));    
  }  
  
  protected Command getOnCommand() {
    return Command.ON_FULL;
  }

  public String getName() {
    return name;
  }
  
  @XmlTransient public Set<FS20Address> getAddresses() {
    return addresses;
  }
}
