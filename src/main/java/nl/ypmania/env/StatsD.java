package nl.ypmania.env;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;

@Component
public class StatsD {
  private StatsDClient statsd = new NonBlockingStatsDClient("spark", 
      StringUtils.defaultString(System.getProperty("statsd.ip"), "localhost"), 8125);

  public void gauge(Zone zone, String name, long value) {
    statsd.gauge(getStatsDAspect(zone, name), value);
  }

  public void increment(Zone zone, String name) {
     statsd.increment(getStatsDAspect(zone, name));
  }

  private String getStatsDAspect(Zone zone, String name) {
    return (zone.getPath() + "." + name).replaceAll(" -/", "_");
  }

  public void count(String name, long value) {
    statsd.count(name, value);
  }
  
}
