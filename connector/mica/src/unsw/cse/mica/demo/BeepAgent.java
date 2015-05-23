/*
 * Created on 13/12/2004
 */
package unsw.cse.mica.demo;

import java.util.ArrayList;
import java.util.Collections;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import unsw.cse.mica.MicaException;
import unsw.cse.mica.agent.DefaultAgent2;
import unsw.cse.mica.data.Mob;
import unsw.cse.mica.demo.audio.AudioClip;
import unsw.cse.mica.demo.audio.AudioUtils;
import unsw.cse.mica.util.Debug;
import unsw.cse.mica.util.MicaProperties;

/**
 * A very simple agent for emitting beep sounds. It uses no threading, which means
 * it has no ability to multiple sounds at once. Also, it seems to become flaky
 * once the system is under load, so it is only useful as a basic example. 
 *  
 * @author jhw
 */
public class BeepAgent extends DefaultAgent2 {
	
	public static final String TYPE_BEEP = "beep";
	public static final String SLOT_DURATION = "duration";
	public static final String SLOT_FREQUENCY = "frequency";
	
	class Beep implements Comparable {
		int duration;
		double frequency;
		AudioClip clip;
		
		public Beep(int duration, double frequency) {
			this.duration = duration;
			this.frequency = frequency;
		}
		
		public void generate() {
			clip = new AudioClip();
			clip.generate(duration, frequency);
		}
		
		public int compareTo(Object o) {
			Beep beep = (Beep) o;
			if(frequency < beep.frequency) {
				return -1;
			} else if (frequency > beep.frequency) {
				return 1;
			} else if (duration < beep.duration) {
				return -1;
			} else if (duration > beep.duration) {
				return 1;
			} else {
				return 0;
			}
		}
		
		public String toString() {
			return "" + duration + "/" + frequency + "/" + clip;
		}
	}
	
	private SourceDataLine line;
	private ArrayList beeps;

	public void init(MicaProperties args) throws MicaException {
		args.add(PARAM_NAME, "BeepAgent");
		super.init(args);
		openLine();
		at.register(TYPE_BEEP);
	}
	
	protected void openLine() {
		beeps = new ArrayList();
		try {
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, AudioUtils.DEFAULT_FORMAT);
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(AudioUtils.DEFAULT_FORMAT);
			line.start();
		} catch (Exception e) {
			// NOTE: RuntimeException(e) replaced by below for compatibility with Java 1.3 and J2ME
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public synchronized void handleNewMob(Mob m) {
		Debug.dp(Debug.INFORMATION, "handling mob: " + m);
		int duration = m.getSlot1AsInt(SLOT_DURATION);
		double frequency = m.getSlot1AsDouble(SLOT_FREQUENCY);
		Beep beep = new Beep(duration, frequency);
		int index = Collections.binarySearch(beeps, beep);
		if(index >= 0) {
			beep = (Beep) beeps.get(index);
		} else {
			beep.generate();
			beeps.add(-index-1, beep);
		}
		Debug.dp(Debug.INFORMATION, "playing beep " + beep);
		beep.clip.play(line, true);
		Debug.dp(Debug.INFORMATION, "played beep");
	}
	
	/*public static void main(String[] arg) throws Exception {
		BeepAgent agent = new BeepAgent();
		agent.openLine();
		Mob mob = new Mob(TYPE_BEEP);
		mob.addSlot(SLOT_DURATION, "500");
		mob.addSlot(SLOT_FREQUENCY, "500");
		agent.handleNewMob(mob);
	}*/
	
}
