/*
 * Created on 9/12/2004
 */
package unsw.cse.mica.demo.audio;

import javax.sound.sampled.AudioFormat;

/**
 * A class containing some utility functions for other Audio classes.
 * @author jhw
 */
public class AudioUtils {
	
	public static final AudioFormat DEFAULT_FORMAT = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 22050, 16, 1, 2, 22050, false);
	
	//private static final void debug(String msg) {
	//	System.err.println("" + new java.sql.Timestamp(System.currentTimeMillis()).toString() + " Clip: " + msg);
	//}
	
	/**
	 * Generates a constant mono, 22050Hz, 16-bits signed PCM signal with
	 * the given duration and frequency. The actual length may be slightly
	 * different in order to get an integral number of samples.
	 * 
	 * @param msecs the desired duration in milliseconds
	 * @param frequency the desired frequency
	 */
	public static byte[] generate(long msecs, double frequency) {
		double rate = DEFAULT_FORMAT.getSampleRate();
		//int bits = DEFAULT_FORMAT.getSampleSizeInBits();
		//int channels = DEFAULT_FORMAT.getChannels();
		
		// the old values:
		double length = msecs / 1000.0;
		double numSamples = rate * length;
		double numSins = length * frequency;
		
		/*
		debug("before: ");
		debug("  length: " + length);
		debug("  numSamples: " + numSamples);
		debug("  numSins: " + numSins);
		debug("  frequency: " + frequency);
		*/	 
		numSamples = Math.floor((rate * length) + 0.5);
		length = numSamples / rate;
		numSins = Math.floor((length * frequency) + 0.5);
		frequency = numSins / length; 
		
		/*
		debug("after: ");
		debug("  length: " + length);
		debug("  numSamples: " + numSamples);
		debug("  numSins: " + numSins);
		debug("  frequency: " + frequency);
		*/
		
		// *2 b/c 16-bit sound
		byte[] data = new byte[(int) (numSamples*2)]; 
		
		//double drate = (double) rate, sin;
		double sin;
		int sample;
		
		int b = 0;
		double scale = frequency * Math.PI * 2 / rate;
		for(int s = 0; s < numSamples; s++,b+=2) {
			sin = Math.sin(scale * s);
			sample = (int) (sin * 32767);
			data[b] = new Integer(sample).byteValue(); 
			data[b+1] = new Integer(sample >> 8).byteValue();
		}
		return data;
	}
}
