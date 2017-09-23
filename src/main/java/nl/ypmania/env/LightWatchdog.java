package nl.ypmania.env;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightWatchdog {
  private static final Logger log = LoggerFactory.getLogger(LightWatchdog.class);
  
  private List<Device> ignores = new ArrayList<Device>();
  
  public LightWatchdog ignore(Device device) {
    ignores.add(device);
    return this;
  }
  
  public LightWatchdog(Environment env, final Zone... zones) {
    env.getTimer().schedule(new TimerTask(){
      public void run() {
        for (Zone zone: zones) {
          if (zone.noActionSinceMinutes(25)) {
            for (Device device: zone.getDevices()) {
              if (!ignores.contains(device)) {
                if (device instanceof Actuator) {
                  log.debug("Turning off {} in {}, since no movement since {}",
                      new Object[] {device, zone, zone.getLastAction()} );
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
