package nl.ypmania.fs20;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FS20Encoder {
  private static final Logger log = LoggerFactory.getLogger(FS20Encoder.class);
  
  private int[] result;
  private int pos = 0;
  
  /**
   * Returns OOK pulse widths in ms, to transmit the given data as FS20. 
   */
  public FS20Encoder (int[] data) {
    log.debug ("Encoding {}", data);
    
    result = new int[(13 + (data.length * 9) + 1) * 2];
    for (int i = 0; i < 12; i++) {
      encodeBit(0);
    }
    encodeBit(1);
    for (int b: data) {
      int parity = 0;
      for (int bit = 7; bit >= 0; bit--) {
        int bitValue = (b & (1 << bit)) >>> bit;
        encodeBit(bitValue);
        parity += bitValue;
      }
      encodeBit ((~parity) & 1);
      
    }
    encodeBit(0);
  }
  
  public int[] getResult() {
    return result;
  }

  private void encodeBit(int i) {
    if (i == 0) {
      result[pos++] = 400; // 400ms
      result[pos++] = 400;
    } else if (i == 1) {
      result[pos++] = 600; // 600ms 
      result[pos++] = 600;
      
    } else throw new IllegalStateException("Unexpected bit: " + i);
  }
  
}
