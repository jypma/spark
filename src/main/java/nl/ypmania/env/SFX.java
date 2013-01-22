package nl.ypmania.env;

import java.io.IOException;
import java.net.URL;

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
  private Clip clip;
  private String lastPlayed;
  private long lastTime;
  
  @PreDestroy
  public synchronized void close() {
    if (clip != null) {
      clip.close();
      clip = null;
    }
  }
  
  public synchronized void play (String resource) {
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
}
