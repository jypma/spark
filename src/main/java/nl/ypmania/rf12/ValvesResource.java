package nl.ypmania.rf12;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import nl.ypmania.ListWrapper;
import nl.ypmania.env.Environment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/valves")
@Component
public class ValvesResource {
  private @Autowired Environment environment;
  
  @GET
  public ListWrapper<ValveDTO> list() {
    List<ValveDTO> result = new ArrayList<ValveDTO>();
    for (GardenValve s: environment.getAll(GardenValve.class)) result.add (new ValveDTO(s));
    return ListWrapper.wrap(result);    
  }
  
  @POST
  public void toggle(ValveDTO dto) {
    for (GardenValve v: environment.getAll(GardenValve.class)) {
      if (v.getZone().getName().equals(dto.getZoneName()) && v.getId().equals(dto.getName())) {
        v.apply(dto);
      }
    }
  }
}
