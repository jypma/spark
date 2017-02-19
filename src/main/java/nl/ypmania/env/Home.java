package nl.ypmania.env;

import java.util.Arrays;
import java.util.TimerTask;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import nl.ypmania.fs20.Command;
import nl.ypmania.fs20.Dimmer;
import nl.ypmania.fs20.FS20Address;
import nl.ypmania.fs20.FS20MotionSensor;
import nl.ypmania.fs20.FS20Packet;
import nl.ypmania.fs20.FS20Route;
import nl.ypmania.fs20.FS20Service;
import nl.ypmania.fs20.Switch;
import nl.ypmania.rf12.Doorbell;
import nl.ypmania.rf12.ElectricityMeter;
import nl.ypmania.rf12.GardenValve;
import nl.ypmania.rf12.HumidityRoomSensor;
import nl.ypmania.rf12.MultiButton;
import nl.ypmania.rf12.OutdoorSensor;
import nl.ypmania.rgb.LampColor;
import nl.ypmania.rgb.RGBLamp;
import nl.ypmania.visonic.DoorSensor;
import nl.ypmania.visonic.SensorDTO;
import nl.ypmania.visonic.VisonicAddress;
import nl.ypmania.visonic.VisonicMotionSensor;
import nl.ypmania.xbmc.XBMCService;
import nl.ypmania.xbmc.XBMCService.State;
import nl.ypmania.zoneminder.ZoneMinderService;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/home")
@Component
public class Home extends Environment {
  private static final Logger log = LoggerFactory.getLogger(Home.class);
  
  public static final String ALARM_NIGHT = "NIGHT";
  public static final String ALARM_ALL = "ALL";
  
  private static final int HOUSE = 12341234;
  private static final int BUTTONS = 12344444;
  private static final int SENSORS = 12343333;
  
  private static final FS20Address MASTER = new FS20Address(HOUSE, 4444);
  private static final FS20Address ALL_LIGHTS = new FS20Address(HOUSE, 4411);
  private static final FS20Address LIVING_ROOM = new FS20Address(HOUSE, 1144);
  private static final FS20Address BRYGGERS = new FS20Address(HOUSE, 1244);
  private static final FS20Address BEDROOM = new FS20Address(HOUSE, 1344);
  //private static final FS20Address DININGROOM = new FS20Address(HOUSE, 1444);
  private static final FS20Address CARPORT = new FS20Address(HOUSE, 3144);
  
  private static final VisonicAddress BRYGGERS_DOOR = new VisonicAddress(141, 137, 106);
  private static final VisonicAddress MAIN_DOOR = new VisonicAddress(55, 191, 122);
  private static final VisonicAddress LIVING_ROOM_SENSOR = new VisonicAddress(54, 18, 28);
  private static final VisonicAddress BEDROOM_SENSOR = new VisonicAddress(243, 143, 27);
  private static final FS20Address CARPORT_SPOTS = new FS20Address(HOUSE, 3111);
  
  private static final String LIVING_ROOM_PROXY = "18:FE:34:9F:50:92";
  private static final String SHED_PROXY = "18:FE:34:9F:50:DB";
  private static final String KITCHEN_PROXY = "18:FE:34:9F:52:05";
  
  private @Autowired FS20Service fs20Service;
  private @Autowired SFX sfx;
  private @Autowired XBMCService xbmcService;
  private @Autowired ZoneMinderService zoneMinderService;
  
  private Settings settings = new Settings();
  
