package nl.ypmania.decoder;

public abstract class Decoder<T> {
  public abstract void handlePulse (int width);
  public abstract T getResult();
  protected abstract void reset();
  
  public T decode (int[] widths, int multiplier) {
    reset();
    for (int i: widths) handlePulse(i * multiplier);
    return getResult();
  }
}
