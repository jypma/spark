package nl.ypmania.visonic;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import nl.ypmania.ListWrapper;
import nl.ypmania.env.Environment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/visonic")
@Component
public class VisonicResource {
  private @Autowired Environment environment;
  
  @GET
  @Path("motionsensors")
  public ListWrapper<SensorDTO> getMotionSensors() {
    List<SensorDTO> result = new ArrayList<SensorDTO>();
    for (MotionSensor d: environment.getAll(MotionSensor.class)) result.add (new SensorDTO(d));
    return ListWrapper.wrap(result);
  }


}
