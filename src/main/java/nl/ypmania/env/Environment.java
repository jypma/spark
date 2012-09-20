package nl.ypmania.env;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import nl.ypmania.GrowlService;
import nl.ypmania.fs20.FS20Packet;
import nl.ypmania.fs20.FS20Service;
import nl.ypmania.visonic.VisonicPacket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

public class Environment {
  private static final Logger log = LoggerFactory.getLogger(Environment.class);
  
  private List<Receiver> receivers = new ArrayList<Receiver>();
  
  private @Autowired GrowlService growlService;
  private @Autowired FS20Service fs20Service;
  private long rf868UsageEnd = System.currentTimeMillis();
  private ConcurrentLinkedQueue<Runnable> rf868Actions = new ConcurrentLinkedQueue<Runnable>();
  private Timer timer = new Timer();
  private TimerTask runRf868 = null;
  private List<TimedTask> timedTasks = new ArrayList<TimedTask>();
  
  private SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(new Location("55.683334", "12.55"), "GMT");
  
  @PreDestroy
  public void stop() {
    timer.cancel();
  }
  
  @PostConstruct
  public void startTimer() {
    Calendar midnight = Calendar.getInstance();
    midnight.set(Calendar.HOUR_OF_DAY, 0);
    midnight.set(Calendar.MINUTE, 0);
    midnight.set(Calendar.SECOND, 0);
    midnight.set(Calendar.MILLISECOND, 0);
    midnight.add(Calendar.DAY_OF_MONTH, 1);
    timer.scheduleAtFixedRate(new TimerTask() { public void run() { scheduleTimedTasks(); }}, midnight.getTime(), 1000l * 60 * 60 * 24);
  }
  
  protected synchronized void scheduleTimedTasks() {
    for (TimedTask task: timedTasks) task.setupToday();
  }

  public synchronized void register (TimedTask task) {
    timedTasks.add(task);
    task.setupToday();
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
  
  public abstract class TimeOfDay {
    public abstract Calendar getTime(Calendar day);
    public TimeOfDay plusHours(final int hours) {
      return new TimeOfDay() {
        public Calendar getTime(Calendar day) {
          Calendar time = TimeOfDay.this.getTime(day);
          time.add(Calendar.HOUR, hours);
          return time;
        }
      };
    }
  }
  
  public class FixedTime extends TimeOfDay {
    private int hour;
    private int minute;
    public FixedTime (int hour, int minute) {
      this.hour = hour;
      this.minute = minute;
    }
    public Calendar getTime(Calendar day) {
      day.set(Calendar.HOUR_OF_DAY, hour);
      day.set(Calendar.MINUTE, minute);
      return day;
    }
  }
  
  public final TimeOfDay SUNRISE = new TimeOfDay() {
    public Calendar getTime(Calendar day) {
      return calculator.getOfficialSunriseCalendarForDate(day);
    };
  };
  
  public final TimeOfDay SUNSET = new TimeOfDay() {
    public Calendar getTime(Calendar day) {
      return calculator.getOfficialSunsetCalendarForDate(day);
    };
  };
  
  public class TimedTask {
    TimeOfDay start;
    TimeOfDay stop;
    Runnable startAction;
    Runnable stopAction;
    public TimedTask(TimeOfDay start, TimeOfDay stop, Runnable startAction,
        Runnable stopAction) {
      this.start = start;
      this.stop = stop;
      this.startAction = startAction;
      this.stopAction = stopAction;
    }
    public void setupToday() {
      Calendar now = Calendar.getInstance();
      Calendar startToday = start.getTime((Calendar) now.clone());
      Calendar stopToday = stop.getTime((Calendar) now.clone());
      log.info("Task for today starts {} and ends {}", startToday.getTime(), stopToday.getTime());
      if (startToday == null || stopToday == null) return;
      if (startToday.after(stopToday)) return;
      boolean currentlyOn = startToday.before(now) && now.before(stopToday);
      if (currentlyOn) {
        log.debug("Currently on.");
        timer.schedule(new TimerTask() { public void run() { startAction.run(); }}, 5000);
      }
      if (now.before(startToday)) {
        timer.schedule(new TimerTask() { public void run() { startAction.run(); }}, startToday.getTime());
      }
      if (now.before(stopToday)) {
        timer.schedule(new TimerTask() { public void run() { stopAction.run(); }}, stopToday.getTime());
      }
    }
  }
}
