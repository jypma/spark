package nl.ypmania;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import nl.ypmania.env.EMailService;
import nl.ypmania.env.Home;
import nl.ypmania.env.SFX;
import nl.ypmania.visonic.VisonicMotionSensor;

import org.chamerling.javagrowl.GrowlNetwork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Component
public class NotifyService {
  private Cache<String, String> batteryWarnings = CacheBuilder.newBuilder()
      .expireAfterWrite(1, TimeUnit.DAYS)
      .build();
  
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
    /*
    if (zones != null && home.getSettings().shouldAlarmFor(zones)) {
      sfx.play("alarm.wav");
      emailService.sendMail(title, "=============== ALARM at " + new Date() + "=====================\n\n" + msg);
    }
    */
  }

  public void sendDoorOpen (String name, String... zones) {
    notify("Door opened", name, zones);
  }
  
  public void sendDoorClosed (String name) {
    notify("Door closed", name, null);
  }
  
  public void sendMotion (VisonicMotionSensor sensor, String... zones) {
    notify("Motion detected", sensor.getName(), zones);
  }
  
  public void sendMotion (String sensorName, String... zones) {
    notify("Motion detected", sensorName, zones);
  }
  
  public void doorbell() {
    notify("Doorbell", "Somebody is at the door!", null);
  }
  
  @PreDestroy
  public void stop() {
    
  }

  public void lowBattery(String name, Double battery) {
    if (batteryWarnings.getIfPresent(name) == null) {
      batteryWarnings.put(name, name);
      emailService.sendMail(name, "Warning: Battery of " + name + " is at " + battery + 
          "mV.\nThe battery might need to be recharged / replaced.");
    }
  }
}
