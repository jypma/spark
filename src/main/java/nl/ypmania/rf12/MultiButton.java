package nl.ypmania.rf12;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.github.os72.protobuf.dynamic.MessageDefinition;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Descriptors.DescriptorValidationException;

import nl.ypmania.env.Device;
import nl.ypmania.env.Zone;

public class MultiButton extends Device {
  private static final Logger log = LoggerFactory.getLogger(MultiButton.class);

  private static final MessageDefinition msgDef = MessageDefinition.newBuilder("Packet") 
      .addField("optional", "uint32", "sender", 1)
      .addField("optional", "uint32", "seq", 8)
      .addField("optional", "uint32", "supply", 9)
      .addField("optional", "uint32", "button", 10)
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
  
  private final int id;
  private final String name;

  public MultiButton(Zone zone, String name, int id) {
    super(zone);
    this.name = name;
    this.id = id;
  }
  
  @Override
  public void receive(RF12Packet packet) {
    try {
      DynamicMessage message = DynamicMessage.parseFrom(schema.getMessageDescriptor("Packet"), packet.getContentsBytes());
      receiveProtobuf(message);
    } catch (InvalidProtocolBufferException e) {
      log.warn("Invalid packet {}: {}", packet, e);
    }
  }
  
  private void receiveProtobuf(DynamicMessage message) {
    int sender = (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("sender"));
    if (sender == (('B' << 8) | id)) {
      int seq = (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("seq"));
      int supply = (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("supply"));
      int button = (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("button"));
      log.info("{}.{}: supply {}, seq {}, button {}", new Object[] { getZone(), name, supply, seq, button }); 
      if (message.hasField(schema.getMessageDescriptor("Packet").findFieldByName("supply"))) {
        if (supply > 20) {
          getEnvironment().gauge(getZone(), name + ".supply", supply);                        
        } else {
          log.warn("Suprious supply in packet");          
        }
      }
      if (message.hasField(schema.getMessageDescriptor("Packet").findFieldByName("button"))) {
        if ((button & 1) > 0) {
          getEnvironment().increment(getZone(), "button." + id + ".1");
          onButton(1);
        } else if ((button & 2) > 0) {
          getEnvironment().increment(getZone(), "button." + id + ".2");          
          onButton(2);          
        } else if ((button & 4) > 0) {
          getEnvironment().increment(getZone(), "button." + id + ".3");          
          onButton(3);
        }
      }
    }    
  }

  protected void onButton(int i) {}

  @Override
  public String getType() {
    return "MultiButton";
  }
  
  
}
