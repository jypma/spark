package nl.ypmania.rf12;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.ypmania.cosm.CosmService.DataPoint;
import nl.ypmania.env.Device;
import nl.ypmania.env.Zone;

public class ElectricityMeter extends Device {
  private static final int sensorId = (int) 'R';
  private static final Logger log = LoggerFactory.getLogger(ElectricityMeter.class);
  
  private final int roomId;
  private final String powerStream;
  private final String energyStream;
  private long electricityWh = 0;
  private long electricityWhOffset = 0;
  private long electricityWhTime = 0;
  private long electricityW;

  public ElectricityMeter (Zone zone, int roomId, String powerStream, String energyStream) {
    super(zone);
    this.roomId = roomId;
    this.powerStream = powerStream;
    this.energyStream = energyStream;
    DataPoint pt = getEnvironment().getCosmService().getDatapoint(energyStream);
    if (pt != null && pt.asLong() != null) {
      Long Wh = pt.asLong();
      if (Wh != null) {
        DateTime time = pt.getTime();
        this.electricityWh = Wh;
        this.electricityWhTime = (time == null) ? System.currentTimeMillis() : time.getMillis();         
        log.debug("Initial energy is {} Wh", Wh);
      }
    }
  }

  @Override
  public String getType() {
    return "ElectricityMeter";
  }
  
  public long getElectricityW() {
    return electricityW;
  }
  
  public long getElectricityWh() {
    return electricityWh;
  }
  
  @Override
  public void receive(RF12Packet packet) {
    if (packet.getContents().size() >= 13 &&
        packet.getContents().get(0) == sensorId &&
        packet.getContents().get(1) == roomId &&
        packet.getContents().get(4) == 4) {
      receiveElectricity(packet);
    }
  }

  private void receiveElectricity(RF12Packet packet) {
    long Wh = 
        ((long)packet.getContents().get(5)) | 
        ((long)packet.getContents().get(6) << 8) |
        ((long)packet.getContents().get(7) << 16) |
        ((long)packet.getContents().get(8) << 24) |
        ((long)packet.getContents().get(9) << 32) |
        ((long)packet.getContents().get(10) << 40) |
        ((long)packet.getContents().get(11) << 48) |
        ((long)packet.getContents().get(12) << 56)
    ;
    log.debug("Received electricity: {} Wh", Wh);
    if (Wh + electricityWhOffset > electricityWh + 1000) {
      log.warn ("More than 1000 Wh above current electrity. Ignoring bogus packet.");
      return;
    }
    if (Wh + electricityWhOffset < electricityWh) {
      log.debug ("Received {} smaller than existing {}. Assuming difference as offset.", Wh, electricityWh);
      electricityWhOffset = (electricityWh - Wh);
    }
    Wh += electricityWhOffset;
    log.debug("Actual electricity: {} Wh", Wh);    
    long now = System.currentTimeMillis();
    if (this.electricityWhTime != 0) {
      long ms = now - electricityWhTime;
      long W = (Wh - electricityWh) * 3600000 / ms;
      getEnvironment().getCosmService().updateDatapoint(powerStream, W);
      this.electricityW = W;
    }
    getEnvironment().getCosmService().updateDatapoint(energyStream, Wh);
    this.electricityWhTime = now;
    this.electricityWh = Wh;
  }
}
