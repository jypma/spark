package nl.ypmania.fs20;

import java.util.Timer;
import java.util.TimerTask;
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
  private Timer timer = new Timer();
  private long lastPacket = 0;
  
  public void queueFS20(final FS20Packet packet) {
    if (System.currentTimeMillis() < lastPacket + 200) {
      log.info("Queueing sending of {}", packet);
      timer.schedule(new TimerTask(){
        @Override
        public void run() {
          log.info("Sending {}", packet);
          nodeService.sendFS20(packet);          
        }
      }, 200);
    } else {
      log.info("Sending {}", packet);
      nodeService.sendFS20(packet);
    }
  }

  public void handle (FS20Packet packet) {
    if (packet != null) {
      lastPacket  = System.currentTimeMillis();
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
