package nl.ypmania.zoneminder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
/**
 * Connects to ZoneMinder's "zmtrigger" service. On Ubuntu, this is usually enabled
 * by enabling OPT_TRIGGERS in the admin interface, and editing /usr/bin/zmtrigger.pl
 * so that only the TCP service on port 6802 is started.
 */
public class ZoneMinderService {
  private static final Logger log = LoggerFactory.getLogger(ZoneMinderService.class);
  
  private String host = "localhost";
  private int port = 6802;
  
  /**
   * Triggers recording of an event in ZoneMinder.
   * @param monitor ID of the monitor (1 if you only have one)
   * @param duration Duration, in seconds, of the event to record (excluding "pre" frames)
   * @param score Alarm score of the event, 0..255
   * @param cause Cause for the event, 32 chars max
   * @param description Description, can be longer
   */
  public void triggerEvent(int monitor, int duration, int score, String cause, String description) {
    Socket socket = new Socket();
    try {
      socket.connect(new InetSocketAddress(host, port), 5000);
      try {
         socket.getOutputStream().write(("" + monitor + "|on+" + duration + "|" + score + "|" + 
                                        cause + "|" + description + "|\n").getBytes("UTF-8"));
         socket.getOutputStream().flush();
         log.debug("Triggered zoneminder for {}", cause);
      } finally {
        socket.close();
      }
    } catch (IOException e) {
      log.error("Error triggering zoneminder", e);
    }
  }
}
