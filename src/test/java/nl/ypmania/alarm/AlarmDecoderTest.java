package nl.ypmania.alarm;

import static org.junit.Assert.*;
import nl.ypmania.alarm.AlarmDecoder;

import org.junit.Before;
import org.junit.Test;

public class AlarmDecoderTest {
  private AlarmDecoder decoder;
  
  @Before
  public void setup() {
    decoder = new AlarmDecoder();
  }
  
  @Test
  public void motion_sensor_sequence_is_decoded() {
    // size 73
    for (int w: new int[] { 127,88,209,105,199,105,206,199,106,98,205,104,202,203,107,198,111,95,206,104,201,106,200,107,199,108,198,109,197,206,106,99,205,202,108,97,206,202,108,97,206,104,201,106,200,108,198,205,106,99,205,104,201,106,200,108,198,205,106,198,110,96,206,103,202,105,200,205,106,198,111,195,112 }) {
      decoder.handlePulse(w * 4);
    }
    assertEquals(new Packet(0x0E, 0xCF, 0xD5, 0xEF, 0x38), decoder.getResult());
  }

  @Test
  public void motion_sensor_sequence_2_is_decoded() {
    // size 74
    for (int w: new int[] { 125,117,88,209,105,199,106,205,200,106,98,206,103,202,204,106,198,111,95,207,102,203,105,200,108,198,109,197,109,197,206,105,100,204,202,108,97,205,202,109,97,206,103,202,106,199,108,198,206,106,98,204,105,201,106,199,108,198,206,106,197,111,95,207,102,203,105,200,205,106,198,110,196,112 }) {
      decoder.handlePulse(w * 4);
    }
    assertEquals(new Packet(0x06, 0xCF, 0xD5, 0xEF, 0x38), decoder.getResult());
  }
  
  @Test
  public void motion_sensor_sequence_3_is_decoded() {
    // size 74
    for (int w: new int[] { 92,120,89,208,106,199,104,206,199,106,99,205,104,201,204,107,198,111,95,206,103,202,106,200,107,199,108,198,109,197,206,106,99,205,202,108,97,206,202,108,97,206,103,202,106,200,107,199,205,106,99,205,104,201,106,200,107,199,206,106,198,110,95,207,102,203,106,200,204,107,198,111,195,112 }) {
      decoder.handlePulse(w * 4);
    }
    assertEquals(new Packet(0x06, 0xCF, 0xD5, 0xEF, 0x38), decoder.getResult());
  }
  
  @Test
  public void motion_sensor_B_sequence_1_is_decoded() {
    // size 74
    for (int w: new int[] { 31,202,128,204,126,97,230,213,123,99,229,106,225,217,120,209,124,208,126,97,229,214,122,208,125,98,229,213,122,208,126,206,125,207,126,206,126,97,230,105,225,110,222,217,120,210,124,207,126,97,230,213,123,99,229,107,224,110,221,218,120,102,227,107,224,216,121,101,228,107,224,217,119 }) {
      decoder.handlePulse(w * 4);
    }
    assertEquals(new Packet(0x02, 0xC4, 0x83, 0x8b, 0xb6), decoder.getResult());
  }

}
