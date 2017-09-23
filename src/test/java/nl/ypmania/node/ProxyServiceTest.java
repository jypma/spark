package nl.ypmania.node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import nl.ypmania.rf12.RF12Packet;

public class ProxyServiceTest {
  DatagramSocket socket;
  
  @Before
  public void setup() throws SocketException {
     socket = new DatagramSocket(4123);
  }
  
  @After
  public void stop() {
    socket.close();
  }
  
  public void send() throws IOException {
    RF12Packet packet = new RF12Packet(42, new int[] { 0,0,0 });
    List<Integer> ints = packet.getContents();
    byte[] content = new byte[ints.size() + 2];
    content[0] = 'R';
    content[1] = (byte) (packet.getHeader() & 0xFF); // header. SparkNode/main.cpp says 0.
    for (int i = 0; i < ints.size(); i++) {
      content[i+2] = (byte) (ints.get(i) & 0xFF);
    }
    InetSocketAddress addr = new InetSocketAddress("192.168.0.175", 4123);
    DatagramPacket udp = new DatagramPacket(content, content.length, addr.getAddress(), addr.getPort());
    socket.send(udp);
  }
  
  //@Test
  public void go() throws IOException, InterruptedException {
    while(true) {
      send();
      Thread.sleep(500);
    }
  }
}
