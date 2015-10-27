package nl.ypmania.env;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Actuator extends Device {
  private static final Logger log = LoggerFactory.getLogger(Actuator.class);
  
  private TimerTask offTask = null;
  private long offTaskTime;
  
  protected abstract String getName();
  
  protected Actuator() { super(null); }
  
  public Actuator (Zone zone) {
    super(zone);
  }
  
  public synchronized Integer getTimedOnMinutesLeft() {
    if (offTask != null) {
      long minutes = (offTaskTime - System.currentTimeMillis()) / (1000 * 60);
      if (minutes <= 0) return null; else return (int)minutes; 
    } else return null;
  }
  
  protected abstract Runnable getOnCommand();
  protected abstract void turnOff();
  
  public void timedOn (long durationSeconds) {
    timedOn (getOnCommand(), durationSeconds);
  }
  
  protected void timedOn (Runnable onCommand, long durationSeconds) {
    timedOnMillis (onCommand, durationSeconds * 1000);
  }
  
  public void timedOnMillis (long durationMillis) {
    timedOnMillis (getOnCommand(), durationMillis);
  }
  
  public synchronized boolean isTimedOn() {
    return isOn() && offTask != null;
  }
  
  public abstract boolean isOn();

  public synchronized void timedOnMillis (Runnable onCommand, long durationMillis) {
    log.debug("Turning {} on for {}ms.", getName(), durationMillis);
    onCommand.run();
    if (offTaskTime > System.currentTimeMillis() + durationMillis) return;
    cancelOff();
    offTask = new TimerTask() {
      @Override
      public void run() {
        turnOff();
        synchronized(this) {
          offTaskTime = 0;          
        }
      }
    };
    offTaskTime = System.currentTimeMillis() + durationMillis;
    getEnvironment().getTimer().schedule(offTask, durationMillis);
  }
  
  protected synchronized void cancelOff() {
    if (offTask != null) {
      offTask.cancel();
      offTask = null;
    }    
  }
  
  public void onFull() {
    cancelOff();
    getOnCommand().run();
  }
  
  public void off() {
    cancelOff();
    turnOff();    
  }  
}
