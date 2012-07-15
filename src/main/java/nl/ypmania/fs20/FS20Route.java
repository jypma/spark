package nl.ypmania.fs20;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import nl.ypmania.env.Receiver;

public abstract class FS20Route extends Receiver {
  private FS20Address address;
  private Set<Command> commands;
  
  public FS20Route(FS20Address address, Command... commands) {
    this.address = address;
    this.commands = new HashSet<Command>();
    this.commands.addAll(Arrays.asList(commands));
  }

  public FS20Route(FS20Address address) {
    this.address = address;
    this.commands = null;
  }

  @Override
  public final void receive(FS20Packet packet) {
    if (commands != null) {
      if (!commands.contains(packet.getCommand())) return;
    }
    if (address != null) {
      if (!packet.getAddress().equals(address)) return;
    }
    handle();
  }
  
  protected abstract void handle();
}
