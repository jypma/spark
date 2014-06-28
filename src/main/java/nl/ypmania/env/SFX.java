package nl.ypmania.env;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SFX {
  private static final Logger log = LoggerFactory.getLogger(SFX.class);
  
  private List<Server> remoteServers = new ArrayList<Server>();
  private Clip clip;
  private String lastPlayed;
  private long lastTime;
  
  public SFX() {
    String sfxServers = System.getProperty("SFX.servers");
    if (sfxServers != null) {
      for (String s: sfxServers.trim().split(",")) {
        try {
          remoteServers.add(new Server(s));
          log.info("Added remote server " + s);
        } catch (RuntimeException x) {
          log.warn ("Couldn't parse server " + s + ": " + x);
        }
      }
    }
  }
  
  
  @PreDestroy
  public synchronized void close() {
    if (clip != null) {
      clip.close();
      clip = null;
    }
  }
  
  public synchronized void play (String resource) {
    for (Server server: remoteServers) {
      server.play(resource);
    }
    if (clip != null && clip.isActive()) {
      clip.stop();
      clip.flush();
    } else try {
      clip = AudioSystem.getClip();
      clip.addLineListener(new LineListener() {
        public void update(LineEvent event) {
          if (event.getType() == LineEvent.Type.STOP) close();
        }
      });
    } catch (LineUnavailableException e) {
      log.warn ("No audio system available. Disabling audio.");
      return;
    }      
    if (lastPlayed != null) {
      if (resource.equals(lastPlayed) && System.currentTimeMillis() < lastTime + 3000) return;
    }
    lastPlayed = resource;
    lastTime = System.currentTimeMillis();
    URL url = getClass().getResource("/" + resource);
    if (url == null) {
      log.warn ("Can't find resource {}", resource);
      return;
    }
    try {
      clip.open(AudioSystem.getAudioInputStream(url));
      clip.start(); 
    } catch (LineUnavailableException e) {
      log.warn("Line unavailable, disabling playback of " + resource, e);
      close();
    } catch (IOException e) {
      log.warn("Can't open file, disabling playback of " + resource, e);
      close();
    } catch (UnsupportedAudioFileException e) {
      log.warn("Can't play file, disabling playback of " + resource, e);
      close();
    }
  }
  
  private class Server {
    private final String host;
    private final int port;
    
    public Server (String desc) {
      String[] comps = desc.trim().split(":");
      if (comps.length != 2) throw new IllegalArgumentException ("Server must be address:host");
      this.host = comps[0];
      this.port = Integer.parseInt(comps[1]);
    }

    public void play(String resource) {
      try (Socket s = new Socket()) {
        s.setSoTimeout(500);
        s.connect(new InetSocketAddress(host, port), 500);
        try (OutputStream out = s.getOutputStream()) {
          out.write(resource.getBytes());
          out.write("\n".getBytes());
        }
      } catch (IOException x) {
        log.warn("Could not play " + resource + " on " + host + ":" + port, x);
      }
    }
  }
}
