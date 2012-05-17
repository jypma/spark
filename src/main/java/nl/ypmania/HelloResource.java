package nl.ypmania;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

   @Path("/hello")
   @Component

   public class HelloResource {
       @GET
       @Produces(MediaType.APPLICATION_JSON)
       public HelloBean getIt() {
           return new HelloBean();
       }
   }