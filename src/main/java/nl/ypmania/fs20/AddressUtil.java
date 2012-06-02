package nl.ypmania.fs20;

public class AddressUtil {
  public static int fromFS20(int i) {
    int result = 0;
    result += ((i / 1000) - 1) * 64;
    i %= 1000;
    result += ((i / 100) - 1) * 16;
    i %= 100;
    result += ((i / 10) - 1) * 4;
    i %= 10;
    result += (i - 1);
    return result;
  }
  
  public static int toFS20(int i) {
    int result = 0;
    result += ((i & 3) + 1);
    result += (((i >> 2) & 3) + 1) * 10;
    result += (((i >> 4) & 3) + 1) * 100;
    result += (((i >> 6) & 3) + 1) * 1000;
    return result;
}

}
