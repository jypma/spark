package nl.ypmania.node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import nl.ypmania.fs20.FS20Packet;
import nl.ypmania.fs20.FS20Service;
import nl.ypmania.rf12.RF12Packet;
import nl.ypmania.rf12.RF12Service;
import nl.ypmania.visonic.VisonicPacket;
import nl.ypmania.visonic.VisonicService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProxyService {
  private static final Logger log = LoggerFactory.getLogger(ProxyService.class);
  
  private final FS20Service fs20Service;
  private final RF12Service rf12Service;
  private final VisonicService visonicService;
  private final Server server;

  @Autowired
  public ProxyService(FS20Service fs20Service, RF12Service rf12Service, VisonicService visonicService) {
    this.fs20Service = fs20Service;
    this.rf12Service = rf12Service;
    this.visonicService = visonicService;
    this.server = new Server();
  }
  
  @PostConstruct
  public void start() {
    this.server.start();
  }
  
  @PreDestroy
  public void shutdown() {
    this.server.shutdown();
  }
  
  private class Server extends Thread {
    private volatile boolean running = true;

    public Server() {
      super("ProxyService-server");
    }
    
    public void run() {
      byte[] data = new byte[1800];
      while(running) {
        try(DatagramSocket socket = new DatagramSocket(4123)) {
          while(running) {
            DatagramPacket packet = new DatagramPacket(data, data.length);
            socket.receive(packet);
            processReceived(packet);
          }
        } catch (IOException e) {
          log.error("Error in server, restarting", e);
          try {
            if (running) sleep(5000);
          } catch (InterruptedException e1) {}
        }
      }
    }
    
    public void shutdown() {
      running  = false;
      interrupt();
    }
  }

  private void processReceived(DatagramPacket packet) {
    log.debug("Received a packet from {} with length {}", packet.getSocketAddress(), packet.getLength());
    if (packet.getLength() == 0) return;
    switch(packet.getData()[0]) {
    case 'F': 
      if (packet.getLength() >= 6) {
        log.info("Received FS20 from {}", packet.getAddress());
        fs20Service.handle(FS20Packet.fromBytes(toFS20(packet)));
      }
      break;
      
    case 'R':
      log.info("Received RF12 from {}", packet.getAddress());
      rf12Service.handle(new RF12Packet(toRF12(packet)));
      break;
      
    case 'V':
      if (packet.getLength() >= 6) {
        log.info("Received Visonic from {}", packet.getAddress());
        visonicService.handle(toVisonic(packet));
      }
    break;
    
    case 'P':
      log.debug("Received ping from {}", packet.getSocketAddress());
    }      
  }

  private VisonicPacket toVisonic(DatagramPacket packet) {
    return new VisonicPacket(
        packet.getData()[1] & 0xFF, 
        packet.getData()[2] & 0xFF, 
        packet.getData()[3] & 0xFF, 
        packet.getData()[4] & 0xFF, 
        packet.getData()[5] & 0xFF);
  }

  private int[] toRF12(DatagramPacket packet) {
    int[] bytes = new int[packet.getLength() - 1];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = packet.getData()[i + 1] & 0xFF;
    }
    return bytes;
  }

  private int[] toFS20(DatagramPacket packet) {
    int[] bytes = new int[5];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = packet.getData()[i + 1] & 0xFF;
    }
    return bytes;
  }
}