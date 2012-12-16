package nl.ypmania.env;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import nl.ypmania.NotifyService;
import nl.ypmania.fs20.FS20Packet;
import nl.ypmania.fs20.FS20Service;
import nl.ypmania.rf12.RF12Packet;
import nl.ypmania.visonic.VisonicPacket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

public class Environment {
  private static final Logger log = LoggerFactory.getLogger(Environment.class);
  
  private List<Receiver> receivers = new ArrayList<Receiver>();
  
  private @Autowired NotifyService notifyService;
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
    Calendar morning = Calendar.getInstance();
    morning.set(Calendar.HOUR_OF_DAY, 3);
    morning.set(Calendar.MINUTE, 0);
    morning.set(Calendar.SECOND, 0);
    morning.set(Calendar.MILLISECOND, 0);
    morning.add(Calendar.DAY_OF_MONTH, 1);
    timer.scheduleAtFixedRate(new TimerTask() { public void run() { scheduleTimedTasks(); }}, morning.getTime(), 1000l * 60 * 60 * 24);
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
  
  public NotifyService getNotifyService() {
    return notifyService;
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

  public void receive(RF12Packet packet) {
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
    public TimeOfDay duringWeekendPlusHours(final int hours) {
      return new TimeOfDay() {
        public Calendar getTime(Calendar day) {
          Calendar time = TimeOfDay.this.getTime(day);
          int dayOfWeek = day.get(Calendar.DAY_OF_WEEK);
          if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            time.add(Calendar.HOUR, hours);            
          }
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
      day.setTimeZone(TimeZone.getTimeZone("Europe/Amsterdam"));
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
  
  public abstract class TimedTask {
    TimeOfDay start;
    TimeOfDay stop;
    public TimedTask(TimeOfDay start, TimeOfDay stop) {
      this.start = start;
      this.stop = stop;
    }
    public void setupToday() {
      final Calendar now = Calendar.getInstance();
      final Calendar startToday = start.getTime((Calendar) now.clone());
      final Calendar stopToday = stop.getTime((Calendar) now.clone());
      log.info("Task for today starts {} and ends {}", startToday.getTime(), stopToday.getTime());
      if (startToday == null || stopToday == null) return;
      if (startToday.after(stopToday)) return;
      boolean currentlyOn = startToday.before(now) && now.before(stopToday);
      if (currentlyOn) {
        log.debug("Currently on.");
        timer.schedule(new TimerTask() { public void run() { start(stopToday.getTimeInMillis() - now.getTimeInMillis()); }}, 5000);
      }
      if (now.before(startToday)) {
        timer.schedule(new TimerTask() { public void run() { start(stopToday.getTimeInMillis() - startToday.getTimeInMillis()); }}, startToday.getTime());
      }
      if (now.before(stopToday)) {
        timer.schedule(new TimerTask() { public void run() { stop(); }}, stopToday.getTime());
      }
    }
    protected abstract void start(long duration);
    protected void stop() {}
  }

}
