package nl.ypmania.rgb;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import nl.ypmania.env.Environment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.NotFoundException;

@Component
@Path("rgblamps")
public class LampResource {
  @Autowired Environment environment;
  
  @Path("{name}")
  @GET
  public LampColor getColor (@PathParam("name") String lampName) {
    for (RGBLamp lamp: environment.getAll(RGBLamp.class)) {
      if (lamp.getName().equals(lampName)) {
        return lamp.getColor();
      }
    }
    throw new NotFoundException();
  }
  
  @Path("{name}")
  @PUT
  public void setColor (@PathParam("name") String lampName, LampColor color) {
    for (RGBLamp lamp: environment.getAll(RGBLamp.class)) {
      if (lamp.getName().equals(lampName)) {
        lamp.setColor(color);
        return;
      }
    }
    throw new NotFoundException();
  }
  
}
