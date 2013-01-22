package nl.ypmania.rf12;

import nl.ypmania.env.Device;
import nl.ypmania.env.Zone;
import nl.ypmania.env.ZoneEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoomSensor extends Device {
  private static final Logger log = LoggerFactory.getLogger(RoomSensor.class);
  
  private static final int sensorId = (int) 'R';
  private final int roomId;
  private long lastPacket = 0;
  private String name;
  private Double battery = null;
  
  public RoomSensor (Zone zone, String name, int roomId) {
    super(zone);
    this.name = name;
    this.roomId = roomId;
  }
  
  @Override
  public void receive(RF12Packet packet) {
    long now = System.currentTimeMillis();
    if (now - lastPacket < 9000) return;
    if (packet.getContents().size() >= 7 && 
        packet.getContents().get(0) == sensorId &&
        packet.getContents().get(1) == roomId) {
      lastPacket = now;
      
      int v = packet.getContents().get(6) * 256 + packet.getContents().get(5);
      double temp = v / 100.0;
      log.info("Got temperature {} in {}.", temp, name);
      
      if (temp > -30 && temp < 40)
        uploadTemp(temp);
      if (battery != null) {
        event(ZoneEvent.temperature(temp, battery));        
      } else {
        event(ZoneEvent.temperature(temp));
      }
      
      if (packet.getContents().size() >= 9) {
        battery = (packet.getContents().get(8).byteValue() * 256 + packet.getContents().get(7)) / 100.0;
        log.info("Battery of {} is {} mV.", name, battery);
        if (battery < 3.1) {
          getEnvironment().getNotifyService().lowBattery("Room sensor in " + name, battery);
        }
      }
      
      if (packet.getContents().size() >= 11) {
        int h = packet.getContents().get(10) * 256 + packet.getContents().get(9);
        double humidity = h / 100.0;
        log.info("Got humidity {} in {}.", humidity, name);
        event(ZoneEvent.humidity(humidity));
        if (humidity >= 0 && humidity < 100) {
          uploadHumidity(humidity);          
        }
      }
    }
  }

  protected void uploadHumidity(double humidity) {
    
  }

  protected void uploadTemp(double temp) {
    getEnvironment().getCosmService().updateDatapoint(name, temp);
  }
  
  public String getName() {
    return name;
  }
  
  @Override
  public String getType() {
    return "RoomSensor";
  }
  
  public Double getBattery() {
    return battery;
  }
  
}
