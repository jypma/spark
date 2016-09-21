package nl.ypmania.node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import nl.ypmania.env.Zone;
import nl.ypmania.fs20.FS20Packet;
import nl.ypmania.fs20.FS20Service;
import nl.ypmania.rf12.RF12Packet;
import nl.ypmania.rf12.RF12Service;
import nl.ypmania.visonic.VisonicPacket;
import nl.ypmania.visonic.VisonicService;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

@Component
public class ProxyService {
  private static final Logger log = LoggerFactory.getLogger(ProxyService.class);
  
  private final FS20Service fs20Service;
  private final RF12Service rf12Service;
  private final VisonicService visonicService;
  private final Server server;
  private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("ProxyService-worker-%d").build());
  private final Map<String,InetSocketAddress> proxies = new HashMap<>();
  private final Map<InetSocketAddress,TimerTask> timeouts = new HashMap<>();
  private final Timer timer = new Timer("ProxyService-timer");
  private final DatagramSocket socket;

  @Autowired
  public ProxyService(FS20Service fs20Service, RF12Service rf12Service, VisonicService visonicService) {
    this.fs20Service = fs20Service;
    this.rf12Service = rf12Service;
    this.visonicService = visonicService;
    String port = System.getProperty("ProxyService.port");
    int p;
    try {
      p = Integer.parseInt(port.trim());
    } catch (Exception x) {
      p = 4123;
    }
    try {
      this.socket = new DatagramSocket(p);
    } catch (SocketException e) {
      throw new RuntimeException(e);
    }
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
  
  public boolean trySend(String proxyId, FS20Packet packet) {
    InetSocketAddress addr;
    synchronized(this) {
      addr = proxies.get(proxyId);
    }
    if (addr == null) {
      log.warn("Trying to send to unknown proxy {}.", proxyId);
      return false;
    } else {
      byte[] content = new byte[6];
      content[0] = 'F';
      int[] ints = packet.toBytes();
      for (int i = 0; i < ints.length; i++) {
        content[i+1] = (byte) (ints[i] & 0xFF);
      }
      DatagramPacket udp = new DatagramPacket(content, content.length, addr.getAddress(), addr.getPort());
      try {
        socket.send(udp);
        socket.send(udp);
        socket.send(udp);
        log.info("Sent {} to proxy {}", packet, proxyId);
        return true;
      } catch (IOException x) {
        log.warn("Can't reach proxy {}: {}", proxyId, x);
        return false;
      }
    }
  }
  
  public boolean trySend(String proxyId, RF12Packet packet) {
    InetSocketAddress addr;
    synchronized(this) {
      addr = proxies.get(proxyId);
    }
    if (addr == null) {
      log.warn("Trying to send to unknown proxy {}.", proxyId);
      return false;
    } else {
      List<Integer> ints = packet.getContents();
      byte[] content = new byte[ints.size() + 2];
      content[0] = 'R';
      content[1] = (byte) (packet.getHeader() & 0xFF); // header. SparkNode/main.cpp says 0.
      for (int i = 0; i < ints.size(); i++) {
        content[i+2] = (byte) (ints.get(i) & 0xFF);
      }
      DatagramPacket udp = new DatagramPacket(content, content.length, addr.getAddress(), addr.getPort());
      try {
        socket.send(udp);
        socket.send(udp);
        socket.send(udp);
        log.info("Sent {} to proxy {}", packet, proxyId);
        return true;
      } catch (IOException x) {
        log.warn("Can't reach proxy {}: {}", proxyId, x);
        return false;
      }
    }
  }
  
  private class Server extends Thread {
    private volatile boolean running = true;

    public Server() {
      super("ProxyService-server");
    }
    
    public void run() {
      while(running) {
        try {
          while(running) {
            byte[] data = new byte[1800];
            final DatagramPacket packet = new DatagramPacket(data, data.length);
            socket.receive(packet);
            log.debug("Received a packet from {} with length {}", packet.getSocketAddress(), packet.getLength());
            executor.execute(new Runnable() {
              public void run() {
                processReceived(packet);
              }
            });
          }
        } catch (Exception e) {
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

  @SuppressWarnings("unchecked")
  private void processReceived(DatagramPacket packet) {
    if (packet.getLength() == 0) return;
    resetTimeout((InetSocketAddress) packet.getSocketAddress());
    switch(packet.getData()[0]) {
    case 'F':
      if (packet.getLength() >= 6) {
        FS20Packet pkt = FS20Packet.fromBytes(toFS20(packet));
        log.info("Received {} from {}", pkt, packet.getAddress());
        fs20Service.handle(pkt);
      }
      break;
      
    case 'R':
      {
        RF12Packet pkt = toRF12(packet);
        log.info("Received {} from {}", pkt, packet.getAddress());
        rf12Service.handle(pkt);
      }
      break;
      
    case 'V':
      if (packet.getLength() >= 6) {
        VisonicPacket pkt = toVisonic(packet);
        log.info("Received {} from {}", pkt, packet.getAddress());
        visonicService.handle(pkt);
      }
    break;
    
    case 'P':
      if (packet.getData().length >= 18) {
        String id = new String(packet.getData(), 1, 17);
        log.debug("Received ping from {} which has ID {}", packet.getSocketAddress(), id);
        synchronized(this) {
          if (proxies.put(id, (InetSocketAddress) packet.getSocketAddress()) == null) {
            log.info("Joined {} at {}. Known: {}", new Object[] { id, packet.getSocketAddress(), StringUtils.join(proxies.keySet()) });                      
          }
        }
        log.info("Known proxies: {}", StringUtils.join(proxies.keySet()));
      }
    }      
  }

  private void resetTimeout(final InetSocketAddress socketAddress) {
    TimerTask existing = timeouts.get(socketAddress);
    if (existing != null) {
      existing.cancel();
    }
    
    timeouts.put(socketAddress, new TimerTask() {
      @SuppressWarnings("unchecked")
      @Override
      public void run() {
        synchronized(ProxyService.this) {
          proxies.values().remove(socketAddress);
          log.info("Dropped {}. Known: {}", socketAddress, StringUtils.join(proxies.keySet()));          
        }
      }
    });
    timer.schedule(timeouts.get(socketAddress), 120000); // ping every 30sec, so go away after 4 missed pings
  }

  private VisonicPacket toVisonic(DatagramPacket packet) {
    return new VisonicPacket(
        packet.getData()[1] & 0xFF, 
        packet.getData()[2] & 0xFF, 
        packet.getData()[3] & 0xFF, 
        packet.getData()[4] & 0xFF, 
        packet.getData()[5] & 0xFF);
  }

  private RF12Packet toRF12(DatagramPacket packet) {
    int[] bytes = new int[packet.getLength() - 2];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = packet.getData()[i + 2] & 0xFF;
    }
    return new RF12Packet(packet.getData()[1], bytes);
  }

  private int[] toFS20(DatagramPacket packet) {
    int[] bytes = new int[5];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = packet.getData()[i + 1] & 0xFF;
    }
    return bytes;
  }

  public List<String> selectProxies(Zone zone) {
    if (zone == null) {
      return getKnownProxies();
    } else if (!zone.getPreferredProxies().iterator().hasNext()) {
      return selectProxies(zone.getParent());
    } else {
      List<String> candidates = getKnownProxies();
      List<String> result = new ArrayList<String>();
      for (String id: zone.getPreferredProxies()) {
        if (candidates.contains(id)) {
          result.add(id);
          candidates.remove(id);
        }
      }
      result.addAll(candidates);
      return result;
    }
  }

  private synchronized List<String> getKnownProxies() {
    List<String> l = new ArrayList<String>();
    l.addAll(proxies.keySet());
    return l;
  }
}