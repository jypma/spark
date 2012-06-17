package nl.ypmania.visonic;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class VisonicAddress {
  private int byte1, byte2, byte3;
  private int hashCode;

  public VisonicAddress(int byte1, int byte2, int byte3) {
    this.byte1 = (byte1 & 0x03); // only the last two bits of byte1 we receive correctly anyways
    this.byte2 = byte2;
    this.byte3 = byte3;
    this.hashCode = new HashCodeBuilder().append(this.byte1).append(this.byte2).append(this.byte3).toHashCode();
  }
  
  public int getByte1() {
    return byte1;
  }
  
  public int getByte2() {
    return byte2;
  }
  
  public int getByte3() {
    return byte3;
  }
  
  @Override
  public int hashCode() {
    return hashCode;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof VisonicAddress)) return false;
    VisonicAddress b = (VisonicAddress) obj;
    return b.byte1 == byte1 && b.byte2 == byte2 && b.byte3 == byte3;
  }
  
  @Override
  public String toString() {
    return "(" + VisonicPacket.hex(byte1) + "-" + VisonicPacket.hex(byte2) + "-" + VisonicPacket.hex(byte3) + "/"
        + VisonicPacket.bits(byte1) + "-" + VisonicPacket.bits(byte2) + "-" + VisonicPacket.bits(byte3) + ")";
  }

}
