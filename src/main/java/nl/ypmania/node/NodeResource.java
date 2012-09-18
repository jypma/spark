package nl.ypmania.node;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import nl.ypmania.env.Environment;
import nl.ypmania.rf12.RF12Packet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/node")
public class NodeResource {
  private @Autowired Environment environment;
  private @Autowired NodeService nodeService;
  
  @POST
  @Path("rf12")
  public void sendRF12 (final RF12Packet packet) {
    environment.onRf868Clear(new Runnable() {
      @Override
      public void run() {
        environment.setRf868UsageEnd(100);
        nodeService.sendRF12(packet);
      }
    });
    
  }

}
