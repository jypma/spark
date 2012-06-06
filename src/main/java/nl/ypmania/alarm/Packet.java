package nl.ypmania.alarm;

public class Packet {
  private int byte1, byte2, byte3, byte4, byte5;

  public Packet(int byte1, int byte2, int byte3, int byte4, int byte5) {
    this.byte1 = byte1;
    this.byte2 = byte2;
    this.byte3 = byte3;
    this.byte4 = byte4;
    this.byte5 = byte5;
  }

  @Override
  public String toString() {
    return bits(byte1) + "-" + bits(byte2) + "-" + bits(byte3) + "-" + bits(byte4) 
        + " (" + hex(byte1) + "-" + hex(byte2) + 
        "-" + hex(byte3) + "-" + hex(byte4) +
        "-" + hex(byte5) + ")";
  }

  private String hex(int b) {
    String s = Integer.toHexString(b);
    if (s.length() == 1) s = "0" + s;
    return s;
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
    return byte1 == b.byte1 && byte2 == b.byte2 && byte3 == b.byte3 && byte4 == b.byte4 && byte5 == b.byte5;
  }
  
}
