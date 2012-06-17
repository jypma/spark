package nl.ypmania.visonic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.ypmania.decoder.Decoder;

public class VisonicDecoder extends Decoder<VisonicPacket> {
  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(LoggerFactory.class);
  
  private State state = new State();
  private int[] data = new int[25];
  private int length = 0;
  
  protected void reset() {
    state = new State();
  }
  
  @Override
  public void handlePulse(int width) {
    // 400 and 800ms
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
    
    int byte5 = (data[4] & 0xFF) | ((data[3] << state.bit) & 0xFF); 
    int byte4 = ((data[3] >>> (8 - state.bit)) & 0xFF) | ((data[2] << state.bit) & 0xFF); 
    int byte3 = ((data[2] >>> (8 - state.bit)) & 0xFF) | ((data[1] << state.bit) & 0xFF); 
    int byte2 = ((data[1] >>> (8 - state.bit)) & 0xFF) | ((data[0] << state.bit) & 0xFF);
    int byte1 =  (data[0] >>> (8 - state.bit)) & 0xFF;
    return new VisonicPacket (byte1, byte2, byte3, byte4, byte5);
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