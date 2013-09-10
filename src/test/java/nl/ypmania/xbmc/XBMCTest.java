package nl.ypmania.xbmc;

//import org.junit.Test;

public class XBMCTest {
  //@Test
  public void test() throws InterruptedException {
    XBMCService service = new XBMCService();
    service.init();
    while (true) {
      System.out.println(service.getState());
      Thread.sleep(1000);      
    }
  }
}
