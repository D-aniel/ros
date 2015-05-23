/*
 * Created on 9/12/2004
 */
package unsw.cse.mica.demo.timeteller;

import java.util.Calendar;
import java.util.Date;

/**
 * @author jhw
 */
public class TimeTellerUtils {

	/**
	 * We could probably try doing this useing a Date format but I don't know if we'd be able to get the
	 * "precisely" word in there...
	 * @param cal
	 * @return
	 */
	public static String getTimeString(long timestamp) {
		Calendar cal = Calendar.getInstance();
	    cal.setTime(new Date(timestamp));	// Replaces cal.setTimeInMillis(timestamp) for J2ME compatibility.
		return getTimeString(cal);
	}
	
	public static String getTimeString(Calendar cal) {
		int h = cal.get(Calendar.HOUR);
		if (h == 0)
			h = 12;
		int m = cal.get(Calendar.MINUTE);
		int s = cal.get(Calendar.SECOND);
		
		String str = "" + h;
		if (m == 0) {
			str += " o'clock";
		} else if (m < 10) {
			str += " 0 " + m;
		} else {
			str += " " + m;
		}
		if (s > 0) {
			str += " and " + s + " seconds";
		} else {
			str += " precisely";
		}
		return str;
	}
}
