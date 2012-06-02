package nl.ypmania.fs20;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import nl.ypmania.ListWrapper;

@Path("/fs20")
@Component
public class FS20Resource {
  private @Autowired FS20Service fs20Service;
  private static final Logger log = LoggerFactory.getLogger(LoggerFactory.class);
  
  @GET
  @Path("dimmers")
  public ListWrapper<Dimmer> getDimmers() {
    log.debug("dimmers: " + ListWrapper.wrap(fs20Service.getDimmers()).getItems().size());
    return ListWrapper.wrap(fs20Service.getDimmers());
  }

  @GET
  @Path("switches")
  public ListWrapper<Switch> getSwitches() {
    return ListWrapper.wrap(fs20Service.getSwitches());
  }

}
