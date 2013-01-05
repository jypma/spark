package nl.ypmania.rf12;

import nl.ypmania.env.Receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoomSensor extends Receiver {
  private static final Logger log = LoggerFactory.getLogger(RoomSensor.class);
  
  private static final int id = (int) 'R';
  private static final int BRYGGERS = (int) '1';
  private long lastPacket = 0;
  
  @Override
  public void receive(RF12Packet packet) {
    long now = System.currentTimeMillis();
    if (now - lastPacket < 10000) return;
    if (packet.getContents().size() >= 7 && 
        packet.getContents().get(0) == id) {
      int room = packet.getContents().get(1);
      int v = packet.getContents().get(6) * 256 + packet.getContents().get(5);
      double temp = v / 100.0;
      log.info("Got temperature {} in room {}.", temp, room);
      lastPacket = now;
      if (room == BRYGGERS) {
        log.debug("Updating bryggers to " + temp);
        getEnvironment().getCosmService().bryggers(temp);
      }
    }
  }
}
