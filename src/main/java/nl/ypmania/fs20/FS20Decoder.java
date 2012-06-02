package nl.ypmania.fs20;

import nl.ypmania.decoder.Decoder;


public class FS20Decoder extends Decoder<Packet> {
  private State state = new SynchronizationState();
  private int[] data = new int[25];
  private int length = 0;
  
  protected void reset() {
    state.wrapup();
    state = new SynchronizationState();
    length = 0;
  }
  
  @Override
  public final void handlePulse (int width) {
    if (width >= 300 & width < 500) {
      state = state.handleZero();
    } else if (width >= 500 & width < 775) {
      state = state.handleOne();
    } else {
      reset();
    }
  }
  
  public int[] getData() {
    int[] result = new int[length];
    System.arraycopy(data, 0, result, 0, length);
    return result;
  }
  
  @Override
  public Packet getResult() {
    return Packet.fromBytes(getData());
  }
  
  private abstract class State {
    public State handleZero() { return this; }    
    public State handleOne() { return this; }
    public void wrapup() {}
  }
  
  private class SynchronizationState extends State {
    int zeroCount = 0;
    int oneCount = 0;
    
    @Override
    public State handleZero() {
      //System.out.println("Sync: 0");
      zeroCount++;
      return this;
    }
    
    @Override
    public State handleOne() {
      //System.out.println("Sync: 1");
      if (zeroCount > 20) {
        oneCount++;
        if (oneCount == 2) {
          return new ByteState();          
        } else {
          return this;
        }
      } else {
        reset();
        return state;
      }
    }
  }
  
  private class ByteState extends State {
    int bit = 0;
    Boolean previousPulse;
    
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
    
    private int getBitsPerByte() {
      return hasParityBit() ? 9 : 8;
    }
    
    protected boolean hasParityBit() {
      return true;
    }
    
    @Override
    public void wrapup() {
      while (bit > 0) receive(0);
    }
    
    @Override
    public State handleOne() {
      if (previousPulse == null) {
        previousPulse = true;
        return this;
      } else if (previousPulse == true) {
        receive(1);
        previousPulse = null;
        return this;
      } else {
        reset();
        return state;
      }
    }
    
    @Override
    public State handleZero() {
      if (previousPulse == null) {
        previousPulse = false;
        return this;
      } else if (previousPulse == false) {
        receive(0);
        previousPulse = null;
        return this;
      } else {
        reset();
        return state;
      }
    }
  }
}
