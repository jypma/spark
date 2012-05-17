package nl.ypmania;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class HelloBean {
  private String me = "foo";
  
  public void setMe(String me) {
    this.me = me;
  }
  
  public String getMe() {
    return me;
  }
}
