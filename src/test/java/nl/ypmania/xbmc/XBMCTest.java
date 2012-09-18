package nl.ypmania.xbmc;

import org.junit.Test;

public class XBMCTest {
  //@Test
  public void test() throws InterruptedException {
    XBMCService service = new XBMCService();
    service.init();
    service.getState();
    Thread.sleep(60000);
  }
}
