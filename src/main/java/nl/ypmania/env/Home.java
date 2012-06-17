package nl.ypmania.env;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Home extends Environment {
  private static final int HOUSE = 12341234;
  private static final int BUTTONS = 12344444;
  
  private static final FS20Address MASTER = new FS20Address(HOUSE, 4444);
  private static final FS20Address ALL_LIGHTS = new FS20Address(HOUSE, 4411);
  private static final FS20Address LIVING_ROOM = new FS20Address(HOUSE, 1144);
  private static final FS20Address BRYGGERS = new FS20Address(HOUSE, 1244);
  private static final FS20Address BEDROOM = new FS20Address(HOUSE, 1344);
  
  private @Autowired FS20Service fs20Service;
  
  public Home() {
    setReceivers(
      new Dimmer("Living room ceiling", MASTER, ALL_LIGHTS, LIVING_ROOM, new FS20Address(HOUSE, 1111)),  
      new Switch("Table lamp", MASTER, ALL_LIGHTS, LIVING_ROOM, new FS20Address(HOUSE, 1112)),  
      new Switch("Reading lamp", MASTER, ALL_LIGHTS, LIVING_ROOM, new FS20Address(HOUSE, 1113)),  
      new Switch("Corner lamp", MASTER, ALL_LIGHTS, LIVING_ROOM, new FS20Address(HOUSE, 1114)),
      
      new Switch("Bryggers ceiling", MASTER, ALL_LIGHTS, BRYGGERS, new FS20Address(HOUSE, 1211)),
      new Dimmer("Guest bathroom", MASTER, ALL_LIGHTS, BRYGGERS, new FS20Address(HOUSE, 1212)),
      
      new Dimmer("Bedroom cupboards", MASTER, ALL_LIGHTS, BEDROOM, new FS20Address(HOUSE, 1311)),
      new Switch("Bedroom LED strip", MASTER, ALL_LIGHTS, BEDROOM, new FS20Address(HOUSE, 1312)),
      
      new FS20Route(new FS20Address(BUTTONS, 1212), Command.OFF) {
        protected void handle() {
          fs20Service.queueFS20(new FS20Packet (LIVING_ROOM, Command.OFF));
        }
      },
      new FS20Route(new FS20Address(BUTTONS, 1212), Command.ON_PREVIOUS) {
        protected void handle() {
          fs20Service.queueFS20(new FS20Packet (LIVING_ROOM, Command.ON_FULL));
        }
      },
      new MotionSensor("Guestroom", new VisonicAddress(0x03, 0x04, 0x83)),
      new MotionSensor("Kitchen", new VisonicAddress(0x04, 0x05, 0x03)),
      new MotionSensor("Office", new VisonicAddress(0x01, 0xc4, 0x83)),
      new MotionSensor("Bedroom", new VisonicAddress(0x04, 0xff, 0x1d)),
      new MotionSensor("Studio", new VisonicAddress(0x01, 0x84, 0x83)),
      new MotionSensor("Living room", new VisonicAddress(0x02, 0xc4, 0x83)),
      new DoorSensor("Main door", new VisonicAddress(0x02, 0xcf, 0xd5)),
      new DoorSensor("Bryggers door", new VisonicAddress(0x03, 0x19, 0x15))
    );
  }

}
