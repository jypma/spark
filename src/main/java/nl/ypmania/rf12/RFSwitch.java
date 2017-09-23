package nl.ypmania.rf12;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.github.os72.protobuf.dynamic.MessageDefinition;
import com.google.protobuf.ByteString;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Descriptors.DescriptorValidationException;

import nl.ypmania.env.Actuator;
import nl.ypmania.env.Environment;
import nl.ypmania.env.Receiver;
import nl.ypmania.env.Switch;
import nl.ypmania.env.Zone;
import nl.ypmania.rf12.state.TxState;

public class RFSwitch extends Receiver {
  private static final MessageDefinition msgDef = MessageDefinition.newBuilder("Packet") 
      .addField("optional", "uint32", "outputs", 1)
      .build();

  private static DynamicSchema schema() {
    DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();
    schemaBuilder.setName("RFSwitch.proto");
    schemaBuilder.addMessageDefinition(msgDef);
    try {
      return schemaBuilder.build();
    } catch (DescriptorValidationException e) {
      throw new RuntimeException(e);
    }
  }
  private static final DynamicSchema schema = schema();

  private volatile int outputs = 0;
  
  private final Zone zone;
  private final Channel channel1;
  private final Channel channel2;
  private final Channel channel3;
  private final Channel channel4;
  private final TxState state;
  
  public RFSwitch(Environment env, Zone zone, int id, String channel1, String channel2, String channel3, String channel4) {
    this.zone = zone;
    this.channel1 = (channel1 == null) ? null : new Channel(channel1, 0);
    this.channel2 = (channel1 == null) ? null : new Channel(channel2, 1);
    this.channel3 = (channel1 == null) ? null : new Channel(channel3, 2);
    this.channel4 = (channel1 == null) ? null : new Channel(channel4, 3);
    this.state = new TxState(env, zone, 'r' << 8 | id, toProtobuf(outputs));
  }

  @Override
  public void receive(RF12Packet packet) {
    state.receive(packet);
  }
  
  private ByteString toProtobuf(int outputs) {
    return DynamicMessage.newBuilder(schema.getMessageDescriptor("Packet"))
        .setField(schema.getMessageDescriptor("Packet").findFieldByName("outputs"), outputs)
        .build()
        .toByteString();
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
      return "RFSwitchChannel";
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
