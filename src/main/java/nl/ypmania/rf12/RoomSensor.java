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
    if (packet.getContents().size() >= 7 && 
        packet.getContents().get(0) == sensorId &&
        packet.getContents().get(1) == roomId) {
      if (packet.getContents().get(4) == 1) {
        receiveTemperature(packet, false);        
      } else if (packet.getContents().get(4) == 10) {
        receiveTemperature(packet, true);                
      }
    }
  }

  private void receiveTemperature(RF12Packet packet, boolean batteryIsPercentage) {
    long now = System.currentTimeMillis();
    if (now - lastPacket < 9000) return;
    lastPacket = now;
    
    int v = packet.getContents().get(6) * 256 + packet.getContents().get(5);
    double temp = v / 100.0;
    log.info("Got temperature {} in {}.", temp, getZone().getName());
    
    if (temp > -30 && temp < 40)
      uploadTemp(temp);
    if (battery != null) {
      event(ZoneEvent.temperature(temp, battery));        
    } else {
      event(ZoneEvent.temperature(temp));
    }
    
    if (packet.getContents().size() >= 9) {
      battery = (packet.getContents().get(8).byteValue() * 256.0 + packet.getContents().get(7));
      if (batteryIsPercentage) {
        // already OK
      } else {
        battery = battery / 100; // now in Volt
        log.info("Battery of {} is {}V.", getZone().getName(), battery);
        if (battery > 4.2) battery = 4.2;
        if (battery < 3.0) battery = 3.0;
        battery = (battery - 3.0) / (4.2 - 3.0) * 100;
      }
      log.info("Battery of {} is {}%.", getZone().getName(), battery);
      if (battery < 10 && battery > 0) {
        getEnvironment().getNotifyService().lowBattery("Room sensor in " + getZone().getName(), battery);
      }
    }
    
    if (packet.getContents().size() >= 11) {
      int h = packet.getContents().get(10) * 256 + packet.getContents().get(9);
      double humidity = h / 100.0;
      log.info("Got humidity {} in {}.", humidity, getZone().getName());
      event(ZoneEvent.humidity(humidity));
      if (humidity >= 0 && humidity < 100) {
        uploadHumidity(humidity);          
      }
    }    
  }

  protected void uploadHumidity(double humidity) {
    
  }

  protected void uploadTemp(double temp) {
    if (name != null) {
      getEnvironment().getCosmService().updateDatapoint(name, temp);      
    }
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
