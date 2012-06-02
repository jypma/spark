package nl.ypmania.fs20;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Environment {
  private List<Receiver> receivers;
  
  public Environment (Receiver... receivers) {
    this.receivers = Arrays.asList(receivers);
  }
  
  public void receive (Packet packet) {
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
