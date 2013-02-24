package nl.ypmania.env;

import java.util.TimerTask;

public class LightWatchdog {
  public LightWatchdog(Environment env, final Zone... zones) {
    env.getTimer().schedule(new TimerTask(){
      public void run() {
        for (Zone zone: zones) {
          if (zone.noActionSinceMinutes(15)) {
            zone.lightsOff();
          }
        }
      }      
    }, 15 * 60 * 1000, 15 * 60 * 1000);
  }
}
