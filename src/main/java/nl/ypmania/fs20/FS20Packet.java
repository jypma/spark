package nl.ypmania.fs20;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class FS20Packet {
  private FS20Address address;
  private Command command;
  private int hashCode;
  private int timerSeconds;
  private boolean received;
  
  public FS20Packet(FS20Address address, Command command) {
    this (address, command, 0);
  }
  
  public FS20Packet(FS20Address address, Command command, int timerSeconds) {
    this (address, command, timerSeconds, false);
  }
  
  public FS20Packet(FS20Address address, Command command, int timerSeconds, boolean received) {
    this.timerSeconds = timerSeconds;
    this.address = address;
    this.command = command;
    this.hashCode = new HashCodeBuilder().append(address).append(command).toHashCode();
    this.received = received;
  }
  
  public boolean isReceived() {
    return received;
  }
  
  public FS20Address getAddress() {
    return address;
  }
  
  public Command getCommand() {
    return command;
  }
  
  public static FS20Packet fromBytes (int[] data) {
    return fromBytes(data, false);
  }
  
  public static FS20Packet fromBytes (int[] data, boolean received) {
    if (data == null) return null;
    if (data.length != 5 && data.length != 6) return null;
    Command cmd = Command.byProtocolValue(data[3] & 0x1F);
    if (cmd == null) return null;
    int timerSeconds = (data.length == 6) ? data[4] : 0;
    return new FS20Packet (new FS20Address(data[0], data[1], data[2]), cmd, toSeconds(timerSeconds), received);
  }
  
  private static int toSeconds(int t) {
    return (int) (Math.pow(2, ((t >>> 4) & 0x0F)) * (t & 0x0F) * 0.25f);
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
    return "" + address + "->" + command + ((timerSeconds != 0) ? "," + timerSeconds + "s" : "");
  }
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof FS20Packet)) return false;
    FS20Packet b = (FS20Packet) obj;
    return address.equals(b.address) && command.equals(b.command) && timerSeconds == b.timerSeconds;
  }
  
  @Override
  public int hashCode() {
    return hashCode;
  }
}
