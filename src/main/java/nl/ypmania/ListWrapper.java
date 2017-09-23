package nl.ypmania;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import nl.ypmania.fs20.Dimmer;
import nl.ypmania.fs20.FS20Switch;
import nl.ypmania.rf12.RFSwitch.Channel;

@XmlRootElement(name="List")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({Dimmer.class, FS20Switch.class, Channel.class})
public class ListWrapper<T> {
  public static <T> ListWrapper<T> wrap (List<T> items) {
    return new ListWrapper<T>(items);
  }
  
  private List<T> items = new ArrayList<T>();
  
  protected ListWrapper() {}
  
  public ListWrapper (List<T> items) {
    this.items = items;
  }
  
  public List<T> getItems() {
    return items;
  }
}
