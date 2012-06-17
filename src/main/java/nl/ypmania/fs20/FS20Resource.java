package nl.ypmania.fs20;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import nl.ypmania.ListWrapper;
import nl.ypmania.env.Environment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/fs20")
@Component
public class FS20Resource {
  private @Autowired Environment environment;
  
  @GET
  @Path("dimmers")
  public ListWrapper<Dimmer> getDimmers() {
    return ListWrapper.wrap(environment.getAll(Dimmer.class));
  }

  @GET
  @Path("switches")
  public ListWrapper<Switch> getSwitches() {
    return ListWrapper.wrap(environment.getAll(Switch.class));
  }

}
