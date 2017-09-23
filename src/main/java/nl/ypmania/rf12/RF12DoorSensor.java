package nl.ypmania.rf12;

import org.joda.time.DateTime;
import org.ocpsoft.pretty.time.PrettyTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.github.os72.protobuf.dynamic.MessageDefinition;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;

import nl.ypmania.env.Device;
import nl.ypmania.env.Zone;
import nl.ypmania.env.ZoneEvent;

public class RF12DoorSensor extends Device {
    private static final Logger log = LoggerFactory.getLogger(RF12DoorSensor.class);

    private static final MessageDefinition msgDef = MessageDefinition.newBuilder("Packet")
        .addField("optional", "uint32", "sender", 1)
        .addField("optional", "uint32", "seq", 8)
        .addField("optional", "uint32", "supply", 9)
        .addField("optional", "uint32", "open", 10).build();

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
    private static final int sensorId = (int) 'd';

    private final int id;
    private final String name;
    private Double battery = null;
    private DateTime opened = null;

    public RF12DoorSensor(Zone zone, String name, int id) {
        super(zone);
        this.name = name;
        this.id = id;
    }
    
    public Double getBattery() {
        return battery;
    }

    @Override
    public String getType() {
        return "RF12DoorSensor";
    }

    public String getName() {
        return name;
    }
    
    public boolean isOpen() {
        return opened != null;
    }
    
    public String getOpenSince() {
        return opened != null ? new PrettyTime().format(opened.toDate()) : "";
    }    
    
    @Override
    public void receive(RF12Packet packet) {
      try {
        DynamicMessage message = DynamicMessage.parseFrom(schema.getMessageDescriptor("Packet"), packet.getContentsBytes());
        receiveProtobuf(message);
      } catch (InvalidProtocolBufferException e) {}
    }
    
    public boolean isOpenAtLeastSeconds(int seconds) {
      if (opened == null) return false;
      return opened.plusSeconds(seconds).isBeforeNow();
    }
    
    protected void opened() {}
    protected void closed() {}
    
    private void receiveProtobuf(DynamicMessage message) {
        int sender = (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("sender"));
        if (sender == ((sensorId << 8) | id)) {
            int seq = (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("seq"));
            int supply = (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("supply"));
            int open = (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("open"));
            log.info("{}: Temp {}, supply {}, seq {}, open {}", 
                    new Object[] { getName(), supply, seq, open });
            if (message.hasField(schema.getMessageDescriptor("Packet").findFieldByName("supply"))) {
                if (supply > 20) {
                  battery = supply / 1000.0;
                  getEnvironment().gauge(getZone(), name + ".supply", supply);                        
                } else {
                  log.warn("Suprious supply in packet");          
                }
            }            
            if (message.hasField(schema.getMessageDescriptor("Packet").findFieldByName("open"))) {
                if (open == 0 || open == 1) {
                  getEnvironment().gauge(getZone(), name + ".open", open);                        
                } else {
                  log.warn("Suprious open in packet");          
                }
                if ((open != 0) && (opened == null)) {
                    opened = DateTime.now();
                    event(ZoneEvent.opened());
                    opened();
                } else if (open == 0) {
                    if (opened != null) {
                        event(ZoneEvent.closed());
                        closed();
                    }
                    opened = null;
                }                
            }
        }
    }
    
    
}
