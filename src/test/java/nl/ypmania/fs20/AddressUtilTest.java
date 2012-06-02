package nl.ypmania.fs20;

import static org.junit.Assert.*;

import org.junit.Test;

public class AddressUtilTest {
  @Test
  public void fs20_addresses_are_correct_binary() {
    assertEquals (0, AddressUtil.fromFS20(1111));
    assertEquals (1, AddressUtil.fromFS20(1112));
    assertEquals (2, AddressUtil.fromFS20(1113));
    assertEquals (3, AddressUtil.fromFS20(1114));
    assertEquals (4, AddressUtil.fromFS20(1121));
    assertEquals (16, AddressUtil.fromFS20(1211));
    assertEquals (64, AddressUtil.fromFS20(2111));
    assertEquals (255, AddressUtil.fromFS20(4444));
  }
  
  @Test
  public void fs20_addresses_are_decoded() {
    assertEquals (1111, AddressUtil.toFS20(0));
    assertEquals (1112, AddressUtil.toFS20(1));
    assertEquals (1113, AddressUtil.toFS20(2));
    assertEquals (1114, AddressUtil.toFS20(3));
    assertEquals (1121, AddressUtil.toFS20(4));
    assertEquals (1211, AddressUtil.toFS20(16));
    assertEquals (2111, AddressUtil.toFS20(64));
    assertEquals (4444, AddressUtil.toFS20(255));    
  }
}
