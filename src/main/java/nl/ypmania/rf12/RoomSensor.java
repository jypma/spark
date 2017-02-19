package nl.ypmania.rf12;

import nl.ypmania.env.Device;
import nl.ypmania.env.Zone;
import nl.ypmania.env.ZoneEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.github.os72.protobuf.dynamic.MessageDefinition;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Descriptors.DescriptorValidationException;

public class RoomSensor extends Device {
  private static final Logger log = LoggerFactory.getLogger(RoomSensor.class);

  private static final MessageDefinition msgDef = MessageDefinition.newBuilder("Packet") 
      .addField("optional", "uint32", "sender", 1)
      .addField("optional", "uint32", "seq", 8)
      .addField("optional", "uint32", "supply", 9)
      .addField("optional", "sint32", "temp", 10)
      .addField("optional", "uint32", "humidity", 11)
      .addField("optional", "uint32", "lux", 12)
      .addField("optional", "uint32", "motion", 13)
      .build();

  private static DynamicSchema schema() {
    DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();
    schemaBuilder.setName("RoomSensorPacket.proto");
    schemaBuilder.addMessageDefinition(msgDef);
    try {
      return schemaBuilder.build();
    } catch (DescriptorValidationException e) {
      throw new RuntimeException(e);
    }
  }
  private static final DynamicSchema schema = schema();
  
  private static final int sensorId = (int) 'R';
  private final int roomId;
  private long lastPacket = 0;
  private final String name;
  private Double battery = null;
  
  public RoomSensor (Zone zone, String name, int roomId) {
    super(zone);
    this.name = name;
    this.roomId = roomId;
  }
  
  
  @Override
  public void receive(RF12Packet packet) {
    try {
      DynamicMessage message = DynamicMessage.parseFrom(schema.getMessageDescriptor("Packet"), packet.getContentsBytes());
      receiveProtobuf(message);
    } catch (InvalidProtocolBufferException e) {
      receiveLegacy(packet);
    }
  }
  
  public void receiveLegacy(RF12Packet packet) {
    if (packet.getContents().size() >= 7 && 
        packet.getContents().get(0) == sensorId &&
        packet.getContents().get(1) == roomId) {
      if (packet.getContents().get(4) == 1) {
        receiveTemperature(packet, false);        
      } else if (packet.getContents().get(4) == 10) {
        receiveTemperature(packet, true);                
      }
    }
  }

  private void receiveTemperature(RF12Packet packet, boolean batteryIsPercentage) {
    long now = System.currentTimeMillis();
    if (now - lastPacket < 9000) return;
    lastPacket = now;
    
    int v = packet.getContents().get(6) * 256 + packet.getContents().get(5);
    double temp = v / 100.0;
    getEnvironment().gauge(getZone(), name + ".temp", v / 10);
    log.info("Got temperature {} in {}.", temp, getZone().getName());
    
    if (temp > -30 && temp < 40)
      uploadTemp(temp);
    if (battery != null) {
      event(ZoneEvent.temperature(temp, battery));        
    } else {
      event(ZoneEvent.temperature(temp));
    }
    
    if (packet.getContents().size() >= 9) {
      int bat = (packet.getContents().get(8).byteValue() * 256 + packet.getContents().get(7));
      if (batteryIsPercentage) {
        // already OK
        battery = bat * 1.0;
      } else {
        getEnvironment().gauge(getZone(), name + ".supply", bat * 10);
        battery = bat / 100.0; // now in Volt
        log.info("Battery of {} is {}V.", getZone().getName(), battery);
        if (battery > 4.2) battery = 4.2;
        if (battery < 3.0) battery = 3.0;
        battery = (battery - 3.0) / (4.2 - 3.0) * 100;
      }
      log.info("Battery of {} is {}%.", getZone().getName(), battery);
      if (battery < 10 && battery > 0) {
        getEnvironment().getNotifyService().lowBattery("Room sensor in " + getZone().getName(), battery);
      }
    }
    
    if (packet.getContents().size() >= 11) {
      int h = packet.getContents().get(10) * 256 + packet.getContents().get(9);
      getEnvironment().gauge(getZone(), name + ".humidity", h / 10); // tenths of percent, 625 for 62.5
      double humidity = h / 100.0;
      log.info("Got humidity {} in {}.", humidity, getZone().getName());
      event(ZoneEvent.humidity(humidity));
      if (humidity >= 0 && humidity < 100) {
        uploadHumidity(humidity);          
      }
    }    
  }

  protected void uploadHumidity(double humidity) {
    
  }

  protected void uploadTemp(double temp) {
    if (name != null) {
      getEnvironment().getCosmService().updateDatapoint(name, temp);      
    }
  }
  
  public String getName() {
    return name;
  }
  
  @Override
  public String getType() {
    return "RoomSensor";
  }
  
  public Double getBattery() {
    return battery;
  }
  
  private void receiveProtobuf(DynamicMessage message) {
    int sender = (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("sender"));
    if (sender == (('Q' << 8) | roomId)) {
      int seq = (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("seq"));
      int supply = (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("supply"));
      int temp = (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("temp"));
      int humidity = (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("humidity"));
      int motion = (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("motion"));
      int lux = (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("lux"));
      log.info("{}: Temp {}, supply {}, seq {}, humidity {}, motion {}, lux {}", 
          new Object[] { getName(), temp, supply, seq, humidity, motion, lux });
      if (message.hasField(schema.getMessageDescriptor("Packet").findFieldByName("temp"))) {
        if (temp < 400 && temp > -200) {
          event(ZoneEvent.temperature(temp / 10.0));
          getEnvironment().gauge(getZone(), name + ".temp", temp);                  
        } else {
          log.warn("Suprious temp in packet");
        }
      }
      if (message.hasField(schema.getMessageDescriptor("Packet").findFieldByName("supply"))) {
        if (supply > 20) {
          getEnvironment().gauge(getZone(), name + ".supply", supply);                        
        } else {
          log.warn("Suprious supply in packet");          
        }
      }
      if (message.hasField(schema.getMessageDescriptor("Packet").findFieldByName("humidity"))) {
        if (humidity >= 10) {
          getEnvironment().gauge(getZone(), name + ".humidity", humidity);          
        } else {
          log.warn("Suprious humidity in packet");                    
        }
      }
      if (message.hasField(schema.getMessageDescriptor("Packet").findFieldByName("lux"))) {
        getEnvironment().gauge(getZone(), name + ".lux", lux);
      }
      if (motion > 0) {
        event(ZoneEvent.motion());
        getEnvironment().increment(getZone(), name + ".movement");        
      }
    }
  }
  
}
