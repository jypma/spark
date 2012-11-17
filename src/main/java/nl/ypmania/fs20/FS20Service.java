package nl.ypmania.fs20;

import java.util.concurrent.TimeUnit;

import nl.ypmania.env.Environment;
import nl.ypmania.node.NodeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Component
public class FS20Service {
  private static final Logger log = LoggerFactory.getLogger(FS20Service.class);
  private @Autowired Environment environment;
  private Cache<FS20Packet, FS20Packet> recentPackets = CacheBuilder.newBuilder()
      .expireAfterWrite(120, TimeUnit.MILLISECONDS)
      .build();
  
  @Autowired private NodeService nodeService;
  
  public void queueFS20(final FS20Packet packet) {
    environment.onRf868Clear(new Runnable() {
      @Override
      public void run() {
        environment.setRf868UsageEnd(200);
        log.info("Sending {}", packet);
        nodeService.sendFS20(packet);
        environment.receive(packet);
      }
    });
  }

  public void handle (FS20Packet packet) {
    if (packet != null) {
      if (recentPackets.getIfPresent(packet) != null) {
        log.debug("Received duplicate.");
      } else {
        // We've just received a packet, so there'll be at most 2 repeats of 75msec each
        environment.setRf868UsageEnd(150);
        log.info("Received {}", packet);
        environment.receive(packet);        
        recentPackets.put(packet, packet);
      }
    }
  }
}
