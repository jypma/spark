package nl.ypmania.fs20;

import java.io.IOException;
import java.io.OutputStream;

public class Packet {
  private Address address;
  private Command command;
  
  public Packet(Address address, Command command) {
    this.address = address;
    this.command = command;
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
  
  public void writeBytesTo(OutputStream out) throws IOException {
    int checksum = (6 + address.getHouseHigh() + address.getHouseLow() + address.getDevice() 
                      + command.getProtocolValue()) & 0xFF;
    out.write(address.getHouseHigh());
    out.write(address.getHouseLow());
    out.write(address.getDevice());
    out.write(command.getProtocolValue());
    out.write(checksum);
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
}
