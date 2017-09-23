package nl.ypmania.env;

public interface Switch {
  Zone getZone();
  String getName();
  void onFull();
  void off();
  boolean isOn();
}
