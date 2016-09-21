package nl.ypmania.visonic;

import nl.ypmania.decoder.Decoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VisonicDecoder extends Decoder<VisonicPacket> {
  private static final Logger log = LoggerFactory.getLogger(VisonicDecoder.class);
  
  private State state = new State();
  private int[] data = new int[25];
  private int length = 0;
  private int[] pulses = new int[1024];
  private int pulseCount = 0;
  
  protected void reset() {
    state = new State();
    pulseCount = 0;
  }
  
  @Override
  public void handlePulse(int width) {
    pulses[pulseCount] = width;
    pulseCount++;
    
    // 400 and 800us
    if (width > 200 & width <= 600) {
      state = state.handleShort();
    } else if (width > 600 & width < 1000) {
      state = state.handleLong();
    } else {
      reset();
    }
  }

  @Override
  public VisonicPacket getResult() {
    if (length != 4) return null;
    
    /*
    if (log.isDebugEnabled()) {
      int left = (4 - state.bit);
      int right = (8 - (4 - state.bit));
      
      int data4 = ((data[4] << left) & 0xFF) | ((data[3] >>> right) & 0xFF);
      int data3 = ((data[3] << left) & 0xFF) | ((data[2] >>> right) & 0xFF);
      int data2 = ((data[2] << left) & 0xFF) | ((data[1] >>> right) & 0xFF);
      int data1 = ((data[1] << left) & 0xFF) | ((data[0] >>> right) & 0xFF);
      int data0 = (data[0] << left) & 0xFF;
      
      VisonicPacket pkt = new VisonicPacket (data0, data1, data2, data3, data4);
      log.debug("Alternative decode: {}", pkt);      
    }
    */
    
    int byte5 = (data[4] & 0xFF) | ((data[3] << state.bit) & 0xFF); 
    int byte4 = ((data[3] >>> (8 - state.bit)) & 0xFF) | ((data[2] << state.bit) & 0xFF); 
    int byte3 = ((data[2] >>> (8 - state.bit)) & 0xFF) | ((data[1] << state.bit) & 0xFF); 
    int byte2 = ((data[1] >>> (8 - state.bit)) & 0xFF) | ((data[0] << state.bit) & 0xFF);
    int byte1 =  (data[0] >>> (8 - state.bit)) & 0xFF;
    VisonicPacket pkt = new VisonicPacket (
        byte1 & 0x03, // only the last two bits of byte1 we receive correctly anyways, 
        byte2, byte3, byte4,
        byte5  & 0xF0); // ignore the lo nibble, its just parity
    
    if (log.isDebugEnabled()) {
      StringBuilder s = new StringBuilder();
      for (int i = 0; i < pulseCount; i++) {
        s.append(pulses[i]);
        if (i != pulseCount - 1) {
          s.append(", ");
        }
      }
      log.debug("Turned {} into {}", s, pkt);
    }
    
    return pkt;
  }

  private class State {
    int bit = 0;
    Boolean previousLong = null;
    boolean flipped = false;
    
    private void receive (int value) {
      if (hasParityBit() && bit == 8) {
        //System.out.println("(" + value + ") ");
        //TODO check parity
      } else {
        //System.out.print(value);
        data[length] = (data[length] << 1) | value;        
      }
      if (++bit >= getBitsPerByte()) {
        bit = 0;
        if (++length > data.length) {
          throw new RuntimeException ("too long");
        }
      }
    }
    
    private void flipBits() {
      for (int i = 0; i < length; i++) {
        data[i] = ~data[i];
      }
      for (int b = 0; b < bit; b++) {
        data[length] ^= (1 << b);
      }
    }
    
    private int getBitsPerByte() {
      return hasParityBit() ? 9 : 8;
    }
    
    protected boolean hasParityBit() {
      return false;
    }
    
    public State handleLong() {
      if (previousLong == null) {
        previousLong = true;
        return this;
      }
      if (!previousLong) {
        receive(1);
        previousLong = null;
        return this;
      } else {
        if (!flipped) {
          flipBits();
          previousLong = true;
          flipped = true;
          return this;
        } else {
          reset();
          return state;
        }
      }
    }

    public State handleShort() {
      if (previousLong == null) {
        previousLong = false;
        return this;
      }
      if (previousLong) {
        receive(0);
        previousLong = null;
        return this;
      } else {
        if (!flipped) {
          flipBits();
          previousLong = false;
          flipped = true;
          return this;
        } else {
          reset();
          return state;
        }
      }
    }
  }  
}
// S S L L