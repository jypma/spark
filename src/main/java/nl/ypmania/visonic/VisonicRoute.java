package nl.ypmania.visonic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.ypmania.env.Receiver;

public abstract class VisonicRoute extends Receiver {
  private static final Logger log = LoggerFactory.getLogger(VisonicRoute.class);
  
  private VisonicAddress address;
  private Boolean clear;
  private Boolean event;
  
  public VisonicRoute(VisonicAddress address) {
    this.address = address;
  }

  public VisonicRoute(VisonicAddress address, Boolean clear, Boolean event) {
    this.address = address;
    this.clear = clear;
    this.event = event;
  }

  @Override
  public final void receive(VisonicPacket packet) {
    log.debug ("Checking for " + clear + "/" + event + ", " + address + ", packet is " + packet);
    if (clear != null) {      
      if (packet.isClear() != clear) return;
    }
    if (event != null) {      
      if (packet.isEvent() != event) return;
    }
    if (address != null) {
      if (!packet.getAddress().equals(address)) return;
    }
    handle(packet);
  }
  
  protected abstract void handle(VisonicPacket packet);
  
  public static abstract class DoorOpen extends VisonicRoute {
    //TODO check that it's actually a door
    public DoorOpen() {
      super (null, false, true);
    }
    public DoorOpen(VisonicAddress address) {
      super (address, false, true);
    }
  }

  public static abstract class DoorClosed extends VisonicRoute {
    //TODO check that it's actually a door
    public DoorClosed() {
      super (null, true, true);
    }
    public DoorClosed(VisonicAddress address) {
      super (address, true, true);
    }
  }
}
