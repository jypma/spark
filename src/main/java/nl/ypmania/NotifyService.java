package nl.ypmania;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import nl.ypmania.env.EMailService;
import nl.ypmania.env.Home;
import nl.ypmania.env.SFX;
import nl.ypmania.visonic.DoorSensor;
import nl.ypmania.visonic.MotionSensor;

import org.chamerling.javagrowl.GrowlNetwork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotifyService {
  
  private GrowlNetwork g1, g2;
  private String password = "lemur";
  private @Autowired Home home;
  private @Autowired SFX sfx;

  @Autowired private EMailService emailService;
  
  @PostConstruct
  public void start() {
    g1 = GrowlNetwork.register("Spark", password, "192.168.0.191"); 
    g2 = GrowlNetwork.register("Spark", password, "192.168.0.193"); 
    notify("Ready", "Spark is ready", null);
  }
  
  private void notify(String title, String msg, String[] zones) {
    g1.notify("Spark", title, msg, password);
    g2.notify("Spark", title, msg, password);
    if (zones != null && home.getSettings().shouldAlarmFor(zones)) {
      sfx.play("alarm.wav");
      emailService.sendMail(title, "=============== ALARM at " + new Date() + "=====================\n\n" + msg);
    }
  }

  public void sendDoorOpen (DoorSensor sensor, String... zones) {
    notify("Door opened", sensor.getName(), zones);
  }
  
  public void sendDoorClosed (DoorSensor sensor) {
    notify("Door closed", sensor.getName(), null);
  }
  
  public void sendMotion (MotionSensor sensor, String... zones) {
    notify("Motion detected", sensor.getName(), zones);
  }
  
  public void sendMotion (String sensorName, String... zones) {
    notify("Motion detected", sensorName, zones);
  }
  
  public void doorbell(int mV) {
    notify("Doorbell", "Somebody is at the door!", null);
    if (mV < 2500 && mV != 0) {
      notify("Doorbell", "Warning: low battery at " + mV + "mV", null);
      emailService.sendMail("Doorbell", "Warning: Doorbell capacitor was only charged to " + mV + 
          "mV.\nThe battery might need to be recharged / replaced.");
    }
  }
  
  @PreDestroy
  public void stop() {
    
  }
}
