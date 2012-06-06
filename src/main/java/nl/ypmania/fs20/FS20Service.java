package nl.ypmania.fs20;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
  private final Environment environment;
  private Cache<Packet, Packet> recentPackets = CacheBuilder.newBuilder()
      .expireAfterWrite(120, TimeUnit.MILLISECONDS)
      .build();
  
  @Autowired private NodeService nodeService;
  
  public FS20Service() {
    final int HOUSE = 12341234;
    final Address MASTER = new Address(HOUSE, 4444);
    final Address ALL_LIGHTS = new Address(HOUSE, 4411);
    final Address LIVING_ROOM = new Address(HOUSE, 1144);
    final Address BRYGGERS = new Address(HOUSE, 1244);
    final Address BEDROOM = new Address(HOUSE, 1344);
    environment = new Environment(
      new Dimmer("Living room ceiling", MASTER, ALL_LIGHTS, LIVING_ROOM, new Address(HOUSE, 1111)),  
      new Switch("Table lamp", MASTER, ALL_LIGHTS, LIVING_ROOM, new Address(HOUSE, 1112)),  
      new Switch("Reading lamp", MASTER, ALL_LIGHTS, LIVING_ROOM, new Address(HOUSE, 1113)),  
      new Switch("Corner lamp", MASTER, ALL_LIGHTS, LIVING_ROOM, new Address(HOUSE, 1114)),
      
      new Switch("Bryggers ceiling", MASTER, ALL_LIGHTS, BRYGGERS, new Address(HOUSE, 1211)),
      new Dimmer("Guest bathroom", MASTER, ALL_LIGHTS, BRYGGERS, new Address(HOUSE, 1212)),
      
      new Dimmer("Bedroom cupboards", MASTER, ALL_LIGHTS, BEDROOM, new Address(HOUSE, 1311)),
      new Switch("Bedroom LED strip", MASTER, ALL_LIGHTS, BEDROOM, new Address(HOUSE, 1312)),
      
      new Route(new Address(HOUSE, 3111), Command.ON_FULL) {
        protected void handle() {
          nodeService.sendFS20(new Packet (LIVING_ROOM, Command.ON_FULL));
        }
      }
    );
  }
  
  public List<Dimmer> getDimmers() {
    return environment.getAll(Dimmer.class);
  }
  
  public List<Switch> getSwitches() {
    return environment.getAll(Switch.class);
  }
    
  public void handle (Packet packet) {
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
