package nl.ypmania.visonic;

import nl.ypmania.env.Receiver;

public abstract class VisonicRoute extends Receiver {
  private VisonicAddress address;
  
  public VisonicRoute(VisonicAddress address) {
    this.address = address;
  }

  @Override
  public final void receive(VisonicPacket packet) {
    if (packet.getAddress().equals(address)) {
      handle(packet);
    }
  }
  
  protected abstract void handle(VisonicPacket packet);
}
