package nl.ypmania.fs20;

import nl.ypmania.env.Receiver;

public abstract class FS20Route extends Receiver {
  private FS20Address address;
  private Command command;
  
  public FS20Route(FS20Address address, Command command) {
    this.address = address;
    this.command = command;
  }

  @Override
  public final void receive(FS20Packet packet) {
    if (packet.getAddress().equals(address) && packet.getCommand().equals(command)) {
      handle();
    }
  }
  
  protected abstract void handle();
}
