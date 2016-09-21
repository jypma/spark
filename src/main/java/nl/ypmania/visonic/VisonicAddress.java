package nl.ypmania.visonic;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class VisonicAddress {
  private static final int BYTE1_MASK = 0b11111000; // last 3 bits of byte 1 are unreliable
  
  private int byte1, byte2, byte3;
  private int hashCode;

  public VisonicAddress(int byte1, int byte2, int byte3) {
    this.byte1 = byte1;
    this.byte2 = byte2;
    this.byte3 = byte3;
    this.hashCode = new HashCodeBuilder().append(this.byte1 & BYTE1_MASK).append(this.byte2).append(this.byte3).toHashCode();
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
    return (b.byte1 & BYTE1_MASK) == (byte1 & BYTE1_MASK) && b.byte2 == byte2 && b.byte3 == byte3;
  }
  
  @Override
  public String toString() {
    return "(" + byte1 + "-" + byte2 + "-" + byte3 + "/"
        + VisonicPacket.bits(byte1) + "-" + VisonicPacket.bits(byte2) + "-" + VisonicPacket.bits(byte3) + ")";
  }

}
