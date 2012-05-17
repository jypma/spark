package nl.ypmania.fs20;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Address {
  private int houseLow; 
  private int houseHigh;
  private int device;
  
  public Address(int house, int device) {
    this.houseHigh = AddressUtil.fromFS20 (house / 10000);
    this.houseLow = AddressUtil.fromFS20 (house % 10000);
    this.device = AddressUtil.fromFS20 (device);
  }

  @Override
  public String toString() {
    return "" + device + "@" + houseHigh + "" + houseLow;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Address)) return false;
    Address b = (Address) obj;
    return houseLow == b.houseLow && houseHigh == b.houseHigh && device == b.device;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(houseLow).append(houseHigh).append(device).toHashCode();
  }
}