  private DateTime doorOpenNotified = null;
  Zone drivewayLeft = new Zone(this, "Driveway, l", Arrays.asList(SHED_PROXY, KITCHEN_PROXY));
  Zone drivewayRight = new Zone(this, "Driveway, r", Arrays.asList(SHED_PROXY, KITCHEN_PROXY));
  Zone carport = new Zone(this, "Carport", Arrays.asList(SHED_PROXY, KITCHEN_PROXY));
  Zone shed = new Zone(this, "Shed", Arrays.asList(SHED_PROXY, KITCHEN_PROXY));
  Zone outside = new Zone(this, "Outside", drivewayLeft, drivewayRight, carport, shed);
  Zone bryggers = new Zone(this, "Bryggers", Arrays.asList(KITCHEN_PROXY, SHED_PROXY));
  Zone entree = new Zone(this, "Entree", Arrays.asList(KITCHEN_PROXY, SHED_PROXY, LIVING_ROOM_PROXY));
  Zone office = new Zone(this, "Office", Arrays.asList(KITCHEN_PROXY, LIVING_ROOM_PROXY, SHED_PROXY));
  Zone kidsRoom = new Zone(this, "Kid's room", Arrays.asList(KITCHEN_PROXY, LIVING_ROOM_PROXY, SHED_PROXY));
  Zone guestRoom = new Zone(this, "Guestroom", Arrays.asList(KITCHEN_PROXY, SHED_PROXY, LIVING_ROOM_PROXY));
  Zone kitchen = new Zone(this, "Kitchen", Arrays.asList(KITCHEN_PROXY, LIVING_ROOM_PROXY, SHED_PROXY));
  Zone studio = new Zone(this, "Studio", Arrays.asList(KITCHEN_PROXY, LIVING_ROOM_PROXY));
  Zone livingRoom = new Zone(this, "Livingroom", Arrays.asList(LIVING_ROOM_PROXY, KITCHEN_PROXY));
  Zone bedRoom = new Zone(this, "Bedroom", Arrays.asList(LIVING_ROOM_PROXY, KITCHEN_PROXY));
  Zone mainBathRoom = new Zone(this, "Main bathroom", Arrays.asList(LIVING_ROOM_PROXY, KITCHEN_PROXY));
  Zone day = new Zone(this, "Day", bryggers, entree, office, kidsRoom, guestRoom, kitchen, studio, livingRoom);
  Zone night = new Zone(this, "Night", bedRoom, mainBathRoom);
  Zone inside = new Zone(this, "Inside", day, night);
  Zone home = new Zone(this, "Area", inside, outside);
  
  Dimmer bedroomCupboards = new Dimmer(bedRoom, "Cupboards", new FS20Address(HOUSE, 1311), MASTER, ALL_LIGHTS, BEDROOM);
  Switch bedroomLEDs = new Switch(bedRoom, "LED strip", new FS20Address(HOUSE, 1312), MASTER, ALL_LIGHTS, BEDROOM);
  Dimmer carportSpots = new Dimmer(carport, entree, "Spots", CARPORT_SPOTS);
  Switch carportFlood = new Switch(carport, entree, "Floodlight", new FS20Address(HOUSE, 3112), MASTER, ALL_LIGHTS, CARPORT);
  Switch bryggersSpots = new Switch(bryggers, "Ceiling", new FS20Address(HOUSE, 1211), MASTER, ALL_LIGHTS, BRYGGERS);
  
  Dimmer livingRoomCeiling = new Dimmer(livingRoom, "Ceiling", new FS20Address(HOUSE, 1111), MASTER, ALL_LIGHTS, LIVING_ROOM);
  Switch livingRoomTableLamp = new Switch(livingRoom, "Table lamp", new FS20Address(HOUSE, 1112), MASTER, ALL_LIGHTS, LIVING_ROOM);
  Switch livingRoomReadingLamp = new Switch(livingRoom, "Reading lamp", new FS20Address(HOUSE, 1113), MASTER, ALL_LIGHTS, LIVING_ROOM);
  Switch livingRoomCornerLamp = new Switch(livingRoom, "Corner lamp", new FS20Address(HOUSE, 1114), MASTER, ALL_LIGHTS, LIVING_ROOM);
  
  GardenValve valve1 = new GardenValve(outside, studio, '1', 5);
  Switch xmasLights = new Switch(kitchen, "X-Mas Lights", new FS20Address(HOUSE, 2211));
  
