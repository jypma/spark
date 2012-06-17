package nl.ypmania.node;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TooManyListenersException;

import nl.ypmania.alarm.AlarmDecoder;
import nl.ypmania.alarm.AlarmService;
import nl.ypmania.fs20.FS20Decoder;
import nl.ypmania.fs20.FS20Encoder;
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
  private static final int FS20_TYPE = 3;
  
  private InputStream in;
  private OutputStream out;
  private InState inState = new InState();
  
  @Autowired private AlarmService alarmService;
  @Autowired private FS20Service fs20Service;
  
  public void start(SerialPort port) {
    try {
      this.in = port.getInputStream();
      this.out = port.getOutputStream();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    port.notifyOnDataAvailable(true);
    try {
      port.addEventListener(new SerialPortEventListener(){
        @Override
        public void serialEvent(SerialPortEvent event) {
          try {
            //log.debug ("Got event, available: {}", in.available());
            while (in.available() > 0) 
              inState.read(in.read());
          } catch (IOException e) {
            log.warn("Error reading from serial", e);
          }
        }
      });
    } catch (TooManyListenersException e) {
      throw new RuntimeException (e);
    }
    inState.reset();
  }
  
  public void stop() {
    log.debug("Stopping");
    try {
      in.close();
      out.close();
    } catch (IOException e) {}
    log.debug("Stopped.");
  }
  
  public synchronized void sendFS20 (nl.ypmania.fs20.Packet fs20Packet) {
    try {
      log.debug ("Sending FS20: {}", fs20Packet);
      /*
      log.info ("Sending FS20: {}", fs20Packet);
      int[] pulses = new FS20Encoder(fs20Packet.toBytes()).getResult();
      log.debug ("as {} pulses: {}", pulses.length, pulses);
      
      out.write(OOK_TYPE);
      out.write(pulses.length);
      for (int p: pulses) {
        out.write (p / 4);
      }
      //out.write(4);
      //for (int i = 0; i < 4; i++) out.write(100);
      
      //out.flush();
      */
      //TODO wait for ACK
      out.write(FS20_TYPE);
      out.write(4);
      int[] bytes = fs20Packet.toBytes();
      out.write(bytes[0]);
      out.write(bytes[1]);
      out.write(bytes[2]);
      out.write(bytes[3]);
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
  
  private class InState {
    private static final int POS_TYPE = -2;
    private static final int POS_LENGTH = -1;
    
    int length;
    int type;
    int pos;
    int[] buf = new int[256];
    Timer timer = new Timer();
    TimerTask timeoutTask = null;
    
    public synchronized void reset() {
      pos = POS_TYPE;
      if (timeoutTask != null) {
        timeoutTask.cancel();
        timeoutTask = null;
      }
    }
    
    public synchronized void read (int b) {
      if (timeoutTask != null) timeoutTask.cancel();
      
      switch (pos) {
      case POS_TYPE:
        type = b;
        pos++;
        break;
      case POS_LENGTH:
        length = b;
        pos++;
        break;
      default:
        buf[pos] = b;
        pos++;
        if (pos >= length) {
          int[] packet = new int[length];
          System.arraycopy(buf, 0, packet, 0, length);
          handle (type, packet);
          reset();
          return;
        }
      }
      timeoutTask = new TimerTask(){
        public void run() {
          log.info("Timeout reading packet after {} of {} bytes.", pos, length);
          reset();
        }
      };
      timer.schedule(timeoutTask, 300);
    }
  }
}
