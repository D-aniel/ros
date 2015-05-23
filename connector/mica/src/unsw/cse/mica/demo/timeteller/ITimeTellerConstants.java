/*
 * Created on 9/12/2004
 */
package unsw.cse.mica.demo.timeteller;

/**
 * Constants used by the TimeTeller demos.
 * 
 * @author jhw
 */
public interface ITimeTellerConstants {
	
	public static final String TYPE_SAY = "say";
	public static final String TYPE_BEEP1 = "beep1";
	public static final String TYPE_BEEP2 = "beep2";
	public static final String TYPE_BEEP3 = "beep3";
	
	public static final int SAY_OFFSET   = 2000;
	public static final int BEEP1_OFFSET = 8000;
	public static final int BEEP2_OFFSET = 9000;
	public static final int BEEP3_OFFSET = 10000;
	
	public static final int BEEP_LENGTH = 400;
	public static final int BEEP1_FREQUENCY = 400;
	public static final int BEEP2_FREQUENCY = 500;
	public static final int BEEP3_FREQUENCY = 600;
	
	public static final String PROMPT = "On the third stroke it will be ";
}