  //Switch rgbLamp = new Switch(livingRoom, "RGB Lamp", new FS20Address(HOUSE, 1411), DININGROOM);
  RGBLamp kitchenLeds = new RGBLamp(kitchen, "Kitchen LEDs", (int)'L', (int)'K');
  
  DateTime lastDoorbellEmail = null;
  private final Doorbell doorbell = new Doorbell(carport, 'D','B') {
    @Override protected void ring() {
      zoneMinderService.triggerEvent(1, 30, 10, "Doorbell", "Doorbell");
      if (!settings.isMuteDoorbell()) {
        sfx.play("doorbell.01.wav");            
      }
      if (inside.noActionSinceMinutes(30)) {
        if (lastDoorbellEmail == null || lastDoorbellEmail.plusMinutes(3).isBeforeNow()) {
          getEmailService().sendMail("Doorbell", "Somebody just rang the doorbell, but it seems nobody is home.");
          lastDoorbellEmail = DateTime.now();
        }
      }
    }
  };
  
  private void handleOpenDoor() {
    boolean obvious = office.actionSinceSeconds(15) || kitchen.actionSinceSeconds(15) || kidsRoom.actionSinceSeconds(15);
    if (!settings.isMuteDoors() && !obvious) {
      doorOpenNotified = DateTime.now();
      sfx.play("tngchime.wav");         
      getTimer().schedule(new TimerTask(){
        @Override
        public void run() {
          doorOpenNotified = null;
        }
      }, 60000);
    }
  }
  
  private void handleCloseDoor() {
    if (doorOpenNotified != null) {
      if (DateTime.now().isAfter(doorOpenNotified.plusSeconds(30))) {
        sfx.play("brdgbtn1.wav");        
      }
    }
    
    doorOpenNotified = null;
  }
  
  DoorSensor bryggersDoor = new DoorSensor(bryggers, "Bryggers door", BRYGGERS_DOOR) {
    protected void opened() {
      handleOpenDoor();
      if (isDark()) {
        bryggersSpots.timedOn(300);
        carportFlood.timedOn(120);
        carportSpots.timedOn(120);                        
      }
    }
    protected void closed() {
      handleCloseDoor();
    }        
  };
  
