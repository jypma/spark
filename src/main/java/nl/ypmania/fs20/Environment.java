package nl.ypmania.fs20;

import java.util.Arrays;
import java.util.List;

public class Environment {
  private List<Receiver> receivers;
  
  public Environment (Receiver... receivers) {
    this.receivers = Arrays.asList(receivers);
  }
  
  public void handle (Packet packet) {
    for (Receiver receiver: receivers) {
      receiver.handle(packet);
    }
  }
}
