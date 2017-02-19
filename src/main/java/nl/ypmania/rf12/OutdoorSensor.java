package nl.ypmania.rf12;

import java.util.List;

import nl.ypmania.env.Device;
import nl.ypmania.env.Zone;
import nl.ypmania.env.ZoneEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.github.os72.protobuf.dynamic.MessageDefinition;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;

public class OutdoorSensor extends Device {
  private static final Logger log = LoggerFactory.getLogger(OutdoorSensor.class);

  private static final MessageDefinition msgDef = MessageDefinition.newBuilder("Packet") // message Person
      .addField("optional", "uint32", "sender", 1)
      .addField("optional", "uint32", "seq", 8)
      .addField("optional", "uint32", "supply", 9)
      .addField("optional", "sint32", "temp", 10)
      .addField("optional", "uint32", "soil", 11)
      .build();
  
  private static DynamicSchema schema() {
    DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();
    schemaBuilder.setName("OutdoorSensorPacket.proto");


    
    schemaBuilder.addMessageDefinition(msgDef);
    try {
      return schemaBuilder.build();
    } catch (DescriptorValidationException e) {
      throw new RuntimeException(e);
    }
  }
  private static final DynamicSchema schema = schema();
  
  private final String name;
  private final int id;

  public OutdoorSensor (Zone zone, String name, int id) {
    super(zone);
    this.name = name;
    this.id = id;
  }

  @Override
  public String getType() {
    return "OutdoorSensor";
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
  
  private void receiveLegacy(RF12Packet packet) {
    List<Integer> c = packet.getContents();
    if (c.size() >= 9 && 
        c.get(0) == 'O' &&
        c.get(1) == id) {
      int soil = 0;
      boolean soilValid = false; 
      boolean soilExt = false;
      int temp = c.get(5) | (c.get(6) << 8);
      int supply = c.get(7) | (c.get(8) << 8);
      if (c.size() >= 13) {
        // has extended soil range
        soilExt = true;
        soilValid = true;
        soil = c.get(9) | (c.get(10) << 8) | (c.get(11) << 16) | (c.get(12) << 24);
        soilValid = soil < 500000;
      }
      log.info("{}: Soil {} (valid:{}, ext:{}), temp {}, supply {}", new Object[] { name, soil, soilValid, soilExt, temp, supply });
      event(ZoneEvent.temperature(temp / 10.0));
      if (soilValid) getEnvironment().gauge(getZone(), name + ".soil", soil);
      if (temp < 400 && temp > -200) {
        getEnvironment().gauge(getZone(), name + ".temp", temp);        
      }
      getEnvironment().gauge(getZone(), name + ".supply", supply);
    }
  }

  private void receiveProtobuf(DynamicMessage message) {
    int sender = (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("sender"));
    if (sender == (('O' << 8) | id)) {
      int seq = (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("seq"));
      int supply = (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("supply"));
      int temp = (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("temp"));
      int soil = (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("soil"));
      log.info("{}: Soil {}, temp {}, supply {}, seq {}", new Object[] { name, soil, temp, supply, seq });
      if (message.hasField(schema.getMessageDescriptor("Packet").findFieldByName("temp")) && temp < 400 && temp > -200) {
        event(ZoneEvent.temperature(temp / 10.0));
        getEnvironment().gauge(getZone(), name + ".temp", temp);        
      }
      if (message.hasField(schema.getMessageDescriptor("Packet").findFieldByName("soil"))) {
        if (soil < 500000) getEnvironment().gauge(getZone(), name + ".soil", soil);
      }
      if (message.hasField(schema.getMessageDescriptor("Packet").findFieldByName("supply"))) {
        getEnvironment().gauge(getZone(), name + ".supply", supply);              
      }
    }
  }
}
