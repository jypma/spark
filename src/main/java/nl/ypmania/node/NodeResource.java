package nl.ypmania.node;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import nl.ypmania.rf12.RF12Packet;
import nl.ypmania.rf12.RF12Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/node")
public class NodeResource {
  private @Autowired RF12Service rf12Service;
  
  @POST
  @Path("rf12")
  public void sendRF12 (final RF12Packet packet) {
	  rf12Service.queue(packet);
  }

}
