package nl.ypmania.node;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NodeService {
  private final Logger log = LoggerFactory.getLogger(NodeService.class);
  
  private InputStream in;
  private OutputStream out;
  private boolean running = true;
  private Reader reader;
  
  public void start(InputStream in, OutputStream out) {
    this.in = in;
    this.out = out;
    this.reader = new Reader();
    reader.start();
  }
  
  public void stop() {
    running = false;
    reader.interrupt();
  }
  
  private void handle(int[] packet) {
    StringBuilder s = new StringBuilder();
    s.append("Packet, size ");
    s.append(packet.length);
    s.append(": ");
    String separator = "";
    for (int i: packet) {
      s.append(separator);
      s.append(i);
      separator = ",";
    }
    log.info ("Received {}", s);
  }
  
  private class Reader extends Thread {
    @Override
    public void run() {
      do {
        try {
          int length = in.read();
          int[] packet = new int[length];
          for (int i = 0; i < length; i++) {
            packet[i] = in.read();
          }
          handle (packet);
        } catch (IOException e) {
          log.warn("Error reading", e);
        }
      } while (running);
    }
  }
}
