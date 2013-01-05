package nl.ypmania.cosm;

import org.junit.Test;

public class CosmTest {
  
  //@Test
  public void test() {
    CosmService s = new CosmService();
    s.init();
    s.bryggers(22.0);
  }
}
