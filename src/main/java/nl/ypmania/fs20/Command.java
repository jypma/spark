package nl.ypmania.fs20;

import java.util.HashMap;
import java.util.Map;

public enum Command {
  OFF(0),
  DIM_1(1),
  DIM_2(2),
  DIM_3(3),
  DIM_4(4),
  DIM_5(5),
  DIM_6(6),
  DIM_7(7),
  DIM_8(8),
  DIM_9(9),
  DIM_10(10),
  DIM_11(11),
  DIM_12(12),
  DIM_13(13),
  DIM_14(14),
  DIM_15(15),
  ON_FULL(16),
  ON_PREVIOUS(17),
  TOGGLE(18),
  DIM_UP(19),
  DIM_DOWN(20),
  DIM_UP_DOWN(21),
  TIMER_SET(22),
  SEND_STATUS(23),
  TIMED_OFF(24),
  TIMED_ON_FULL(25),
  TIMED_ON_PREVIOUS(26),
  RESET(27),
  ;
  
  Command(int protocolValue) {
    this.protocolValue = protocolValue;
  }
  
  private int protocolValue;
  
  public int getProtocolValue() {
    return protocolValue;
  }
  
  private static Map<Integer,Command> commandByProtocolValue = new HashMap<Integer,Command>();
  
  static {
    for (Command c: Command.values()) {
      commandByProtocolValue.put(c.protocolValue, c);
    }
  }
  
  public static Command byProtocolValue (int command) {
    return commandByProtocolValue.get(command);
  }
}
