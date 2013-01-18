package nl.ypmania.cosm;


public class CosmTest {
  
  //@Test
  public void test() {
    CosmService s = new CosmService();
    s.init();
    s.updateDatapoint("Bryggers", 21.0);
  }
}
