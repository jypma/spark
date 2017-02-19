package nl.ypmania.rf12;

import nl.ypmania.env.Device;
import nl.ypmania.env.Zone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.github.os72.protobuf.dynamic.MessageDefinition;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;

public class ElectricityMeter extends Device {
  private static final int sensorId = (int) 'R';
  private static final Logger log = LoggerFactory.getLogger(ElectricityMeter.class);

  private static final MessageDefinition msgDef = MessageDefinition.newBuilder("Packet") 
      .addField("optional", "uint32", "sender", 1)
      .addField("optional", "uint32", "seq", 8)
      .addField("optional", "uint32", "electricity", 9)
      .build();

  private static DynamicSchema schema() {
    DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();
    schemaBuilder.setName("MeterBoxSensorPacket.proto");
    schemaBuilder.addMessageDefinition(msgDef);
    try {
      return schemaBuilder.build();
    } catch (DescriptorValidationException e) {
      throw new RuntimeException(e);
    }
  }
  private static final DynamicSchema schema = schema();
  
  private final int roomId;
  private long electricityWh = -1;
  private long electricityWhTime = -1;
  
  private long electricityW;

  public ElectricityMeter (Zone zone, int roomId) {
    super(zone);
    this.roomId = roomId;
  }

  @Override
  public String getType() {
    return "ElectricityMeter";
  }
  
  public long getElectricityW() {
    return electricityW;
  }
  
  public long getElectricityWh() {
    return electricityWh;
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
  
  private void receiveProtobuf(DynamicMessage message) {
    int sender = (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("sender"));
    if (sender == ((sensorId << 8) | roomId)) {
      int seq = (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("seq"));
      int electricity = (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("electricity"));
      log.debug("{} received seq {}, electricity {}", new Object[] { roomId, seq, electricity });
      receiveWh(electricity);
    }    
  }

  private void receiveLegacy(RF12Packet packet) {
    if (packet.getContents().size() >= 13 &&
        packet.getContents().get(0) == sensorId &&
        packet.getContents().get(1) == roomId &&
        packet.getContents().get(4) == 4) {
      receiveElectricity(packet);
    }
  }

  private void receiveElectricity(RF12Packet packet) {
    long Wh = 
        ((long)packet.getContents().get(5)) | 
        ((long)packet.getContents().get(6) << 8) |
        ((long)packet.getContents().get(7) << 16) |
        ((long)packet.getContents().get(8) << 24) |
        ((long)packet.getContents().get(9) << 32) |
        ((long)packet.getContents().get(10) << 40) |
        ((long)packet.getContents().get(11) << 48) |
        ((long)packet.getContents().get(12) << 56);
    receiveWh(Wh);
  }

  private void receiveWh(long Wh) {
    if (electricityWh == -1 || Wh < electricityWh) {
      // first packet, or just rolled over
      electricityW = 0;
    } else {
      long ms = System.currentTimeMillis() - electricityWhTime;
      electricityW = (Wh - electricityWh) * 3600000 / ms;
      if (electricityW < 12000) {
        getEnvironment().gauge(getZone(), "electricity.power", electricityW);        
      }
    }
    electricityWh = Wh;
    electricityWhTime = System.currentTimeMillis();
    
    log.debug("Now consuming {} W", electricityW);
  }
}
