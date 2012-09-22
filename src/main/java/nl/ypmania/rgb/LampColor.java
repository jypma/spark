package nl.ypmania.rgb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LampColor {
  private int r;
  private int g;
  private int b;
  private int q;
  
  protected LampColor() {}
  
  public LampColor (int r, int g, int b, int q) {
    this.r = r;
    this.g = g;
    this.b = b;
    this.q = q;
  }
  
  public int getR() {
    return r;
  }
  
  public int getG() {
    return g;
  }
  
  public int getB() {
    return b;
  }
  
  public int getQ() {
    return q;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof LampColor)) return false;
    LampColor c2 = (LampColor) obj;
    return r == c2.r && g == c2.g && b == c2.b && q == c2.q;
  }
  
  @Override
  public String toString() {
    return "RGB(" + r + "," + g + "," + b + "x" + q + ")";
  }
}
