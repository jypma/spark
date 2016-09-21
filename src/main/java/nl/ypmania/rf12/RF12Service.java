package nl.ypmania.rf12;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import nl.ypmania.env.Environment;
import nl.ypmania.env.Zone;
import nl.ypmania.fs20.FS20Packet;
import nl.ypmania.fs20.FS20Service;

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
  
  @Autowired private FS20Service fs20Service;
  
  private final Map<FS20Packet, FS20Packet> translations = new HashMap<>();
  {
    //translations.put(FS20Packet.fromBytes(new int[] {  177, 85, 9, 196, 37, 16 }), new FS20Packet(new FS20Address(12343333, 3111), Command.TIMED_ON_FULL, 60, true));
    //translations.put(FS20Packet.fromBytes(new int[] {  177, 85, 9, 194, 73, 254 }), new FS20Packet(new FS20Address(12343333, 3112), Command.TIMED_ON_FULL, 60, true));
    //translations.put(FS20Packet.fromBytes(new int[] {  177, 85, 9, 196, 73 }), new FS20Packet(new FS20Address(12343333, 3112), Command.TIMED_ON_FULL, 60, true));
  }
  
  public void queue(final Zone zone, final RF12Packet packet) {
    environment.onRf868Clear(new Runnable() {
      @Override
      public void run() {
        environment.setRf868UsageEnd(100);
        boolean sent = false;
        for (String proxyId: environment.getProxyService().selectProxies(zone)) {
          recentPackets.put(packet, packet);
          if (environment.getProxyService().trySend(proxyId, packet)) {
            sent = true;
            break;
          }
        }
        if (!sent) {
          log.warn("No proxy available to send {}", packet);            
        }
      }
    });
  }

  public void handle (RF12Packet packet) {
    if (packet != null) {
      synchronized(this) {
        if (recentPackets.getIfPresent(packet) != null) {
          log.debug("Received duplicate.");
          return;
        } else {
          log.info("Received {}", packet);
          recentPackets.put(packet, packet);
        }        
      }
      environment.receive(packet);        
      int first = packet.getContents().get(0);
      if (((first >> 4) & 15) == packet.getContents().size() - 1) {
        int type = (first & 15);
        log.debug("Looks like OOK relay, type {}", type);
        if (type == 4) {
          int[] data = new int[packet.getContents().size() - 2];
          for (int i = 1; i < packet.getContents().size() - 1; i++) {
            data[i - 1] = packet.getContents().get(i);
          }
          FS20Packet fs20 = FS20Packet.fromBytes(data, true);
          // Still a bug in the OOK relay. Handle known cases here.
          FS20Packet translated = translations.get(fs20);
          if (translated != null) {
            fs20 = translated;
          }
          fs20Service.handle(fs20 );
        }
      }
    }
  }
  
}
