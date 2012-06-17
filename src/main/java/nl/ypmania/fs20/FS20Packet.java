package nl.ypmania.fs20;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class FS20Packet {
  private FS20Address address;
  private Command command;
  private int hashCode;
  
  public FS20Packet(FS20Address address, Command command) {
    this.address = address;
    this.command = command;
    this.hashCode = new HashCodeBuilder().append(address).append(command).toHashCode();
  }
  
  public FS20Address getAddress() {
    return address;
  }
  
  public Command getCommand() {
    return command;
  }
  
  public static FS20Packet fromBytes (int[] data) {
    if (data == null) return null;
    if (data.length != 5) return null;
    Command cmd = Command.byProtocolValue(data[3]);
    if (cmd == null) return null;
    return new FS20Packet (new FS20Address(data[0], data[1], data[2]), cmd);
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
    if (!(obj instanceof FS20Packet)) return false;
    FS20Packet b = (FS20Packet) obj;
    return address.equals(b.address) && command.equals(b.command);
  }
  
  @Override
  public int hashCode() {
    return hashCode;
  }
}
