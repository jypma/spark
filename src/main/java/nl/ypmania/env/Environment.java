package nl.ypmania.env;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import nl.ypmania.GrowlService;
import nl.ypmania.fs20.FS20Packet;
import nl.ypmania.fs20.FS20Service;
import nl.ypmania.visonic.VisonicPacket;

public class Environment {
  private List<Receiver> receivers = new ArrayList<Receiver>();
  
  private @Autowired GrowlService growlService;
  private @Autowired FS20Service fs20Service;
  private Timer timer = new Timer();
  
  private SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(new Location("55.683334", "12.55"), "GMT");
  
  @PreDestroy
  public void stop() {
    timer.cancel();
  }
  
  public boolean isDay() {
    Calendar now = Calendar.getInstance();
    Calendar sunrise = calculator.getOfficialSunriseCalendarForDate(now);
    Calendar sunset = calculator.getOfficialSunsetCalendarForDate(now);
    if (sunrise == null || sunset == null) {
      return true;
    }
    if (sunrise.before(sunset)) {
      return sunrise.before(now) && sunset.after(now);
    } else { // sunrise is after sunset
      return sunset.after(now) || sunrise.before(now); 
    }
  }
  
  public boolean isNight() {
    return !isDay();
  }
  
  public Timer getTimer() {
    return timer;
  }
  
  public GrowlService getGrowlService() {
    return growlService;
  }
  
  public FS20Service getFs20Service() {
    return fs20Service;
  }
  
  public void setReceivers (Receiver... receivers) {
    this.receivers = Arrays.asList(receivers);
    for (Receiver r: receivers) {
      r.setEnvironment(this);
    }
  }
  
  public void receive (FS20Packet packet) {
    for (Receiver receiver: receivers) {
      receiver.receive(packet);
    }
  }
  
  public void receive (VisonicPacket packet) {
    for (Receiver receiver: receivers) {
      receiver.receive(packet);
    }    
  }

  @SuppressWarnings("unchecked")
  public <T extends Receiver> List<T> getAll (Class<T> type) {
    List<T> result = new ArrayList<T>();
    for (Receiver r: receivers) {
      if (type.isInstance(r))
        result.add((T) r);
    }
    return result;
  }
  
}
