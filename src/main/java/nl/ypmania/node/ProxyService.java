package nl.ypmania.node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import nl.ypmania.env.StatsD;
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

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.github.os72.protobuf.dynamic.MessageDefinition;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;

@Component
public class ProxyService {
  private static final Logger log = LoggerFactory.getLogger(ProxyService.class);
  
  private static final MessageDefinition msgDef = MessageDefinition.newBuilder("Packet")
      .addField("optional", "bytes", "mac", 1)
      .addField("optional", "uint32", "seq", 2)
      .addField("optional", "uint32", "packets_out", 3)
      .addField("optional", "uint32", "packets_in", 4)
      .addField("optional", "uint32", "rfm_watchdog", 5)
      .addField("optional", "uint32", "esp_watchdog", 6).build();

  private static DynamicSchema schema() {
      DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();
      schemaBuilder.setName("ProxyPing.proto");
      schemaBuilder.addMessageDefinition(msgDef);
      try {
          return schemaBuilder.build();
      } catch (DescriptorValidationException e) {
          throw new RuntimeException(e);
      }
  }

  private static final DynamicSchema schema = schema();

  private static final Descriptor msgDesc = schema.getMessageDescriptor("Packet");
  
  private final FS20Service fs20Service;
  private final RF12Service rf12Service;
  private final VisonicService visonicService;
  private final StatsD statsd;
  private final Server server;
  private final Server server2;
  private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("ProxyService-worker-%d").build());
  private final Map<String,InetSocketAddress> proxies = new HashMap<>();
  private final Map<InetSocketAddress,TimerTask> timeouts = new HashMap<>();
  private final Timer timer = new Timer("ProxyService-timer");

  @Autowired
  public ProxyService(FS20Service fs20Service, RF12Service rf12Service, VisonicService visonicService, StatsD statsd) {
    this.fs20Service = fs20Service;
    this.rf12Service = rf12Service;
    this.visonicService = visonicService;
    this.statsd = statsd;
    String port = System.getProperty("ProxyService.port");
    int p;
    try {
      p = Integer.parseInt(port.trim());
    } catch (Exception x) {
      p = 4123;
    }
    this.server = new Server(p);
    this.server2 = new Server(4124);
  }
  
  @PostConstruct
  public void start() {
    this.server.start();
    this.server2.start();
  }
  
  @PreDestroy
  public void shutdown() {
    this.server.shutdown();
    this.server2.shutdown();
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
        server.send(udp);
        server.send(udp);
        server.send(udp);
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
        server.send(udp);
        server.send(udp);
        server.send(udp);
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
    private final DatagramSocket socket;

    public Server(int port) {
      super("ProxyService-server-" + port);
      try {
        this.socket = new DatagramSocket(port);
      } catch (SocketException e) {
        throw new RuntimeException(e);
      }      
    }
    
    public void send(DatagramPacket udp) throws IOException {
      socket.send(udp);
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
      try { this.socket.close(); } catch (Exception x) { x.printStackTrace(); }
      interrupt();
    }
  }

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
        log.debug("Received ping from {} which has ID {}: {}", 
            new Object[] { packet.getSocketAddress(), id, new String(packet.getData(), 0, packet.getLength()) });
        if (id.charAt(2) != ':' || id.charAt(5) != ':' || id.charAt(8) != ':' || id.charAt(11) != ':' || id.charAt(14) != ':') {
          log.warn("Invalid ping from {}: {}", packet.getSocketAddress(), id);
          return;
        }
        addProxy((InetSocketAddress) packet.getSocketAddress(), id);
      }
    break;
      
    case 'Q':
      receivePing(packet);
    }      
  }

  private void addProxy(InetSocketAddress address, String id) {
    synchronized(this) {
      if (proxies.put(id, address) == null) {
        log.info("Joined {} at {}.", new Object[] { id, address });                      
      }
    }
    StringBuilder s = new StringBuilder();
    String sep = "";
    for (Map.Entry<String, InetSocketAddress> entry: proxies.entrySet()) {
      s.append(sep);
      s.append(entry.getKey());
      s.append(" -> ");
      s.append(entry.getValue());
      sep = ", ";
    }
    log.info("Known proxies: {}", s);
  }

  private void receivePing(DatagramPacket packet) {
    byte[] protobuf = new byte[packet.getLength() - 1];
    System.arraycopy(packet.getData(), 1, protobuf, 0, protobuf.length);
    try {
      DynamicMessage message = DynamicMessage.parseFrom(msgDesc, protobuf);
      receivePing(message, (InetSocketAddress) packet.getSocketAddress());
    } catch (InvalidProtocolBufferException e) {
      log.warn("Invalid ping packet: {}", Arrays.toString(protobuf));
    }
  }

  private void receivePing(DynamicMessage message, InetSocketAddress address) {
    ByteString mac = (ByteString) message.getField(msgDesc.findFieldByName("mac"));
    if (mac.size() != 6) {
      log.warn("Illegal mac: {}", mac);
      return;
    }
    String id = String.format("%02X:%02X:%02X:%02X:%02X:%02X", mac.byteAt(0), mac.byteAt(1), mac.byteAt(2), mac.byteAt(3), mac.byteAt(4), mac.byteAt(5));
    addProxy(address, id);
    int seq = (Integer) message.getField(msgDesc.findFieldByName("seq"));
    int packets_in = (Integer) message.getField(msgDesc.findFieldByName("packets_in"));
    int packets_out = (Integer) message.getField(msgDesc.findFieldByName("packets_out"));
    int rfm_watchdog = (Integer) message.getField(msgDesc.findFieldByName("rfm_watchdog"));
    int esp_watchdog = (Integer) message.getField(msgDesc.findFieldByName("esp_watchdog"));
    log.debug("Received ping from {}, seq {}, in {}, out {}, rfm {}, esp {}", new Object[] {
        id, seq, packets_in, packets_out, rfm_watchdog, esp_watchdog });
    String statsName = "proxy." + id.replace(':', '_');
    if (message.hasField(msgDesc.findFieldByName("packets_in"))) {
      statsd.count(statsName + ".packets_in", packets_in);
    }
    if (message.hasField(msgDesc.findFieldByName("packets_out"))) {
      statsd.count(statsName + ".packets_out", packets_out);
    }
    if (message.hasField(msgDesc.findFieldByName("rfm_watchdog"))) {
      statsd.count(statsName + ".rfm_watchdog", rfm_watchdog);
    }
    if (message.hasField(msgDesc.findFieldByName("esp_watchdog"))) {
      statsd.count(statsName + ".esp_watchdog", esp_watchdog);
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