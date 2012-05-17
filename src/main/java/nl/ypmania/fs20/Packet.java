package nl.ypmania.fs20;

public class Packet {
  private Address address;
  private Command command;
  
  public Packet(Address address, Command command) {
    this.address = address;
    this.command = command;
  }
  
  public Address getAddress() {
    return address;
  }
  
  public Command getCommand() {
    return command;
  }
}
