package nl.ypmania.cosm;

import javax.annotation.PostConstruct;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

@Component
public class CosmService {
  
  private static final Logger log = LoggerFactory.getLogger(CosmService.class);
  private String apiKey = System.getProperty("cosm.apikey");
  private int feed = 94446;
  private Client client;
  
  private static final DateTimeFormatter fmt = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC(); 
  
  @PostConstruct
  public void init() {
    log.debug("Initing");
    ClientConfig clientConfig = new DefaultClientConfig();
    clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
    client = Client.create(clientConfig);
    client.setConnectTimeout(1000);
    client.setReadTimeout(1000);
    log.debug("Inited");
  }
  
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class DataPoint {
    private String current_value;
    private String at;
    
    public String getCurrent_value() {
      return current_value;
    }
    
    public String getAt() {
      return at;
    }
    
    public Long asLong() {
      try {
        return (current_value == null) ? null : Long.parseLong(current_value);
      } catch (IllegalArgumentException x) {
        return null;
      } 
    }
    
    public DateTime getTime() {
      try {
        return (at == null) ? null : ISODateTimeFormat.dateTimeParser().parseDateTime(at);
      } catch (IllegalArgumentException x) {
        return null;
      }
    }
  }
  
  public DataPoint getDatapoint (String datastream) {
    WebResource resource = client.resource("https://api.cosm.com/v2/feeds/" + feed + "/datastreams/" + datastream);
    try {
      return resource.header("X-ApiKey", apiKey).get(DataPoint.class);
    } catch (Exception x) {
      log.error ("Error getting cosm datastream " + datastream, x);
      return null;
    }    
  }
  
  public void updateDatapoint (String datastream, double value) {
    updateDatapoint(datastream, String.valueOf(value));
  }
  
  public void updateDatapoint (String datastream, long value) {
    updateDatapoint(datastream, String.valueOf(value));    
  }
  
  public void updateDatapoint (String datastream, String value) {
    log.debug("Updating {} to {}", datastream, value);
    /*
    WebResource resource = client.resource("https://api.cosm.com/v2/feeds/" + feed + "/datastreams/" + datastream + "/datapoints");
    String response = "";
    try {
      String time = fmt.print(DateTime.now());
      String body = "{\"datapoints\":[{\"at\": \"" + time + "\", \"value\": \"" + value + "\"}]}";
      log.debug(body);
      response = resource.header("X-ApiKey", apiKey).entity(body).post(String.class);
    } catch (Exception x) {
      log.error ("Error updating cosm: " + response, x);
    }
    */
  }

}
