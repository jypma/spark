package nl.ypmania.alarm;

public class Packet {
  private int byte1, byte2, byte3, byte4;

  public Packet(int byte1, int byte2, int byte3, int byte4) {
    this.byte1 = byte1;
    this.byte2 = byte2;
    this.byte3 = byte3;
    this.byte4 = byte4;
  }

  @Override
  public String toString() {
    return bits(byte1) + "-" + bits(byte2) + "-" + bits(byte3) + "-" + bits(byte4) 
        + " (" + Integer.toHexString(byte1) + "-" + Integer.toHexString(byte2) + 
        "-" + Integer.toHexString(byte3) + "-" + Integer.toHexString(byte4) + ")";
  }

  private String bits(int b) {
    String s = Integer.toBinaryString(b);
    while (s.length() < 8) s = "0" + s;
    return s;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Packet)) return false;
    Packet b = (Packet) obj;
    return byte1 == b.byte1 && byte2 == b.byte2 && byte3 == b.byte3 && byte4 == b.byte4;
  }
  
}
