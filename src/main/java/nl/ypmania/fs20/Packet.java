package nl.ypmania.fs20;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Packet {
  private Address address;
  private Command command;
  private int hashCode;
  
  public Packet(Address address, Command command) {
    this.address = address;
    this.command = command;
    this.hashCode = new HashCodeBuilder().append(address).append(command).toHashCode();
  }
  
  public Address getAddress() {
    return address;
  }
  
  public Command getCommand() {
    return command;
  }
  
  public static Packet fromBytes (int[] data) {
    if (data == null) return null;
    if (data.length != 5) return null;
    Command cmd = Command.byProtocolValue(data[3]);
    if (cmd == null) return null;
    return new Packet (new Address(data[0], data[1], data[2]), cmd);
  }
  
  public int[] toBytes() {
    int[] result = new int[5];
    int checksum = (6 + address.getHouseHigh() + address.getHouseLow() + address.getDevice() 
                      + command.getProtocolValue()) & 0xFF;
    result[0] = address.getHouseHigh();
    result[1] = address.getHouseLow();
    result[2] = address.getDevice();
    result[3] = command.getProtocolValue();
    result[4] = checksum; 
    
    return result;
  }
  
  @Override
  public String toString() {
    return "" + address + "->" + command;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Packet)) return false;
    Packet b = (Packet) obj;
    return address.equals(b.address) && command.equals(b.command);
  }
  
  @Override
  public int hashCode() {
    return hashCode;
  }
}
