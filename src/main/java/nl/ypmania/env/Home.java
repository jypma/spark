package nl.ypmania.env;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import nl.ypmania.fs20.Command;
import nl.ypmania.fs20.Dimmer;
import nl.ypmania.fs20.FS20Address;
import nl.ypmania.fs20.FS20Packet;
import nl.ypmania.fs20.FS20Route;
import nl.ypmania.fs20.FS20Service;
import nl.ypmania.fs20.Switch;
import nl.ypmania.visonic.DoorSensor;
import nl.ypmania.visonic.MotionSensor;
import nl.ypmania.visonic.VisonicAddress;
import nl.ypmania.visonic.VisonicPacket;
import nl.ypmania.visonic.VisonicRoute;
import nl.ypmania.xbmc.XBMCService;
import nl.ypmania.xbmc.XBMCService.State;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/home")
@Component
public class Home extends Environment {
  private static final Logger log = LoggerFactory.getLogger(Home.class);
  
  private static final int HOUSE = 12341234;
  private static final int BUTTONS = 12344444;
  private static final int SENSORS = 12343333;
  
  private static final FS20Address MASTER = new FS20Address(HOUSE, 4444);
  private static final FS20Address ALL_LIGHTS = new FS20Address(HOUSE, 4411);
  private static final FS20Address LIVING_ROOM = new FS20Address(HOUSE, 1144);
  private static final FS20Address BRYGGERS = new FS20Address(HOUSE, 1244);
  private static final FS20Address BEDROOM = new FS20Address(HOUSE, 1344);
  private static final FS20Address DININGROOM = new FS20Address(HOUSE, 1444);
  
  private static final VisonicAddress BRYGGERS_DOOR = new VisonicAddress(0x03, 0x19, 0x15);
  private static final VisonicAddress MAIN_DOOR = new VisonicAddress(0x02, 0xcf, 0xd5);
  private static final FS20Address CARPORT_SPOTS = new FS20Address(HOUSE, 3111);
  
  private static final Dimmer carportSpots = new Dimmer("Carport spots", CARPORT_SPOTS);
  private static final Switch bryggersSpots = new Switch("Bryggers ceiling", new FS20Address(HOUSE, 1211), MASTER, ALL_LIGHTS, BRYGGERS);
  
  private static final Dimmer livingRoomCeiling = new Dimmer("Living room ceiling", new FS20Address(HOUSE, 1111), MASTER, ALL_LIGHTS, LIVING_ROOM);
  private static final Switch livingRoomTableLamp = new Switch("Table lamp", new FS20Address(HOUSE, 1112), MASTER, ALL_LIGHTS, LIVING_ROOM);
  private static final Switch livingRoomReadingLamp = new Switch("Reading lamp", new FS20Address(HOUSE, 1113), MASTER, ALL_LIGHTS, LIVING_ROOM);
  private static final Switch livingRoomCornerLamp = new Switch("Corner lamp", new FS20Address(HOUSE, 1114), MASTER, ALL_LIGHTS, LIVING_ROOM);
  
  private @Autowired FS20Service fs20Service;
  private @Autowired SFX sfx;
  private @Autowired XBMCService xbmcService;
  
  private Settings settings = new Settings();
  
