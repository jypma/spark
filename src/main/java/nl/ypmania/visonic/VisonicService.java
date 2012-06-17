package nl.ypmania.visonic;

import java.util.concurrent.TimeUnit;

import nl.ypmania.env.Environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Component
public class VisonicService {
  private static final Logger log = LoggerFactory.getLogger(VisonicService.class);

  private Cache<VisonicPacket, VisonicPacket> recentPackets = CacheBuilder.newBuilder()
      .expireAfterWrite(2500, TimeUnit.MILLISECONDS)
      .build();
  
  private @Autowired Environment environment;
  
  public void handle(VisonicPacket packet) {
    if (packet != null) {
      if (recentPackets.getIfPresent(packet) != null) {
        log.debug("Received duplicate.");
      } else {
        log.info("Received {}", packet);
        environment.receive(packet);        
      }
      recentPackets.put(packet, packet);
    }
  }
  
}
