package nl.ypmania.rf12;

import nl.ypmania.env.Zone;

public class HumidityRoomSensor extends RoomSensor {
  private final String hum_name;

  public HumidityRoomSensor (Zone zone, String name, String hum_name, int roomId) {
    super(zone, name, roomId);
    this.hum_name = hum_name;
  }
  
  protected void uploadHumidity(double humidity) {
    getEnvironment().getCosmService().updateDatapoint(hum_name, humidity);
  }

  protected void uploadTemp(double temp) {
    getEnvironment().getCosmService().updateDatapoint(getName(), temp);
  }
  
  
}
