package nl.ypmania.fs20;

public abstract class Route extends Receiver {
  private Address address;
  private Command command;
  
  public Route(Address address, Command command) {
    this.address = address;
    this.command = command;
  }

  @Override
  public final void receive(Packet packet) {
    if (packet.getAddress().equals(address) && packet.getCommand().equals(command)) {
      handle();
    }
  }
  
  protected abstract void handle();
}
