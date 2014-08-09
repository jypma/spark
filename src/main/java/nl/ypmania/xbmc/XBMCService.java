package nl.ypmania.xbmc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import nl.ypmania.env.Zone;
import nl.ypmania.env.ZoneEvent;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

@Component
public class XBMCService {
  private static final Logger log = LoggerFactory.getLogger(XBMCService.class);
  
  private Client client;
  private Timer timer;
  private State state = State.STOPPED;
  private int lastSeconds = -1;
  private Map<State,List<Runnable>> callbacks = new HashMap<State,List<Runnable>>();
  private Zone location;
  
  public void setLocation(Zone location) {
    this.location = location;
  }
  
  @PostConstruct
  public void init() {
    ClientConfig clientConfig = new DefaultClientConfig();
    clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
    client = Client.create(clientConfig);
    client.setConnectTimeout(5000);
    client.setReadTimeout(5000);
    timer = new Timer("xbmc");
    timer.schedule(new TimerTask(){
      @Override
      public void run() {
        State newState = retrieveState();
        if (newState == State.STOPPED) {
          lastSeconds = -1;
        }
        if (newState != state) {
          log.info ("State is now " + newState);
          try {
            callback(newState);
          } catch (Exception x) {
            log.error("Error in callback", x);
          }
        }
        state = newState;
        if (state == State.PLAYING && location != null) {
          location.event(ZoneEvent.moviePlaying());
        }
      }
    }, 5000, 5000);
  }
  
  protected synchronized void callback(State state) {
    List<Runnable> list = callbacks.get(state);
    if (list != null) {
      for (Runnable r: list) {
        r.run();
      }
    }
  }

  @PreDestroy
  public void stop() {
    if (timer != null) timer.cancel();
  }
  
  public State getState() {
    return state;
  }
  
  public synchronized void on (State s, Runnable r) {
    List<Runnable> list = callbacks.get(s);
    if (list == null) {
      list = new ArrayList<Runnable>();
      callbacks.put(s, list);
    }
    list.add (r);
  }
  
  protected State retrieveState() {
    try {
      WebResource resource = client.resource("http://192.168.0.183:8080/jsonrpc");
      JSONObject result = resource.entity("{\"jsonrpc\": \"2.0\", \"method\": \"Player.GetProperties\", \"id\":1, \"params\":{\"playerid\": 1, \"properties\": [\"time\"]}}", "application/json").post(JSONObject.class);
      if (result.has("result")) {
        result = result.getJSONObject("result");
        if (result.has("time")) {
          int seconds = result.getJSONObject("time").getInt("seconds");
          if (lastSeconds == -1 || lastSeconds == seconds) {
            lastSeconds = seconds;
            return State.PAUSED;
          } else {
            lastSeconds = seconds;
            return State.PLAYING;
          }
        } else {
          return State.STOPPED;
        }
      } else {
        return State.STOPPED;
      }
    } catch (ClientHandlerException x) {
      // server is sleeping
      return State.STOPPED;
    } catch (Exception e) {
      log.error ("Unexpected XBMC response> ", e);
      return State.STOPPED;
    }
  }
  
  public static enum State {
    STOPPED, PAUSED, PLAYING
  }
}
