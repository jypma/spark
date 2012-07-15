package nl.ypmania;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import nl.ypmania.visonic.DoorSensor;
import nl.ypmania.visonic.MotionSensor;

import org.chamerling.javagrowl.GrowlNetwork;
import org.springframework.stereotype.Component;

@Component
public class GrowlService {
  private GrowlNetwork g1, g2;
  private String password = "lemur";
  

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

  public void sendDoorOpen (DoorSensor sensor) {
    notify("Door opened", sensor.getName());
  }
  
  public void sendDoorClosed (DoorSensor sensor) {
    notify("Door closed", sensor.getName());
  }
  
  public void sendMotion (MotionSensor sensor) {
    notify("Motion detected", sensor.getName());
  }
  
  public void sendMotion (String sensorName) {
    notify("Motion detected", sensorName);
  }
  
  @PreDestroy
  public void stop() {
    
  }
}
