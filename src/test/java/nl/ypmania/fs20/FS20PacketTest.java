package nl.ypmania.fs20;

import java.util.Arrays;

import org.junit.Test;

public class FS20PacketTest {
  @Test public void can_decode_ook_relay() {
    FS20Packet p = FS20Packet.fromBytes(new int[] {  177, 85, 9, 194, 73, 254 }, true);
    
    print(p);
    
    FS20Packet q = new FS20Packet(new FS20Address(12343333, 3112), Command.TIMED_ON_FULL, 0, true);
    print(q);
  }

  private void print(FS20Packet p) {
    for (int i: p.toBytes()) {
      System.out.print(Integer.toString(i, 2));
      System.out.print(" ");
    }
    System.out.println();
  }
}
