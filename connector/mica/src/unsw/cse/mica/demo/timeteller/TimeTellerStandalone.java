/*
 * Created on 9/12/2004
 */
package unsw.cse.mica.demo.timeteller;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

/**
 * In order to keep Mica from being dependent on JSAPI we leaver this commented out,
 * but we'll leave the code there so it can be uncommented if desired to provide a more
 * meaningful demo...
 */
/*
import javax.speech.Central;
import javax.speech.EngineList;
import javax.speech.EngineModeDesc;
import javax.speech.synthesis.Synthesizer;
*/
import unsw.cse.mica.demo.audio.AudioClip;
import unsw.cse.mica.tools.timer.ITimerHandler;
import unsw.cse.mica.tools.timer.Timer;
import unsw.cse.mica.tools.timer.TimerEvent;

/**
 * A standalone version of the TimeTeller. It users its own Timer and 
 * AudioClips for timing and beeps and creates a String prompt.
 * Code is present in the class demonstrating a simple way
 * to speak the prompts, using JSAPI, however, MICA needs to remain free from any
 * JSAPI dependency so it is commented out in the distributed version.
 * For an agent-based version see <tt>TimeTellerAgent</tt>.
 * 
 * @author jhw
 */
public class TimeTellerStandalone implements ITimerHandler, ITimeTellerConstants {
	

	private Timer timer;
	private SourceDataLine line;
	private AudioClip[] clips;
	//private Synthesizer synthesizer;
	
	public TimeTellerStandalone() {
		
	}
	
	public void init() throws Exception {
		debug("Initialising clips");
		clips = new AudioClip[3];
		clips[0] = new AudioClip();
		clips[0].generate(BEEP_LENGTH, BEEP1_FREQUENCY);
		clips[1] = new AudioClip();
		clips[1].generate(BEEP_LENGTH, BEEP2_FREQUENCY);
		clips[2] = new AudioClip();
		clips[2].generate(BEEP_LENGTH, BEEP3_FREQUENCY);
		debug("Initialised clips");
		
		debug("Initialising line");
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, clips[0].getFormat());
		line = (SourceDataLine) AudioSystem.getLine(info);
		line.open(clips[0].getFormat());
		line.start();
		debug("Initialised line");
		
		// init the synthesizer
		/*
		debug("Initialising synthesizer");
		EngineList list = Central.availableSynthesizers(null);
		synthesizer = Central.createSynthesizer((EngineModeDesc) list.get(0));
		synthesizer.allocate();
		debug("Initialised synthesizer");
		*/
		
		// init the timer...
		debug("Initialising timer");
		timer = new Timer(this);
		timer.init();
		long now = System.currentTimeMillis();
		long time = now - (now % 10000);
		if(time < now) {
			time += 10000;
		}
		timer.addEvent(new TimerEvent(TYPE_SAY,   TYPE_SAY,   time + SAY_OFFSET, 10000));  
		timer.addEvent(new TimerEvent(TYPE_BEEP1, TYPE_BEEP1, time + BEEP1_OFFSET, 10000));
		timer.addEvent(new TimerEvent(TYPE_BEEP2, TYPE_BEEP2, time + BEEP2_OFFSET, 10000));
		timer.addEvent(new TimerEvent(TYPE_BEEP3, TYPE_BEEP3, time + BEEP3_OFFSET, 10000));
		debug("Initialised timer");
	}
	
	private static final void debug(String msg) {
		System.err.println(new java.sql.Timestamp(System.currentTimeMillis()).toString() + ": " + msg);
	}
	
	public void terminate() throws Exception {
		debug("Terminating timer");
		timer.terminate();
		/*
		debug("Terminating synthesizer");
		synthesizer.cancelAll();
		synthesizer.deallocate();
		*/
		debug("Terminating player");
		line.stop();
		line.close();
		debug("Terminate complete");
	}
	
	public void fire(TimerEvent event) {
		//String str;
		if(event.getType() == TYPE_SAY) {
			say(event.getTime());
		} else if (event.getType() == TYPE_BEEP1) {
			beep(0);
		} else if (event.getType() == TYPE_BEEP2) {
			beep(1);
		} else if (event.getType() == TYPE_BEEP3) {
			beep(2);
		}
	}
	
	private void say(long timestamp) {
		String str = PROMPT + TimeTellerUtils.getTimeString(timestamp + 10000 - SAY_OFFSET);
		System.out.println(str);
		//synthesizer.speakPlainText(str, null);
	}
	
	private void beep(int type) {	
		clips[type].play(line, false);
	}
	
	/**
	 * Test harness. simply run it and begin hearing the time. The system will exits
	 * when a key is pressed.
	 *  
	 * @param arg
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		TimeTellerStandalone tt = new TimeTellerStandalone();
		tt.init();
		System.in.read();
		tt.terminate();
	}
}
