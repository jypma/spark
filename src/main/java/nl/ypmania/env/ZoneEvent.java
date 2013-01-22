package nl.ypmania.env;

import org.joda.time.DateTime;

public class ZoneEvent {
  private final DateTime time;
  private final Type type;
  
  //TODO refactor battery out, each value its own event
  private final double batteryMillivolt;
  private final long count;
  private final double temperature;
  
  private ZoneEvent (Type type, long count, double batteryMillivolt, double temperature) {
    this.count = count;
    this.temperature = temperature;
    this.time = DateTime.now();
    this.type = type;
    this.batteryMillivolt = batteryMillivolt;
  }
  
  public static ZoneEvent motion () {
    return new ZoneEvent(Type.MOTION, 0, 0, 0);
  }
  
  public static ZoneEvent opened () {
    return new ZoneEvent(Type.OPENED, 0, 0, 0);
  }
  
  public static ZoneEvent closed () {
    return new ZoneEvent(Type.CLOSED, 0, 0, 0);
  }
  
  public static ZoneEvent count (long value) {
    return new ZoneEvent(Type.COUNT, value, 0, 0);
  }
  
  public static ZoneEvent ring () {
    return new ZoneEvent(Type.RING, 0, 0, 0);
  }
  
  public static ZoneEvent temperature (double temperature, double batteryMillivolt) {
    return new ZoneEvent(Type.TEMPERATURE, 0, batteryMillivolt, temperature);
  }
  
  public static ZoneEvent temperature (double temperature) {
    return new ZoneEvent(Type.TEMPERATURE, 0, 0, temperature);
  }
  
  public static ZoneEvent humidity (double humidity) {
    return new ZoneEvent(Type.HUMIDITY, 0, 0, humidity);
  }
  
  public enum Type {
    MOTION, OPENED, CLOSED, COUNT, RING, TEMPERATURE, HUMIDITY
  }
   
  public DateTime getTime() {
    return time;
  }
  
  public double getBatteryMillivolt() {
    return batteryMillivolt;
  }
  
  public Type getType() {
    return type;
  }
  
  public long getCount() {
    return count;
  }
  
  public double getValue() {
    return temperature;
  }
}
