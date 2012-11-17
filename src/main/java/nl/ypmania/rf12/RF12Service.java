package nl.ypmania.rf12;

import java.util.concurrent.TimeUnit;

import nl.ypmania.env.Environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Component
public class RF12Service {
  private static final Logger log = LoggerFactory.getLogger(RF12Service.class);
  private @Autowired Environment environment;
  private Cache<RF12Packet, RF12Packet> recentPackets = CacheBuilder.newBuilder()
      .expireAfterWrite(1000, TimeUnit.MILLISECONDS)
      .build();
  
  public void handle (RF12Packet packet) {
    if (packet != null) {
      if (recentPackets.getIfPresent(packet) != null) {
        log.debug("Received duplicate.");
      } else {
        log.info("Received {}", packet);
        environment.receive(packet);        
        recentPackets.put(packet, packet);
      }
    }
  }
  
}
