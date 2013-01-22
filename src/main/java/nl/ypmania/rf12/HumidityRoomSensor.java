package nl.ypmania.rf12;

import nl.ypmania.env.Zone;

public class HumidityRoomSensor extends RoomSensor {
  public HumidityRoomSensor (Zone zone, String name, int roomId) {
    super(zone, name, roomId);
  }
  
  protected void uploadHumidity(double humidity) {
    getEnvironment().getCosmService().updateDatapoint(getName() + "_H", humidity);
  }

  protected void uploadTemp(double temp) {
    getEnvironment().getCosmService().updateDatapoint(getName() + "_T", temp);
  }
  
  
}
