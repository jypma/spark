package nl.ypmania.rf12;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.ypmania.env.Device;
import nl.ypmania.env.Zone;

// grapes watered: Jun 09, 17:30

public class GardenValve extends Device {
  private static final Logger log = LoggerFactory.getLogger(GardenValve.class);
  private static final int RETRIES = 5;
  private static final TimeZone TIMEZONE = TimeZone.getTimeZone("Europe/Copenhagen");
  
  private final char id;
  private final Zone receiverZone;
  private final int[] valveCounts;
  
  private boolean target1 = false;
  private boolean target2 = false;
  private boolean target3 = false;
  private volatile int on = 0;
  private boolean test = false;

  private int count;
  private long lastEnable = 0;
  private TimerTask resendState;

  public GardenValve(Zone zone, Zone receiverZone, char id, int... valveCounts) {
    super(zone);
    this.receiverZone = receiverZone;
    this.id = id;
    this.valveCounts = valveCounts;
  }
  
  @Override
  public synchronized void receive(RF12Packet packet) {
    if (packet.getContents().size() >= 6 &&
        packet.getContents().get(0) == 'V' &&
        packet.getContents().get(1) == id &&
        packet.getContents().get(4) == 0) {
      count = packet.getContents().get(5);
      log.debug("Received ping, receiver {} has {} valves.", id, count);
      
      if (packet.getContents().size() >= 8) {
        int voltage = packet.getContents().get(6) |
                     (packet.getContents().get(7) << 8);
        log.debug("  Voltage is {}mV", voltage);
        getEnvironment().gauge(getZone(), "valve" + "." + id + ".supply", voltage);      
      }
      sendState();
    }
    
    if (packet.getContents().size() >= 6 &&
        packet.getContents().get(0) == 'V' &&
        packet.getContents().get(1) == id &&
        packet.getContents().get(4) == 1) {
      on = packet.getContents().get(5);
      log.debug("Received state, receiver {} has valve {} on", id, on);
      getEnvironment().gauge(getZone(), "valve" + "." + id + ".on", on);      
      
      if (resendState != null) {
        resendState.cancel();
        resendState = null;
      }
      
      if (packet.getContents().size() >= 9) {
        int voltage = packet.getContents().get(7) |
                     (packet.getContents().get(8) << 8);
        log.debug("  Voltage is {}mV", voltage);
        getEnvironment().gauge(getZone(), "valve" + "." + id + ".supply", voltage);      
      }
    }    
  }
  
  private void sendState() {
    boolean enable;
    if (test || (isNight() && (System.currentTimeMillis() - lastEnable) > 12 * 3600 * 1000)) {
      lastEnable = System.currentTimeMillis();
      enable = true;
    } else {
      enable = false;
    }
    sendState(enable, RETRIES);
  }
  
  private boolean isNight() {
    Calendar calendar = Calendar.getInstance(TIMEZONE);
    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    return hour > 18 || hour <= 6;
  }

  private synchronized void sendState(final boolean enable, final int retries) {
    resendState = null;
    log.debug("Valve {}, enable {}, target1 {}", new Object[] { id, enable, target1 });
    if (retries >= 0) {
      RF12Packet packet = new RF12Packet(31, new int[] { 'S', 'P', 'V', id, 2, (target1 && enable) ? valveCounts[0] : 0 });
      getEnvironment().getRf12Service().queue(receiverZone, packet);
      resendState = new TimerTask() {
        @Override
        public void run() {
          sendState(enable, retries - 1);
        }
      };
      getEnvironment().getTimer().schedule(resendState, 600);
    }
  }
  
  public boolean isTarget1() {
    return target1;
  }
  
  public boolean isTarget2() {
    return target2;
  }
  
  public boolean isTarget3() {
    return target3;
  }
  
  public int getCount() {
    return count;
  }
  
  public int getOn() {
    return on;
  }
  
  public String getId() {
    return "" + id;
  }
  
  @Override
  public String getType() {
    return "gardenvalve";
  }
  
  public boolean isTest() {
    return test;
  }

  public void apply(ValveDTO dto) {
    boolean update = false;
    if (dto.getTarget1() != null) {
      lastEnable = 0;
      if (target1 && !dto.getTarget1()) update = true;
      target1 = dto.getTarget1();
    }
    if (dto.getTarget2() != null) {
      lastEnable = 0;
      if (target2 && !dto.getTarget2()) update = true;
      target2 = dto.getTarget2();
    }
    if (dto.getTarget3() != null) {
      lastEnable = 0;
      if (target3 && !dto.getTarget3()) update = true;
      target3 = dto.getTarget3();
    }
    test = dto.isTest();
    if (update || test) {
      sendState();
    }
  }

}
