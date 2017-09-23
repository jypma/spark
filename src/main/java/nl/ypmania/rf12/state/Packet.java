package nl.ypmania.rf12.state;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.github.os72.protobuf.dynamic.MessageDefinition;
import com.google.protobuf.ByteString;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;

import nl.ypmania.rf12.RF12Packet;

import com.google.protobuf.Descriptors.DescriptorValidationException;

public class Packet {
  static enum Direction {
    /** spark to node */
    Tx(2), 
    /** node to spark */
    Rx(3);
    
    int header;
    private Direction(int h) {
      header = h;
    }
    
    public int getHeader() {
      return header;
    }
    
    public static Direction ofHeader (int h) {
      for (Direction d: Direction.values()) {
        if (d.header == h) {
          return d;
        }
      }
      return null;
    }
  }
  
  private static final MessageDefinition msgDef = MessageDefinition.newBuilder("Packet")
      .addField("optional", "uint32", "nodeId", 1)
      .addField("optional", "uint32", "seq", 2)
      .addField("optional", "bytes", "body", 3)
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

  public static Packet fromRF12OrNull(RF12Packet p) {
    try {
      Direction d = Direction.ofHeader(p.getHeader());
      if (d == null) {
        return null;
      }
      DynamicMessage message = DynamicMessage.parseFrom(schema.getMessageDescriptor("Packet"), p.getContentsBytes());
      return new Packet(d,
          (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("nodeId")),
          (Integer) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("seq")),
          (ByteString) message.getField(schema.getMessageDescriptor("Packet").findFieldByName("body")));
    } catch (InvalidProtocolBufferException e) {
      return null;
    }
  }
  
  private final Direction direction;
  private final int nodeId;
  private final int seq;
  private final ByteString body;

  public Packet(Direction direction, int nodeId, int seq, ByteString body) {
    this.direction = direction;
    this.nodeId = nodeId;
    this.seq = seq;
    this.body = body;
  }
  
  public ByteString getBody() {
    return body;
  }
  
  public int getNodeId() {
    return nodeId;
  }
  
  public int getSeq() {
    return seq;
  }
  
  public Direction getDirection() {
    return direction;
  }
  
  public RF12Packet toRF12Packet() {
    DynamicMessage message = DynamicMessage.newBuilder(schema.getMessageDescriptor("Packet"))
        .setField(schema.getMessageDescriptor("Packet").findFieldByName("nodeId"), nodeId)
        .setField(schema.getMessageDescriptor("Packet").findFieldByName("seq"), seq)
        .setField(schema.getMessageDescriptor("Packet").findFieldByName("body"), body)
        .build();
    byte[] bytes = message.toByteArray();
    int[] ints = new int[bytes.length];
    for (int i = 0; i < ints.length; i++) {
      ints[i] = bytes[i] & 0xFF;
    }
    return new RF12Packet(direction.header, ints);
  }
}
