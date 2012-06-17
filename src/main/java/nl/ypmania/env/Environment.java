package nl.ypmania.env;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.ypmania.fs20.FS20Packet;
import nl.ypmania.visonic.VisonicPacket;

public class Environment {
  private List<Receiver> receivers = new ArrayList<Receiver>();
  
  public void setReceivers (Receiver... receivers) {
    this.receivers = Arrays.asList(receivers);
  }
  
  public void receive (FS20Packet packet) {
    for (Receiver receiver: receivers) {
      receiver.receive(packet);
    }
  }
  
  public void receive (VisonicPacket packet) {
    for (Receiver receiver: receivers) {
      receiver.receive(packet);
    }    
  }

  @SuppressWarnings("unchecked")
  public <T extends Receiver> List<T> getAll (Class<T> type) {
    List<T> result = new ArrayList<T>();
    for (Receiver r: receivers) {
      if (type.isInstance(r))
        result.add((T) r);
    }
    return result;
  }
  
}
