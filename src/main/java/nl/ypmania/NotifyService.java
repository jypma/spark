package nl.ypmania;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import nl.ypmania.env.EMailService;
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

  @Autowired private EMailService emailService;
  
  @PostConstruct
  public void start() {
    g1 = GrowlNetwork.register("Spark", password, "192.168.0.191"); 
    g2 = GrowlNetwork.register("Spark", password, "192.168.0.193"); 
    notify("Ready", "Spark is ready");
  }
  
  private void notify(String title, String msg) {
    g1.notify("Spark", title, msg, password);
    g2.notify("Spark", title, msg, password);
  }

  public void sendDoorOpen (String name) {
    notify("Door opened", name);
  }
  
  public void sendDoorClosed (String name) {
    notify("Door closed", name);
  }
  
  public void sendMotion (VisonicMotionSensor sensor) {
    notify("Motion detected", sensor.getName());
  }
  
  public void sendMotion (String sensorName) {
    notify("Motion detected", sensorName);
  }
  
  public void doorbell() {
    notify("Doorbell", "Somebody is at the door!");
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
