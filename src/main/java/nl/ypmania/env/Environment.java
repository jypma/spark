package nl.ypmania.env;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import nl.ypmania.GrowlService;
import nl.ypmania.fs20.FS20Packet;
import nl.ypmania.fs20.FS20Service;
import nl.ypmania.visonic.VisonicPacket;

public class Environment {
  private static final Logger log = LoggerFactory.getLogger(Environment.class);
  
  private List<Receiver> receivers = new ArrayList<Receiver>();
  
  private @Autowired GrowlService growlService;
  private @Autowired FS20Service fs20Service;
  private long rf868UsageEnd = System.currentTimeMillis();
  private ConcurrentLinkedQueue<Runnable> rf868Actions = new ConcurrentLinkedQueue<Runnable>();
  private Timer timer = new Timer();
  private TimerTask runRf868 = null;
  
  private SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(new Location("55.683334", "12.55"), "GMT");
  
  @PreDestroy
  public void stop() {
    timer.cancel();
  }
  
  public boolean isLight() {
    Calendar now = Calendar.getInstance();
    Calendar sunrise = calculator.getOfficialSunriseCalendarForDate(now);
    Calendar sunset = calculator.getOfficialSunsetCalendarForDate(now);
    if (sunrise == null || sunset == null) {
      return true;
    }
    if (sunrise.before(sunset)) {
      sunrise.add(Calendar.HOUR_OF_DAY, 1);
      sunset.add(Calendar.HOUR_OF_DAY, -1);
      return sunrise.before(now) && sunset.after(now);
    } else { // sunrise is after sunset
      sunrise.add(Calendar.HOUR_OF_DAY, 1);
      sunset.add(Calendar.HOUR_OF_DAY, -1);
      return sunset.after(now) || sunrise.before(now); 
    }
  }
  
  public boolean isDark() {
    return !isLight();
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
  
  public synchronized void setRf868UsageEnd (long delayFromNow) {
    rf868UsageEnd = Math.max(rf868UsageEnd, System.currentTimeMillis() + delayFromNow);
  }
  
  public synchronized void onRf868Clear (Runnable action) {
    long now = System.currentTimeMillis();
    if (rf868UsageEnd < now) {
      action.run();
    } else {
      log.info ("Queueing action, since RF868 in use until {} (in {} msecs)", new Date(rf868UsageEnd), rf868UsageEnd - now);
      rf868Actions.add(action);
      if (runRf868 == null) {
        scheduleRf868();
      }
    }
  }

  private synchronized void scheduleRf868() {
    runRf868 = new TimerTask() {
      @Override
      public void run() {
        synchronized (Environment.this) {
          runRf868 = null;
          if (rf868Actions.isEmpty()) return;
          long now = System.currentTimeMillis();
          if (rf868UsageEnd > now) {
            log.debug ("Still waiting in timer, since RF868 in use until {} (in {} msecs)", new Date(rf868UsageEnd), rf868UsageEnd - now);
            scheduleRf868();
            return;
          }
        }
        Runnable action = rf868Actions.remove();
        action.run();
        if (!rf868Actions.isEmpty()) {
          scheduleRf868();
        }
      }
    };
    timer.schedule(runRf868, new Date(rf868UsageEnd));
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
