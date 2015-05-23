/*
 * Created on 8/12/2004
 */
package unsw.cse.mica.demo.audio;

import java.io.File;
import java.io.IOException;
//import java.sql.Timestamp;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import unsw.cse.mica.util.Debug;

/**
 * An AudioClip combines an AudioFormat and some data, and provides the ability to
 * play it to a <tt>SourceDataLine</tt>. The clip can be read from a file or generated as a
 * constant tone with a given duration and frequency.
 * 
 * <p>Author's note: I only discovered the <tt>javax.sound.sampled.Clip</tt> class after I'd written this
 * class. It is probably more suitable than this class for the functionality here, and would allow
 * one less re-invention of the wheel. However, there doesn't appear to be
 * anyway to do paused looping, so the <tt>AudioLoop</tt> class which extends this one would still
 * need to be re-written. 
 *     
 * @author jhw
 */
public class AudioClip {
    
	private AudioFormat format;
	private byte[] data;
	private long lengthInMsecs;
	
	public AudioClip() {    
	}
	
	/**
	 * Load a sound file for use by the player.
	 * 
	 * @param filename The sound file to be loaded.
	 */
	public void loadFile(String filename) throws UnsupportedAudioFileException, IOException {
		AudioInputStream ais = AudioSystem.getAudioInputStream(new File(filename));
		  
	    format = ais.getFormat();
	    int numBytes = (int)(ais.getFrameLength()*format.getFrameSize());
	    long sampleLength = (numBytes * 8) / (format.getSampleSizeInBits() * format.getChannels());
	    lengthInMsecs = (long) (1000 * (sampleLength / format.getSampleRate()));
	    

	    // Read everything, and then make sure we got it all
	    data = new byte[numBytes];
	    int read = ais.read(data);
	    if (read != data.length) {
	    	throw new IOException("Read only " + read + " of " + data.length + " bytes from file " + filename);
	    }
	}
	
	/**
	 * Get the clip's length in msecs
	 */
	public long getLengthInMsecs() {
		return lengthInMsecs;
	}
	
	/**
	 * Get the clip's format
	 */
	public AudioFormat getFormat() {
		return format;
	}
	
	/**
	 * Put the clip's data onto the line. This method blocks for as long as it takes for
	 * the method to get the entire clip onto the line and play it to completion.
	 */
	public void play(SourceDataLine line, boolean block) {
		int offset = 0, available, written = 0;
		long endtime = System.currentTimeMillis() + lengthInMsecs;
		for(;;) {
			available = Math.min(line.available(), data.length - offset);
			if(available == 0) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException ie) {
					
				}
				continue;
			}
			available = Math.min(available, 1000*format.getFrameSize());
			written = line.write(data, offset, available);
			debug("Wrote " + written + " of " + available + " available");
			offset += written;
			if (offset < data.length) {
				continue;
			}
			if(block) {
				sleepUntil(endtime);
			}
			break;
		}
	}
	
	/**
	 * For internal use only, sleep until a given time.
	 * @param time
	 */
	protected void sleepUntil(long time) {
		long now = System.currentTimeMillis();
		if(now < time) {
			try {
				Thread.sleep(time - now);
			} catch (InterruptedException ie) {
			}
		}
	}
	
	public void generate(int duration, double frequency) {	
		format = AudioUtils.DEFAULT_FORMAT;
		data = AudioUtils.generate(duration, frequency);
		int numSamples = data.length * 8 / format.getChannels() / format.getSampleSizeInBits();
		lengthInMsecs = (int) (1000 * numSamples / format.getSampleRate());  
	}
	
	protected final void debug(String msg) {
		//System.err.println("" + new java.sql.Timestamp(System.currentTimeMillis()).toString() + " Clip: " + msg);
	}
	
	public void debugFormat() {
		System.err.println("  numChannels = " + format.getChannels());
		System.err.println("  frameRate = " + format.getFrameRate() + " frames/sec");
		System.err.println("  frameSize = " + format.getFrameSize() + " bytes");
		System.err.println("  sampleRate = " + format.getSampleRate() + " samples/sec");
		System.err.println("  sampleSize = " + format.getSampleSizeInBits() + " bits");
		System.err.println("  encoding = " + format.getEncoding());
		System.err.println("  endianness = " + (format.isBigEndian() ? " big" : " little"));
	}
	
	/**
	 * The main method is a simple test harness for the AudioClip.
	 * 
	 * @param arg
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		String filename = "demo\\audio\\heartbeat.wav";
		Debug.dp(Debug.INFORMATION, "Opening file " + filename);
		AudioClip clip = new AudioClip();
		clip.loadFile(filename);
		Debug.dp(Debug.INFORMATION, "Opened file");
		clip.debugFormat();
		
		Debug.dp(Debug.INFORMATION, "Opening line");
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, clip.getFormat());
		SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
		line.open(clip.getFormat());
		line.start();
		Debug.dp(Debug.INFORMATION, "Opened line");
		Thread.sleep(1000);
		Debug.dp(Debug.INFORMATION, "");
		
		Debug.dp(Debug.INFORMATION, "Playing clip");
		clip.play(line, true);
		Debug.dp(Debug.INFORMATION, "Played clip");
		Thread.sleep(1000);
		Debug.dp(Debug.INFORMATION, "");
		
		Debug.dp(Debug.INFORMATION, "Generating clip @ 1000");
		clip.generate(1000, 100);
		Debug.dp(Debug.INFORMATION, "Generated clip ");
		
		Debug.dp(Debug.INFORMATION, "Playing clip");
		clip.play(line, true);
		Debug.dp(Debug.INFORMATION, "Played clip");
		Thread.sleep(1000);
		Debug.dp(Debug.INFORMATION, "");
		
		Debug.dp(Debug.INFORMATION, "Generating clip @ 2000");
		clip.generate(1000, 200);
		Debug.dp(Debug.INFORMATION, "Generated clip ");
		
		Debug.dp(Debug.INFORMATION, "Playing clip");
		clip.play(line, true);
		Debug.dp(Debug.INFORMATION, "Played clip");
		Thread.sleep(1000);
		Debug.dp(Debug.INFORMATION, "");
		
		Debug.dp(Debug.INFORMATION, "Closing line");
		line.close();	
		Debug.dp(Debug.INFORMATION, "Closed");
	}
}	
