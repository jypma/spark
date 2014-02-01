package nl.ypmania.rf12;

import javax.xml.bind.annotation.XmlTransient;

import nl.ypmania.env.Device;
import nl.ypmania.env.Zone;
import nl.ypmania.env.ZoneEvent;

import org.joda.time.DateTime;
import org.ocpsoft.pretty.time.PrettyTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Doorbell extends Device {
  private static final Logger log = LoggerFactory.getLogger(Doorbell.class);
  
  private int id1, id2;
  private DateTime lastRing = null;
  private Double battery;
  
  public Doorbell (Zone zone, char id1, char id2) {
    super(zone);
    this.id1 = (int) id1;
    this.id2 = (int) id2;
  }
  
  @Override
  public void receive(RF12Packet packet) {
    if (packet.getContents().size() >= 5 && 
        packet.getContents().get(0) == id1 && 
        packet.getContents().get(1) == id2 &&
        packet.getContents().get(4) == 1) {
      log.debug("Sending ack for doorbell");
      sendAck();
      
      if (lastRing != null && lastRing.plusMillis(1000).isAfterNow()) return;
      log.info("Doorbell is ringing");
      lastRing = DateTime.now();
      ring();
      event(ZoneEvent.ring());
      getEnvironment().getNotifyService().doorbell();
    } else if (packet.getContents().size() >= 9 && 
       packet.getContents().get(0) == id1 && 
       packet.getContents().get(1) == id2 &&
       packet.getContents().get(4) == 3) {
      
       int v = (packet.getContents().get(6).byteValue()) * 256 + packet.getContents().get(5);
       double temp = v / 100.0;
       log.info("Old temperature packet. Got temperature {}.", temp);
        
       battery = (packet.getContents().get(8) * 256 + packet.getContents().get(7)) / 100.0;
       log.info("Battery of is {} mV.", battery);
       if (battery < 3) {
         getEnvironment().getNotifyService().lowBattery("Doorbell", battery);
       }
       if (temp > -30 && temp < 40)
         getEnvironment().getCosmService().updateDatapoint("Carport", temp);
       event(ZoneEvent.temperature(temp, battery));        
    } else if (packet.getContents().size() >= 9 && 
        packet.getContents().get(0) == id1 && 
        packet.getContents().get(1) == id2 &&
        packet.getContents().get(4) == 4) {
       
        int v = (packet.getContents().get(6).byteValue()) * 256 + packet.getContents().get(5);
        double temp = v / 100.0;
        log.info("Got temperature {}.", temp);
         
        battery = (packet.getContents().get(8) * 256.0 + packet.getContents().get(7));
        log.info("Battery of is {}%.", battery);
        if (battery < 5) {
          getEnvironment().getNotifyService().lowBattery("Doorbell", battery);
        }
        if (temp > -30 && temp < 40)
          getEnvironment().getCosmService().updateDatapoint("Carport", temp);
        event(ZoneEvent.temperature(temp, battery));        
     }
  }   
  
  @XmlTransient public DateTime getLastRing() {
    return lastRing;
  }
  
  public String getLastRingPretty() {
    return lastRing == null ? "unknown" : new PrettyTime().format(lastRing.toDate());
  }
  
  public Double getBattery() {
    return battery;
  }
  
  private void sendAck() {
    int[] contents = new int[5];
    contents[0] = 'S';
    contents[1] = 'P';
    contents[2] = 'D';
    contents[3] = 'B';
    contents[4] = 2;
    getEnvironment().getRf12Service().queue(new RF12Packet(contents));
  }

  protected void ring() {}
  
  @Override
  public String getType() {
    return "Doorbell";
  }
}
