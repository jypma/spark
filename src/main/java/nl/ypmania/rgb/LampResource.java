package nl.ypmania.rgb;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.NotFoundException;

import nl.ypmania.ListWrapper;
import nl.ypmania.env.Environment;
import nl.ypmania.fs20.ActuatorDTO;

@Component
@Path("rgblamps")
public class LampResource {
  @Autowired Environment environment;
  
  @GET
  public ListWrapper<ActuatorDTO> getList() {
    List<ActuatorDTO> result = new ArrayList<ActuatorDTO>();
    for (RGBLamp s: environment.getAll(RGBLamp.class)) result.add (new ActuatorDTO(s));
    return ListWrapper.wrap(result);
  }
  
  @POST 
  public void update(ActuatorDTO dto) {
    if (dto.getName() == null || dto.getBrightness() == null) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }
    for (RGBLamp lamp: environment.getAll(RGBLamp.class)) {
      if (lamp.getName().equals(dto.getName()) && lamp.getZone().getName().equals(dto.getZoneName())) {
        lamp.setBrightness(dto.getBrightness());
        return;
      }
    }
    throw new NotFoundException();    
  }
  
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
