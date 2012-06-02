package nl.ypmania.node;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nl.ypmania.alarm.AlarmDecoder;
import nl.ypmania.alarm.AlarmService;
import nl.ypmania.fs20.FS20Decoder;
import nl.ypmania.fs20.FS20Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NodeService {
  private final Logger log = LoggerFactory.getLogger(NodeService.class);
  
  private static final int OOK_TYPE = 1;
  private static final int RF12_TYPE = 2;
  
  private InputStream in;
  private OutputStream out;
  private boolean running = true;
  private Reader reader;
  
  @Autowired private AlarmService alarmService;
  @Autowired private FS20Service fs20Service;
  
  public void start(InputStream in, OutputStream out) {
    this.in = in;
    this.out = out;
    this.reader = new Reader();
    reader.start();
  }
  
  public void stop() {
    log.debug("Stopping");
    running = false;
    try {
      in.close();
    } catch (IOException e) {}
    reader.interrupt();
    log.debug("Stopped.");
  }
  
  public synchronized void sendFS20 (nl.ypmania.fs20.Packet fs20Packet) {
    try {
      out.write(6); // length
      out.write(OOK_TYPE);
      // TODO write durations as 4ms-steps
    } catch (IOException e) {
      throw new RuntimeException (e);
    }    
  }
  
  private void handle(int type, int[] packet) {
    StringBuilder s = new StringBuilder();
    s.append("Packet, size ");
    s.append(packet.length);
    s.append(", type ");
    s.append(type);
    s.append(": ");
    String separator = "";
    for (int i: packet) {
      s.append(separator);
      s.append(i);
      separator = ",";
    }
    log.info ("Received {}", s);
    switch (type) {
    case OOK_TYPE:      
      fs20Service.handle(new FS20Decoder().decode(packet, 4));
      alarmService.handle(new AlarmDecoder().decode(packet, 4));
      break;
    case RF12_TYPE:
      
      break;
    }
  }
  
  private class Reader extends Thread {
    public Reader() {
      setDaemon(true);
    }
    @Override
    public void run() {
      do {
        try {
          int length = in.read();
          int type = in.read();
          int[] packet = new int[length];
          for (int i = 0; i < length; i++) {
            packet[i] = in.read();
          }
          handle (type, packet);
        } catch (IOException e) {
          log.warn("Error reading", e);
        }
      } while (running);
    }
  }
}
