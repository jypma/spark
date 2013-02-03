package nl.ypmania.fs20;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import nl.ypmania.ListWrapper;
import nl.ypmania.env.Environment;
import nl.ypmania.visonic.SensorDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/fs20")
@Component
public class FS20Resource {
  private @Autowired Environment environment;
  
  @GET
  @Path("dimmers")
  public ListWrapper<ActuatorDTO> getDimmers() {
    List<ActuatorDTO> result = new ArrayList<ActuatorDTO>();
    for (Dimmer d: environment.getAll(Dimmer.class)) result.add (new ActuatorDTO(d));
    return ListWrapper.wrap(result);
  }

  @GET
  @Path("switches")
  public ListWrapper<ActuatorDTO> getSwitches() {
    List<ActuatorDTO> result = new ArrayList<ActuatorDTO>();
    for (Switch s: environment.getAll(Switch.class)) result.add (new ActuatorDTO(s));
    return ListWrapper.wrap(result);
  }

  @GET
  @Path("motionsensors")
  public ListWrapper<SensorDTO> getMotionSensors() {
    List<SensorDTO> result = new ArrayList<SensorDTO>();
    for (FS20MotionSensor d: environment.getAll(FS20MotionSensor.class)) result.add (new SensorDTO(d));
    return ListWrapper.wrap(result);
  }
  
  @POST
  public void apply(ActuatorDTO actuator) {
    for (Dimmer d: environment.getAll(Dimmer.class)) {
      if (d.getZone().getName().equals(actuator.getZoneName()) && d.getName().equals(actuator.getName()))
        apply(actuator, d);
    }
    for (Switch s: environment.getAll(Switch.class)) {
      if (s.getZone().getName().equals(actuator.getZoneName()) && s.getName().equals(actuator.getName()))
        apply(actuator, s);      
    }
  }

  private void apply(ActuatorDTO actuator, Switch s) {
    if (actuator.getOn() != null) {
      if (actuator.getOn()) s.onFull(); else s.off();
    } else if (actuator.getBrightness() != null) {
      if (actuator.getBrightness() > 0) s.onFull(); else s.off();
    }
  }

  private void apply(ActuatorDTO actuator, Dimmer d) {
    if (actuator.getBrightness() != null) {
      d.dim(actuator.getBrightness());
    } else if (actuator.getOn() != null) {
      if (actuator.getOn()) d.onFull(); else d.off();
    }
  }
}
