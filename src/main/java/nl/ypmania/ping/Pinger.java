package nl.ypmania.ping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pinger {
  private static final Logger log = LoggerFactory.getLogger(Pinger.class);
  
  private String host;

  public Pinger(String host) {
    this.host = host;
  }
  
  public PingResult ping() throws IOException {
    final Process process = Runtime.getRuntime().exec(new String[] {
        "ping", "-c", "1", host 
    });
    final List<String> lines = new ArrayList<String>();
    final Thread reader = new Thread() {
      public void run() {
        try {
          lines.addAll(IOUtils.readLines(process.getInputStream()));
        } catch (IOException x) {
          log.error("Error reading ping output", x);
        }        
      }
    };
    reader.start();
    final Thread main = Thread.currentThread();
    final Thread timeout = new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          return; // ping exited within timeout
        }
        main.interrupt();
      }
    };
    timeout.start();
    try {
      process.waitFor();
    } catch (InterruptedException e) {
      // we received a timeout from the timeout thread
      return PingResult.unreachable();
    }
    timeout.interrupt();
    try {
      reader.join();
    } catch (InterruptedException e) {}
    // round-trip min/avg/max/stddev = 0.725/0.725/0.725/0.000 ms
    Pattern avg = Pattern.compile(".*=\\ [\\.0-9]+/([\\.0-9]+)/[\\.0-9]+/.*");
    if (lines.size() > 0) {
      String line = lines.get(lines.size() - 1);
      Matcher matcher = avg.matcher(line);
      if (matcher.matches()) {
        String average = matcher.group(1);
        try {
          double time = Double.parseDouble(average);
          return PingResult.reachable(time);
        } catch (NumberFormatException x) {
          throw new RuntimeException ("Could not parse average in ping output: " + average);
        }
      } else throw new RuntimeException ("Could not parse last line in ping output: " + line);
    } else throw new RuntimeException ("No output in ping command");
  }
  
}
