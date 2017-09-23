package nl.ypmania.rf12.state;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

import nl.ypmania.env.Environment;
import nl.ypmania.env.Zone;
import nl.ypmania.rf12.RF12Packet;

/**
 * Reliably transmits a changing current state to a target node, that is using RxState.hpp to receive it.
 */
public class TxState {
  private static final Logger log = LoggerFactory.getLogger(TxState.class);
  private static final int[] RESEND_DELAYS = {1,2,3,5,8,13,21,34,55,89,144};
  private static final int MAX_RESEND = 200; // 200 * 1.5secs = retry for 5 minutes
  
  private final int nodeId;
  private final Zone zone;
  private final int resendOffset;
  
  private int seq = 0;
  private int resendCount = 0;
  private ByteString state;
  private Environment env;
  private boolean tx = false;
  private TimerTask resend = null;

  public TxState(Environment env, Zone zone, int nodeId, ByteString initial) {
    this.env = env;
    this.nodeId = nodeId;
    this.state = initial;
    this.zone = zone;
    this.resendOffset = (((nodeId) ^ (nodeId >> 4) ^ (nodeId >> 8) ^ (nodeId >> 12)) & 0x000F);
  }
  
  public synchronized void setState(ByteString s) {
    if (!s.equals(state)) {
      seq = (seq + 1) % 256;
      state = s;
      resendCount = 0;
      log.info("Setting new state for {}/{}, seq now {}", new Object[] { zone, nodeId, seq });
      send();      
    }
  }
  
  public synchronized void receive(RF12Packet p) {
    Ack ack = Ack.fromRF12OrNull(p);
    if (ack != null && ack.getNodeId() == nodeId && ack.getSeq() == seq) {
      tx = false;
      if (resend != null) {
        resend.cancel();
      }
      resendCount = 0;
    } else if (ack != null) {
      log.debug("Ignoring ack nodeId={} seq={}, expecting nodeId={} seq={}", new Object[] { ack.getNodeId(), ack.getSeq(), nodeId, seq });
    }
  }
  
  private synchronized void send() {
    log.debug("Going to send state for {}/{}, attempt {}", new Object[] { zone, nodeId, resendCount });
    final Packet p = new Packet(Packet.Direction.Tx, nodeId, seq, state);
    tx = true;
    env.onRf868Clear(new Runnable() {
      public void run() {
        synchronized(TxState.this) {
          log.info("Sending state for {}/{}, attempt {}", new Object[] { zone, nodeId, resendCount });
          env.getRf12Service().queue(zone, p.toRF12Packet());
          if (resend != null) {
            resend.cancel();
          }
          resend = new TimerTask() {
            public void run() {
              synchronized(TxState.this) {
                if (tx) {
                  if (resendCount < MAX_RESEND) {
                    resendCount++;
                    send();              
                  } else {
                    log.error("Giving up sending state for {}/{}", zone, nodeId);                    
                  }
                }
              }
            }
          };
          int delayIdx = Math.min(resendCount, RESEND_DELAYS.length - 1);
          env.getTimer().schedule(resend, 10 * (RESEND_DELAYS[delayIdx] + resendOffset));
        }
      }
    });
  }
}