  @PostConstruct
  public void started() {
    
    setReceivers(
      livingRoomCeiling,  
      livingRoomTableLamp,  
      livingRoomReadingLamp,  
      livingRoomCornerLamp,
      
      bryggersSpots,
      kitchenLeds,
      new Dimmer(entree, "Guest bathroom", new FS20Address(HOUSE, 1212), MASTER, ALL_LIGHTS, BRYGGERS),
      
      bedroomCupboards,
      bedroomLEDs,
      valve1,
      
      //rgbLamp,
      carportSpots,
      carportFlood,
      xmasLights,
      
      new OutdoorSensor(outside, "strawberries", 'a'),
      new OutdoorSensor(outside, "grapes", 'b'),
      
      new FS20Route(new FS20Address(BUTTONS, 1111), Command.OFF) {
        protected void handle() {
          livingRoom.event(ZoneEvent.buttonPressed());
          livingRoomCeiling.onFull();
          livingRoomReadingLamp.onFull();
          livingRoomTableLamp.off();
          livingRoomCornerLamp.off();
          fs20Service.queueFS20(livingRoom, new FS20Packet (BEDROOM, Command.OFF));
        }
      },
      new FS20Route(new FS20Address(BUTTONS, 1111), Command.ON_PREVIOUS) {
        protected void handle() {
          livingRoom.event(ZoneEvent.buttonPressed());
          livingRoomCeiling.dim(1);
          livingRoomTableLamp.onFull();
          livingRoomCornerLamp.onFull();
          livingRoomReadingLamp.off();
          fs20Service.queueFS20(livingRoom, new FS20Packet (BEDROOM, Command.OFF));
        }
      },
      new FS20Route(new FS20Address(BUTTONS, 1112), Command.OFF) {
        protected void handle() {
          livingRoom.event(ZoneEvent.buttonPressed());
          fs20Service.queueFS20(livingRoom, new FS20Packet (BEDROOM, Command.ON_PREVIOUS));
          fs20Service.queueFS20(livingRoom, new FS20Packet (LIVING_ROOM, Command.OFF));
          livingRoomReadingLamp.off();
          //rgbLamp.off();
        }
      },
      new FS20Route(new FS20Address(BUTTONS, 1112), Command.ON_PREVIOUS) {
        protected void handle() {
          livingRoom.event(ZoneEvent.buttonPressed());
          fs20Service.queueFS20(livingRoom, new FS20Packet (BEDROOM, Command.OFF));
          fs20Service.queueFS20(livingRoom, new FS20Packet (LIVING_ROOM, Command.OFF));
          livingRoomReadingLamp.off();
          //rgbLamp.off();
        }
      },
      new FS20MotionSensor(drivewayLeft, "Driveway, left side", new FS20Address(SENSORS, 3111)) {
        protected void motion() {
          if (isDark() && !settings.isNoAutoLightsCarport()) {
            carportFlood.timedOn(180);
            carportSpots.timedOn(180);            
          }
        }
      },
      new FS20MotionSensor(drivewayRight, "Driveway, right side", new FS20Address(SENSORS, 3112)) {
        protected void motion() {
          if (isDark() && !settings.isNoAutoLightsCarport()) {
            carportFlood.timedOn(180);
            carportSpots.timedOn(180);            
          }
        }
      },
      new FS20MotionSensor(carport, "Carport", new FS20Address(SENSORS, 3113)) {
        protected void motion() {
          zoneMinderService.triggerEvent(1, 30, 10, "CarportMotion", "Motion on carport");
          if (!settings.isMuteMotion() && 
              (bryggersDoor.isClosed() || bryggersDoor.isOpenAtLeastSeconds(60))) {
            sfx.play("tngchime.wav");
          }
          if (isDark() && !settings.isNoAutoLightsCarport()) {
            carportFlood.timedOn(300);
            carportSpots.timedOn(300);            
          }
        }
      },
      new VisonicMotionSensor(office, "Office", new VisonicAddress(13, 18, 28)),
      new VisonicMotionSensor(kitchen, "Kitchen", new VisonicAddress(3, 10, 28)) {
        protected void motion() {
          if (!settings.isNoAutoLightsKitchen()) {
            xmasLights.timedOn(30 * 60);            
          }
          log.debug("Considering turning on kitchen={}/{}, dark={}", new Object[] { kitchenLeds.isOn(), kitchenLeds.getColor(), isDark() });
          if (kitchenLeds.isOn()) {
            kitchenLeds.timedOn(kitchenLeds.getColor(), 30 * 60);
          } else
          if (isDark() && !settings.isNoAutoLightsKitchen()) {
            if (!kitchenLeds.isOn()) {
              kitchenLeds.timedOn(new LampColor(255, 255, 255, 4), 30 * 60);              
            }
          }
        }
      },
      new VisonicMotionSensor(shed, "Shed", new VisonicAddress(144, 151, 28)),
      new VisonicMotionSensor(kidsRoom, "Kid's room", new VisonicAddress(57, 18, 28)),
      new VisonicMotionSensor(bedRoom, "Bedroom", BEDROOM_SENSOR),
      new VisonicMotionSensor(studio, "Studio", new VisonicAddress(25, 18, 28)),
      new VisonicMotionSensor(guestRoom, "Guestroom", new VisonicAddress(104, 194, 43)),
      new VisonicMotionSensor(livingRoom, "Living room", LIVING_ROOM_SENSOR) {
        protected void motion() {
          if (isDark()) {
            log.debug("Considering turning on living room. Current brightness {}, timed: {}", 
                livingRoomCeiling.getBrightness(), livingRoomCeiling.isTimedOn());
            if (livingRoomCeiling.getBrightness() == 0) {
              if (settings.isNoAutoLightsLiving()) {
                log.debug("Not turning on, disabled in settings.");
              } else {                
                livingRoomCeiling.timedDim(1, 1200);
              }
            } else {
              livingRoomCeiling.timedDim(livingRoomCeiling.getBrightness(), 1200);
            }
          }
        }
      },
      new DoorSensor(entree, "Main door", MAIN_DOOR) {
        protected void opened() {
          handleOpenDoor();
        }
        protected void closed() {
          handleCloseDoor();
        }
      },
      bryggersDoor,
      
      doorbell,
      
//      new RoomSensor(bryggers, "Bryggers", (int)'1'),
      new HumidityRoomSensor(livingRoom, "Stue", "Stue_H", (int)'2'),
      new HumidityRoomSensor(bedRoom, "Bedroom_T", "Bedroom_H", (int)'4'),
      new HumidityRoomSensor(studio, "Studio", null, (int)'5'),
      new HumidityRoomSensor(office, "Office", null, (int)'6'),
      new ElectricityMeter(bryggers, (int)'1'),
      
      //new RGBLamp(livingRoom, "Plantlamp", (int)'R', (int)'G'),
      new RGBLamp(bedRoom, "Sleepstrip", (int)'L', (int)'2'),
      
      new Dimmer(mainBathRoom, "Spots", new FS20Address(HOUSE, 2111), new FS20Address(HOUSE, 2144), ALL_LIGHTS, MASTER),
      
      new MultiButton(entree, "colorbuttons", '1') {
        protected void onButton(int i) {
          sfx.play("button" + i + ".wav");
        };
      }
    );
    
    xbmcService.setLocation(livingRoom);
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
          livingRoomCeiling.timedDim(8, 900);
          livingRoomCornerLamp.off();
          livingRoomTableLamp.off();
        }
      }      
    });
    register(new TimedTask(new FixedTime(7, 00).duringWeekendPlusHours(2), SUNRISE.plusHours(1)) {
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
    /*
    register(new TimedTask(new FixedTime(7, 00), SUNRISE.plusHours(1)) {
      @Override
      protected void start(long duration) {
        rgbLamp.timedOnMillis(duration);
      }
    });
    register(new TimedTask(SUNSET.plusHours(-1), new FixedTime(22,00)) {
      @Override
      protected void start(long duration) {
        rgbLamp.timedOnMillis(duration);
      }
    });
    */
    new LightWatchdog(this, bedRoom, livingRoom)/*.ignore(rgbLamp)*/;
    /*// vacation
    register(new TimedTask(new FixedTime(15, 00), new FixedTime(23, 00)) {
      @Override
      protected void start(long duration) {
        livingRoomCeiling.timedDim(6, (int) (duration / 1000));
        livingRoomCornerLamp.timedOnMillis(duration);
        livingRoomTableLamp.timedOnMillis(duration);
      }
    });
    register(new TimedTask(new FixedTime(21, 00), new FixedTime(23, 30)) {
      @Override
      protected void start(long duration) {
        bedroomCupboards.timedOnMillis(duration);
        bedroomLEDs.timedOnMillis(duration);
      }
    });
    */
    new LightWatchdog(this, carport);
  }
  
  @Override
  public boolean isAlarmArmed(Zone zone) {
    String alarm = settings.getAlarmMode();
    if (alarm != null) {
      return zone.getName().equalsIgnoreCase(alarm);
    } else
      return false;
  }
  
  @Path("zone")
  @GET
  public Zone getZone() {
    return home;
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
  
  @Path("doorbell")
  @GET
  public SensorDTO getDoorbell() {
    return new SensorDTO(doorbell);
  }
}
