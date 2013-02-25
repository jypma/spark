package nl.ypmania.env;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import nl.ypmania.fs20.Actuator;

public class LightWatchdog {
  private List<Device> ignores = new ArrayList<Device>();
  
  public LightWatchdog ignore(Device device) {
    ignores.add(device);
    return this;
  }
  
  public LightWatchdog(Environment env, final Zone... zones) {
    env.getTimer().schedule(new TimerTask(){
      public void run() {
        for (Zone zone: zones) {
          if (zone.noActionSinceMinutes(15)) {
            for (Device device: zone.getDevices()) {
              if (!ignores.contains(device)) {
                if (device instanceof Actuator) {
                  ((Actuator)device).off();
                }
              }
            }
          }
        }
      }      
    }, 15 * 60 * 1000, 15 * 60 * 1000);
  }
}
