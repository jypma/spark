package nl.ypmania.alarm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AlarmService {
  private static final Logger log = LoggerFactory.getLogger(AlarmService.class);

  public void handle(Packet packet) {
    if (packet == null) return;
    log.info ("Received " + packet);
  }
  
}
