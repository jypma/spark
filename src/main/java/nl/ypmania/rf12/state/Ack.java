package nl.ypmania.rf12.state;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.github.os72.protobuf.dynamic.MessageDefinition;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;

import nl.ypmania.rf12.RF12Packet;

public class Ack {
  private static final MessageDefinition msgDef = MessageDefinition.newBuilder("Packet")
      .addField("optional", "uint32", "nodeId", 1)
      .addField("optional", "uint32", "seq", 2)
      .build();

  private static DynamicSchema schema() {
      DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();
      schemaBuilder.setName("Packet.proto");
      schemaBuilder.addMessageDefinition(msgDef);
      try {
          return schemaBuilder.build();
      } catch (DescriptorValidationException e) {
          throw new RuntimeException(e);
      }
  }

  private static final DynamicSchema schema = schema();

  public static Ack fromRF12OrNull(RF12Packet p) {
    try {
      if (p.getHeader() != 1) {
        return null;
      }
      DynamicMessage message = DynamicMessage.parseFrom(schema.getMessageDescriptor("Packet"), p.getContentsBytes());
      return new Ack(
          (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("nodeId")),
          (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("seq")));
    } catch (InvalidProtocolBufferException e) {
      return null;
    }
  }
  
  private final int nodeId;
  private final int seq;

  public Ack(int nodeId, int seq) {
    this.nodeId = nodeId;
    this.seq = seq;
  }
  
  public int getNodeId() {
    return nodeId;
  }
  
  public int getSeq() {
    return seq;
  }
  
  public RF12Packet toRF12Packet() {
    DynamicMessage message = DynamicMessage.newBuilder(schema.getMessageDescriptor("Packet"))
        .setField(schema.getMessageDescriptor("Packet").findFieldByName("nodeId"), nodeId)
        .setField(schema.getMessageDescriptor("Packet").findFieldByName("seq"), seq)
        .build();
    byte[] bytes = message.toByteArray();
    int[] ints = new int[bytes.length];
    for (int i = 0; i < ints.length; i++) {
      ints[i] = bytes[i] & 0xFF;
    }
    return new RF12Packet(1, ints);
  }
}
