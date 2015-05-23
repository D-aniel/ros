/*
 * Created on 13/09/2004
 */
package unsw.cse.mica.demo.audio;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import unsw.cse.mica.util.Debug;

/**
 * An extension of the AudioClip class enabling the looped playing of a 
 * clip with a delay between each play. Note that the times used are approximate so
 * a delay of zero is likely to cause unpleasant clicking when the audio is played, 
 * so this class is <i>not</i> suitable for continuous looping without pausing. 
 * (In such a situation it may be more approriate to use a <tt>javax.sound.sampled.AudioClip</tt>
 * or similar.)
 * 
 * @author jhw
 */
public class AudioLoop extends AudioClip implements Runnable {
	
	//private static final int CHUNK_SIZE = 512;

	private Thread thread;
	private SourceDataLine line;
	private long pause;
	private int count;
	
	private boolean busy;
	private boolean shutdown;

	public AudioLoop() {
	}
	
	public synchronized void startLoopThread() {
		if(thread != null) {
			return;
		}
		shutdown = false;
		thread = new Thread(this);
		thread.start();
	}
	
	public synchronized void stopLoopThread() {
		if (thread != null) {
			count = 0;
			shutdown = true;
			thread.interrupt();
		}
	}
	
	public void loop(SourceDataLine line, long pause) {
		loop(line, -1, pause);
	}
	
	public synchronized void loop(SourceDataLine line, int count, long pause) {
		if(thread == null || busy) {
			return;
		}
		this.busy = true;
		this.line = line;
		this.count = count;
		this.pause = pause;
		thread.interrupt();
	}
	
	public synchronized void interrupt() {
		if(thread != null && busy) {
			debug("Loop: interrupting");
			count = 0;
			thread.interrupt();
		}
	} 
	
	public void run() {
		while(!shutdown) {
			debug("Loop:   top of run loop");
			if(count != 0) {
				doLoop();
				continue;
			}
			try {
				debug("Loop:   inactive so sleeping");
				Thread.sleep(Long.MAX_VALUE);
			} catch (InterruptedException ie) {
			}
		}
		shutdown = false;
		thread = null;
	}
	
	private void doLoop() {
		debug("Loop:   top of doLoop");
		long time = System.currentTimeMillis();
		while(count != 0) {
			debug("Loop:   playing");
			play(line, false);
			debug("Loop:   played");
			if(count > 0) {
				count--;
			}
			if(pause > 0) {
				time = time + getLengthInMsecs() + pause;
				sleepUntil(time);
			}
		}
		busy = false;
	}
	
	/**
	 * The main method is a simple test harness for the AudioPlayer.
	 * 
	 * @param arg
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		
		String filename = "demo\\audio\\heartbeat.wav";
		Debug.dp(Debug.INFORMATION, "Opening file " + filename);
		AudioLoop loop = new AudioLoop();
		loop.loadFile(filename);
		loop.startLoopThread();
		Debug.dp(Debug.INFORMATION, "Opened file");
		loop.debugFormat();
		
		Debug.dp(Debug.INFORMATION, "Opening line");
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, loop.getFormat());
		SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
		line.open(loop.getFormat());
		line.start();
		Debug.dp(Debug.INFORMATION, "Opened line");
		
		Debug.dp(Debug.INFORMATION, "Looping clip 3 times");
		loop.loop(line, 3, 500);
		Thread.sleep(5000);
		Debug.dp(Debug.INFORMATION, "Looped clip 3 times");
		
		Debug.dp(Debug.INFORMATION, "Looping clip indefinitely");
		loop.loop(line, 500);
		Thread.sleep(3000);
		loop.interrupt();
		Debug.dp(Debug.INFORMATION, "Interrupted clip");
		Thread.sleep(1000);
		
		
		Debug.dp(Debug.INFORMATION, "Looping without pausing");
		loop.generate(1000, 200);
		loop.loop(line, 5, 0);
		Thread.sleep(5000);
		Debug.dp(Debug.INFORMATION, "Interrupted clip");
				
		Thread.sleep(1000);
		
		Debug.dp(Debug.INFORMATION, "Closing line");
		loop.stopLoopThread();
		line.stop();
		line.close();
		Debug.dp(Debug.INFORMATION, "Closed");
	}

}