package nl.ypmania.fs20;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FS20Address {
  private int houseHigh;
  private int houseLow; 
  private int device;
  private int hashCode;
  
  protected FS20Address() {}
  
  public FS20Address(int houseHighByte, int houseLowByte, int deviceByte) {
    this.houseHigh = houseHighByte;
    this.houseLow = houseLowByte;
    this.device = deviceByte;
    calcHashCode();
  }
  
  public FS20Address(int house, int device) {
    this.houseHigh = AddressUtil.fromFS20 (house / 10000);
    this.houseLow = AddressUtil.fromFS20 (house % 10000);
    this.device = AddressUtil.fromFS20 (device);
    calcHashCode();
  }
  
  private void calcHashCode() {
    hashCode = new HashCodeBuilder().append(houseLow).append(houseHigh).append(device).toHashCode();  
  }

  public int getHouseHigh() {
    return houseHigh;
  }
  
  public int getHouseLow() {
    return houseLow;
  }
  
  public int getDevice() {
    return device;
  }

  @Override
  public String toString() {
    return "" + AddressUtil.toFS20(device) + "@" + AddressUtil.toFS20(houseHigh) + "" + AddressUtil.toFS20(houseLow);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof FS20Address)) return false;
    FS20Address b = (FS20Address) obj;
    return houseLow == b.houseLow && houseHigh == b.houseHigh && device == b.device;
  }
  
  @Override
  public int hashCode() {
    return hashCode;
  }
}
