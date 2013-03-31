package nl.ypmania.cosm;

import nl.ypmania.cosm.CosmService.DataPoint;

import org.junit.Test;
public class CosmTest {
  
//@Test
  public void test() {
    CosmService s = new CosmService();
    s.init();
    DataPoint pt = s.getDatapoint("El_Energy");
    System.out.println("va: " + pt.asLong());
    System.out.println("at: " + pt.getAt());
    System.out.println("at: " + pt.getTime());
  }
}
