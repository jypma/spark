package nl.ypmania.visonic;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class VisonicPacket {
  private int byte4, byte5;
  private VisonicAddress address;
  private int hashCode;

  public VisonicPacket(int byte1, int byte2, int byte3, int byte4, int byte5) {
    this.address = new VisonicAddress(byte1, byte2, byte3);
    this.byte4 = byte4;
    this.byte5 = byte5 & 0xF0; // ignore the lo nibble, its just parity
    this.hashCode = new HashCodeBuilder().append(address).append(this.byte4).append(this.byte5).toHashCode();
  }

  @Override
  public String toString() {
    
    return "" + address + ":" + bits(byte4) + "-" + bits(byte5)
        + "/" + hex(byte4) + "-" + hex(byte5);
  }

  public static String hex(int b) {
    String s = Integer.toHexString(b);
    if (s.length() == 1) s = "0" + s;
    return s;
  }
  
  public static String bits(int b) {
    String s = Integer.toBinaryString(b);
    while (s.length() < 8) s = "0" + s;
    return s;
  }
  
  public VisonicAddress getAddress() {
    return address;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof VisonicPacket)) return false;
    VisonicPacket b = (VisonicPacket) obj;
    return address.equals(b.address) && byte4 == b.byte4 && byte5 == b.byte5;
  }
  
  @Override
  public int hashCode() {
    return hashCode;
  }
  
  public int getByte4() {
    return byte4;
  }
  
  public int getByte5() {
    return byte5;
  }
}
