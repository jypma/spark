package nl.ypmania.node;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.util.Enumeration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SerialPortService {
  private Logger log = LoggerFactory.getLogger(SerialPortService.class);
  
  @Value ("${node.serialPort}")
  private String serialPort;
  
  @Autowired private NodeService node;
  
  private SerialPort port;

  public void setSerialPort(String serialPort) {
    this.serialPort = serialPort;
  }
  
  @PostConstruct
  public void start() {
    try {
      port = (SerialPort) findPort().open("NodeService", 2000);
      port.setSerialPortParams(57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
      node.start(port);
    } catch (PortInUseException e) {
      throw new RuntimeException (e);
    } catch (UnsupportedCommOperationException e) {
      throw new RuntimeException (e);
    }
  }
  
  private CommPortIdentifier findPort() {
    StringBuilder ports = new StringBuilder();
    Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();
    while (portList.hasMoreElements()) {
      CommPortIdentifier port = (CommPortIdentifier) portList.nextElement();
      ports.append(port.getName());
      ports.append("/");
      ports.append(port.getPortType());
      ports.append(" ");
      if (port.getPortType() == CommPortIdentifier.PORT_SERIAL &&
          port.getName().equals(serialPort)) return port;
    }
    throw new RuntimeException ("Port " + serialPort + " not found. Available ports: " + ports);
  }

  @PreDestroy
  public void stop() {
    if (node != null) {
      node.stop();
    }
    log.debug ("Closing " + port);
    if (port != null) {
      port.close();
    }
    log.debug("Closed.");
  }
}
