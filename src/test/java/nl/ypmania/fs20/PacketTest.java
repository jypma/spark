package nl.ypmania.fs20;

import java.util.Arrays;

import org.junit.Test;

public class PacketTest {
  @Test
  public void testPacket() {
    Packet p = new Packet(new Address(12341234, 1111), Command.TOGGLE);
    System.out.println(Arrays.toString(p.toBytes()));
  }
}
