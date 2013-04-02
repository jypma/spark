package nl.ypmania.ping;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class PingerTest {
  //@Test
  public void ping() throws IOException {
    Pinger p = new Pinger("192.168.0.11");
    FileWriter w = new FileWriter(new File("ping.log"), true);
    while (true) {
      PingResult ping = p.ping();
      w.append("[ " + new Date() + "]: ");
      System.out.print ("[ " + new Date() + "]: ");
      if (ping.isReachable()) {
        System.out.println("up, " + ping.getTime() + "ms");
        w.append("up, " + ping.getTime() + "ms\n");
      } else {
        System.out.println("DOWN");
        w.append("DOWN\n");
      }
      w.flush();
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        return;
      }
    }
  }

}
