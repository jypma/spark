package nl.ypmania.rf12.state;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.github.os72.protobuf.dynamic.MessageDefinition;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;

import nl.ypmania.rf12.RF12Packet;

public class Request {
  private static final MessageDefinition msgDef = MessageDefinition.newBuilder("Packet")
      .addField("optional", "uint32", "nodeId", 1)
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

  public static Request fromRF12OrNull(RF12Packet p) {
    try {
      if (p.getHeader() != 4) {
        return null;
      }
      DynamicMessage message = DynamicMessage.parseFrom(schema.getMessageDescriptor("Packet"), p.getContentsBytes());
      return new Request(
          (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("nodeId")));
    } catch (InvalidProtocolBufferException e) {
      return null;
    }
  }
  
  private final int nodeId;

  public Request(int nodeId) {
    this.nodeId = nodeId;
  }
  
  public int getNodeId() {
    return nodeId;
  }
  
  public RF12Packet toRF12Packet() {
    DynamicMessage message = DynamicMessage.newBuilder(schema.getMessageDescriptor("Packet"))
        .setField(schema.getMessageDescriptor("Packet").findFieldByName("nodeId"), nodeId)
        .build();
    byte[] bytes = message.toByteArray();
    int[] ints = new int[bytes.length];
    for (int i = 0; i < ints.length; i++) {
      ints[i] = bytes[i] & 0xFF;
    }
    return new RF12Packet(4, ints);
  }
}