  @PostConstruct
  public void started() {
    //sfx.play("st-good.wav");
    setReceivers(
      livingRoomCeiling,  
      livingRoomTableLamp,  
      livingRoomReadingLamp,  
      livingRoomCornerLamp,
      
      bryggersSpots,
      new Dimmer("Guest bathroom", new FS20Address(HOUSE, 1212), MASTER, ALL_LIGHTS, BRYGGERS),
      
      new Dimmer("Bedroom cupboards", new FS20Address(HOUSE, 1311), MASTER, ALL_LIGHTS, BEDROOM),
      new Switch("Bedroom LED strip", new FS20Address(HOUSE, 1312), MASTER, ALL_LIGHTS, BEDROOM),
      
      new Switch("RGB Lamp", new FS20Address(HOUSE, 1411), DININGROOM),
      
      carportSpots,
      
      new FS20Route(new FS20Address(BUTTONS, 1111), Command.OFF) {
        protected void handle() {
          livingRoomCeiling.onFull();
          livingRoomReadingLamp.onFull();
          livingRoomTableLamp.off();
          livingRoomCornerLamp.off();
          fs20Service.queueFS20(new FS20Packet (BEDROOM, Command.OFF));
        }
      },
      new FS20Route(new FS20Address(BUTTONS, 1111), Command.ON_PREVIOUS) {
        protected void handle() {
          livingRoomCeiling.dim(1);
          livingRoomTableLamp.onFull();
          livingRoomCornerLamp.onFull();
          livingRoomReadingLamp.off();
          fs20Service.queueFS20(new FS20Packet (BEDROOM, Command.OFF));
        }
      },
      new FS20Route(new FS20Address(BUTTONS, 1112), Command.OFF) {
        protected void handle() {
          fs20Service.queueFS20(new FS20Packet (BEDROOM, Command.ON_PREVIOUS));
          fs20Service.queueFS20(new FS20Packet (LIVING_ROOM, Command.OFF));
        }
      },
      new FS20Route(new FS20Address(BUTTONS, 1112), Command.ON_PREVIOUS) {
        protected void handle() {
          fs20Service.queueFS20(new FS20Packet (BEDROOM, Command.OFF));
          fs20Service.queueFS20(new FS20Packet (LIVING_ROOM, Command.OFF));
        }
      },
      /* old?
      new FS20Route(new FS20Address(HOUSE, 3111), Command.TIMED_ON_PREVIOUS, Command.TIMED_ON_FULL) {
        protected void handle() {
          sfx.play("tngchime.wav");
        }
      }, */
      new FS20Route(new FS20Address(SENSORS, 3111), Command.TIMED_ON_PREVIOUS, Command.TIMED_ON_FULL) {
        protected void handle() {
          log.info("Motion on left driveway sensor");
          getEnvironment().getGrowlService().sendMotion("Driveway, left side");
          //sfx.play("tngchime.wav");
          if (isDark()) {
            carportSpots.timedOn(180);            
          }
        }
      },
      new FS20Route(new FS20Address(SENSORS, 3112), Command.TIMED_ON_PREVIOUS, Command.TIMED_ON_FULL) {
        protected void handle() {
          log.info("Motion on right driveway sensor");
          getEnvironment().getGrowlService().sendMotion("Driveway, right side");
          //sfx.play("tngchime.wav");
          if (isDark()) {
            carportSpots.timedOn(180);            
          }
        }
      },
      new FS20Route(new FS20Address(SENSORS, 3113), Command.TIMED_ON_PREVIOUS, Command.TIMED_ON_FULL) {
        protected void handle() {
          log.info("Motion on carport sensor");
          getEnvironment().getGrowlService().sendMotion("Carport");
          if (!settings.isMuteMotion()) {
            sfx.play("tngchime.wav");
          }
          if (isDark()) {
            carportSpots.timedOn(300);            
          }
        }
      },
      new VisonicRoute.DoorClosed(BRYGGERS_DOOR) {
        protected void handle(VisonicPacket packet) {
          if (isDark() && !settings.isMuteDoors()) {
            sfx.play("brdgbtn1.wav");
          }
        }        
      },
      new VisonicRoute.DoorClosed(MAIN_DOOR) {
        protected void handle(VisonicPacket packet) {
          if (isDark() && !settings.isMuteDoors()) {
            sfx.play("brdgbtn1.wav");
          }
        }        
      },
      new VisonicRoute.DoorOpen(BRYGGERS_DOOR) {
        protected void handle(VisonicPacket packet) {
          if (isDark()) {
            bryggersSpots.timedOn(300);
          }
        }        
      },
      new MotionSensor("Guestroom", new VisonicAddress(0x03, 0x04, 0x83)),
      new MotionSensor("Kitchen", new VisonicAddress(0x04, 0x05, 0x03)),
      new MotionSensor("Office", new VisonicAddress(0x01, 0xc4, 0x83)),
      new MotionSensor("Bedroom", new VisonicAddress(0x04, 0xff, 0x1d)),
      new MotionSensor("Studio", new VisonicAddress(0x01, 0x84, 0x83)),
      new MotionSensor("Living room", new VisonicAddress(0x02, 0xc4, 0x83)),
      new DoorSensor("Main door", MAIN_DOOR),
      new DoorSensor("Bryggers door", BRYGGERS_DOOR)
    );
    
    xbmcService.on(State.PLAYING, new Runnable() {
      public void run() {
        if (isDark()) {
          livingRoomCeiling.dim(1);
          livingRoomTableLamp.onFull();
          livingRoomCornerLamp.onFull();
          livingRoomReadingLamp.off();                  
        }
      }      
    });
    xbmcService.on(State.PAUSED, new Runnable() {
      public void run() {
        if (isDark()) {
          livingRoomCeiling.dim(8);
        }
      }      
    });
    xbmcService.on(State.STOPPED, new Runnable() {
      public void run() {
        if (isDark()) {
          livingRoomCeiling.dim(8);
          livingRoomCornerLamp.off();
          livingRoomTableLamp.off();
        }
      }      
    });
    register(new TimedTask(new FixedTime(7, 00), SUNRISE.plusHours(1)) {
      @Override
      protected void start(long duration) {
        bryggersSpots.timedOnMillis(duration);
      }
    });
    register(new TimedTask(SUNSET.plusHours(-2), new FixedTime(23,00)) {
      @Override
      protected void start(long duration) {
        bryggersSpots.timedOnMillis(duration);
      }
    });
  }

  @Path("settings")
  @GET
  public synchronized Settings getSettings() {
    return settings;
  }
  
  @Path("settings")
  @PUT
  public synchronized void setSettings(Settings settings) {
    this.settings = settings;
  }
}
