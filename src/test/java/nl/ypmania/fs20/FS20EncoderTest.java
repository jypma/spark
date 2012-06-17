package nl.ypmania.fs20;

import static org.junit.Assert.*;

import org.junit.Test;

public class FS20EncoderTest {
  private FS20Packet packet = new FS20Packet(new FS20Address(12341234, 4321), Command.DIM_10);
  
  @Test
  public void decoder_can_decode_encoded_result() {
    assertEquals (packet, new FS20Decoder().decode(new FS20Encoder(packet.toBytes()).getResult(), 1));
  }

}
