package nl.ypmania.rf12;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.ArrayUtils;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RF12Packet {
  private List<Integer> contents;
  
  protected RF12Packet() {}
  
  public RF12Packet(int[] contents) {
    this.contents = new ArrayList<Integer>(contents.length);
    for (int i: contents) this.contents.add(i);
  }

  public List<Integer> getContents() {
    return contents;
  }
  
  @Override
  public String toString() {
    return "RF12:" + Arrays.toString(contents.toArray());
  }
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof RF12Packet)) return false;
    RF12Packet b = (RF12Packet) obj;
    return ArrayUtils.isEquals(contents.toArray(), b.contents.toArray());
  }
  
  @Override
  public int hashCode() {
    return contents.hashCode();
  }
}
