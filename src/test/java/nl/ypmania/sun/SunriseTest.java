package nl.ypmania.sun;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Test;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

public class SunriseTest {
  @Test
  public void testSunriseWorks() {
    Location location = new Location("55.683334", "12.55");
    SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, "Europe/Amsterdam");
    DateFormat fmt = new SimpleDateFormat();
    fmt.setTimeZone(TimeZone.getTimeZone("Europe/Amsterdam"));
    
    Calendar officialSunrise = calculator.getOfficialSunriseCalendarForDate(Calendar.getInstance());
    System.out.println (fmt.format(officialSunrise.getTime()));
    Calendar officialSunset = calculator.getOfficialSunsetCalendarForDate(Calendar.getInstance());
    System.out.println (fmt.format(officialSunset.getTime()));
    
    Calendar civilSunrise = calculator.getCivilSunriseCalendarForDate(Calendar.getInstance());
    System.out.println (fmt.format(civilSunrise.getTime()));
    Calendar civilSunset = calculator.getCivilSunsetCalendarForDate(Calendar.getInstance());
    System.out.println (fmt.format(civilSunset.getTime()));
  }
  
  @Test
  public void testIceland() {
    Location location = new Location("64.135491", "-21.896149");
    SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, "Europe/Amsterdam");
    DateFormat fmt = new SimpleDateFormat();
    fmt.setTimeZone(TimeZone.getTimeZone("Europe/Amsterdam"));
    
    Calendar officialSunrise = calculator.getOfficialSunriseCalendarForDate(Calendar.getInstance());
    System.out.println (fmt.format(officialSunrise.getTime()));
    Calendar officialSunset = calculator.getOfficialSunsetCalendarForDate(Calendar.getInstance());
    System.out.println (fmt.format(officialSunset.getTime()));
  }

}
