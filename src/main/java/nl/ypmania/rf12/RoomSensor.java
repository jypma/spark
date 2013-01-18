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
      int v = packet.getContents().get(6) * 256 + packet.getContents().get(5);
      double temp = v / 100.0;
      log.info("Got temperature {} in {}.", temp, name);
      
      if (packet.getContents().size() >= 9) {
         battery = (packet.getContents().get(8).byteValue() * 256 + packet.getContents().get(7)) / 100.0;
         log.info("Battery of {} is {} mV.", name, battery);
         if (battery < 3.1) {
           getEnvironment().getNotifyService().lowBattery("Room sensor in " + name, battery);
         }
      }
      lastPacket = now;
      if (temp > -30 && temp < 40)
        getEnvironment().getCosmService().updateDatapoint(name, temp);
      if (battery != null) {
        event(ZoneEvent.temperature(temp, battery));        
      } else {
        event(ZoneEvent.temperature(temp));
      }
    }
  }
  
  @Override
  public String getType() {
    return "RoomSensor";
  }
  
  public Double getBattery() {
    return battery;
  }
  
}
