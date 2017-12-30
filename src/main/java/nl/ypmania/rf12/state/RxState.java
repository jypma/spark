package nl.ypmania.rf12.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

import nl.ypmania.env.Environment;
import nl.ypmania.env.Zone;
import nl.ypmania.rf12.RF12Packet;
import nl.ypmania.rf12.state.Packet.Direction;

public class RxState {
  private static final Logger log = LoggerFactory.getLogger(RxState.class);
  
  private final int nodeId;
  private final Zone zone;
  private final Environment env;
  
  private int seq = 0;
  private ByteString state;
  
  public RxState(Environment env, Zone zone, int nodeId, ByteString initial) {
    this.env = env;
    this.nodeId = nodeId;
    this.state = initial;
    this.zone = zone;
  }

  public synchronized ByteString getState() {
    return state;
  }
  
  public int getSeqNr() {
    return seq;
  }
  
  public synchronized void receive(RF12Packet p) {
    Packet packet = Packet.fromRF12OrNull(p);
    if (packet != null && packet.getNodeId() == nodeId && packet.getDirection() == Direction.Rx) {
      this.state = packet.getBody();
      this.seq = packet.getSeq();
      log.info("Sending ack for {}/{}, seq now {}", new Object[] { zone, nodeId, seq } );
      env.getRf12Service().queue(zone, new Ack(nodeId, seq).toRF12Packet());
      onStateChanged();
    }
  }
  
  protected void onStateChanged() {}
}
