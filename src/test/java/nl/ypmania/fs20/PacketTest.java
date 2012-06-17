package nl.ypmania.fs20;

import java.util.Arrays;

import org.junit.Test;

public class PacketTest {
  @Test
  public void testPacket() {
    FS20Packet p = new FS20Packet(new FS20Address(12341234, 1111), Command.TOGGLE);
    System.out.println(Arrays.toString(p.toBytes()));
  }
}
