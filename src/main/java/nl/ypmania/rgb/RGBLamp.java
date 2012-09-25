package nl.ypmania.rgb;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import nl.ypmania.env.Environment;
import nl.ypmania.node.NodeService;
import nl.ypmania.rf12.RF12Packet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/rgblamp")
public class RGBLamp {
  private static final Logger log = LoggerFactory.getLogger(RGBLamp.class);
  
  private @Autowired Environment environment;
  private @Autowired NodeService nodeService;

  private LampColor color = new LampColor (180, 180, 180, 100);
  private LampColor nextColor = color;
  
  @GET
  public LampColor getColor() {
    return color;
  }
  
  @POST
  public synchronized void setColor (LampColor newColor) {
    if (nextColor.equals(newColor)) return;
    log.info ("Queueing change from {} to {}", color, newColor);
    boolean waiting = !nextColor.equals(color);
    nextColor = newColor;
    if (!waiting) {
      log.debug ("Scheduling a change");
      environment.onRf868Clear(new Runnable() {
        @Override
        public void run() {
          synchronized(this) {
            log.info ("Changing from {} to {}", color, nextColor);
            environment.setRf868UsageEnd(100);
            nodeService.sendRF12(new RF12Packet(new int[] { 1,1,82,71,nextColor.getR(),nextColor.getG(),nextColor.getB(),nextColor.getQ(),0,0,0,0 } ));
            color = nextColor;
          }
        }
      });      
    }
  }
}
