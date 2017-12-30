package nl.ypmania.rf12;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.github.os72.protobuf.dynamic.MessageDefinition;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;

import nl.ypmania.env.Actuator;
import nl.ypmania.env.Device;
import nl.ypmania.env.Environment;
import nl.ypmania.env.Switch;
import nl.ypmania.env.Zone;
import nl.ypmania.rf12.state.RxTxState;

public class NimhCharger extends Device {
  private static final Logger log = LoggerFactory.getLogger(NimhCharger.class);
  
  private static final MessageDefinition outputsDef = MessageDefinition.newBuilder("Packet") 
      .addField("optional", "uint32", "outputs", 1)
      .build();

  private static DynamicSchema outputsSchema() {
    DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();
    schemaBuilder.setName("RFSwitch.proto");
    schemaBuilder.addMessageDefinition(outputsDef);
    try {
      return schemaBuilder.build();
    } catch (DescriptorValidationException e) {
      throw new RuntimeException(e);
    }
  }
  
  private static final DynamicSchema outputsSchema = outputsSchema();

  private static final MessageDefinition statusDef = MessageDefinition.newBuilder("Packet") 
      .addField("optional", "uint32", "sender", 1)
      .addField("optional", "uint32", "supplyVoltage", 2)
      .addField("optional", "uint32", "solarVoltage", 3)
      .addField("optional", "uint32", "charges", 4)
      .addField("optional", "sint32", "temperature", 5)
      .addField("optional", "uint32", "stopReason", 6)
      .build();

  private static Descriptor statusDesc() {
    DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();
    schemaBuilder.setName("RFSwitch.proto");
    schemaBuilder.addMessageDefinition(statusDef);
    try {
      return schemaBuilder.build().getMessageDescriptor("Packet");
    } catch (DescriptorValidationException e) {
      throw new RuntimeException(e);
    }
  }
  private static final Descriptor statusDesc = statusDesc();
  
  private volatile int outputs = 0;
  
  private final Zone zone;
  private final Channel channel1;
  private final Channel channel2;
  private final Channel channel3;
  private final Channel channel4;
  private final RxTxState state;
  private final int senderId;
  private final int id;
  
  public NimhCharger(Environment env, Zone zone, int id, String channel1, String channel2, String channel3, String channel4) {
    super(zone);
    this.zone = zone;
    this.id = id;
    this.channel1 = (channel1 == null) ? null : new Channel(channel1, 0);
    this.channel2 = (channel1 == null) ? null : new Channel(channel2, 1);
    this.channel3 = (channel1 == null) ? null : new Channel(channel3, 2);
    this.channel4 = (channel1 == null) ? null : new Channel(channel4, 3);
    senderId = 'n' << 8 | id;
    this.state = new RxTxState(env, zone, senderId, toProtobuf(outputs));
  }
  
  @Override
  public String getType() {
    return "NimhCharger";
  }

  private ByteString toProtobuf(int outputs) {
    return DynamicMessage.newBuilder(outputsSchema.getMessageDescriptor("Packet"))
        .setField(outputsSchema.getMessageDescriptor("Packet").findFieldByName("outputs"), outputs)
        .build()
        .toByteString();
  }

  @Override
  public void receive(RF12Packet p) {
    state.receive(p);
    if (p.getHeader() == 42) {
      try {
        receiveStatus(DynamicMessage.parseFrom(statusDesc, p.getContentsBytes()));
      } catch (InvalidProtocolBufferException e) {}      
    }
  }
  
  private void receiveStatus(DynamicMessage message) {
    int sender = (Integer) message.getField(statusDesc.findFieldByName("sender"));
    if (sender != senderId) return;
    
    if (message.hasField(statusDesc.findFieldByName("supplyVoltage"))) {
      int supply = (Integer) message.getField(statusDesc.findFieldByName("supplyVoltage"));
      log.debug("{}: supply {}mV", id, supply);
      if (supply > 200) {
        getEnvironment().gauge(getZone(), "nimh." + id + ".supply", supply);                        
      }       
    }
    if (message.hasField(statusDesc.findFieldByName("solarVoltage"))) {
      int solar = (Integer) message.getField(statusDesc.findFieldByName("solarVoltage"));
      log.debug("{}: solar {}mV", id, solar);
      getEnvironment().gauge(getZone(), "nimh." + id + ".solar", solar);                        
    }
    if (message.hasField(statusDesc.findFieldByName("charges"))) {
      int charges = (Integer) message.getField(statusDesc.findFieldByName("charges"));
      log.debug("{}: charges {}", id, charges);
      getEnvironment().gauge(getZone(), "nimh." + id + ".charges", charges);                        
    }
    if (message.hasField(statusDesc.findFieldByName("temperature"))) {
      int temperature = (Integer) message.getField(statusDesc.findFieldByName("temperature"));
      log.debug("{}: temperature {} C", id, temperature / 10.0);
      if (temperature < 500 && temperature > -200) {
        getEnvironment().gauge(getZone(), "nimh." + id + ".temp", temperature);                                
      }
    }
    if (message.hasField(statusDesc.findFieldByName("stopReason"))) {
      int stopReason = (Integer) message.getField(statusDesc.findFieldByName("stopReason"));
      log.debug("{}: stopReason {}", id, stopReason);
    }
  }

  public final class Channel extends Actuator implements Switch {
    private final String name;
    private final int bit;
    
    private Channel(String name, int bit) {
      super(zone);
      this.name = name;
      this.bit = bit;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    protected Runnable getOnCommand() {
      return new Runnable() {
        @Override
        public void run() {
          turnOn();
        }
      };
    }

    @Override
    protected void turnOff() {
      outputs &= ~(1 << bit);
      state.setState(toProtobuf(outputs));      
    }
    
    public void turnOn() {
      outputs |= (1 << bit);
      state.setState(toProtobuf(outputs));
    }

    @Override
    public boolean isOn() {
      return (outputs & (1 << bit)) != 0;
    }

    @Override
    public String getType() {
      return "NimhChargerChannel";
    }
  }
  
  public Channel getChannel1() {
    return channel1;
  }
  
  public Channel getChannel2() {
    return channel2;
  }
  
  public Channel getChannel3() {
    return channel3;
  }
  
  public Channel getChannel4() {
    return channel4;
  }
}
