/*
 * Created on 10/09/2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package unsw.cse.mica.demo;

import unsw.cse.mica.MicaException;
import unsw.cse.mica.agent.DefaultAgent;
import unsw.cse.mica.agent.XMLOverTCPAgentTransport;
import unsw.cse.mica.data.Mob;
import unsw.cse.mica.util.MicaProperties;
import unsw.cse.mica.util.MicaUtils;

/**
 * @author waleed
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Pinger extends DefaultAgent implements PingConstants {
	protected int numPingsExpected;
	protected int numPingsSent;
	protected int numPingsReceived;
	protected long totalWriteTime;
	protected long[] sendTimes;
	protected long[] receiveTimes; 
	
	public Pinger() {
		this(10);
	}
	
	public Pinger(int numPingsExpected) {
		this.numPingsExpected = numPingsExpected; 
		sendTimes = new long[numPingsExpected];
		receiveTimes = new long[numPingsExpected];
		numPingsSent = 0;
		numPingsReceived = 0; 
	}

	public void handleNewMob(Mob mob) {
		if(!mob.getType().equals(MOB_TYPE_PINGREPLY)) {
			return;
		}
		int id = mob.getSlot1AsInt(SLOT_ID);
		receiveTimes[id] = System.currentTimeMillis();
		if(id > numPingsReceived){
			System.out.println("WARNING: Out of order pingR = " + numPingsReceived + " id = " + id);
		}
		numPingsReceived++;  
		if(numPingsReceived == numPingsExpected) {
			analyseResults();
			try {
				terminate();
			} catch (MicaException me) {
				me.printStackTrace();
			}
			System.exit(0); 
		} else {
			sendPing();		
		}
	}
	
	public void sendPing() {    
		Mob mob = new Mob(MOB_TYPE_PINGREQUEST); 
		mob.addSlot(SLOT_ID, Integer.toString(numPingsSent));
		
		sendTimes[numPingsSent] = System.currentTimeMillis();
		mob.addSlot(SLOT_SENDTIME, MicaUtils.timeToString(sendTimes[numPingsSent]));
		try {
			at.writeMob(mob);
		} catch (MicaException me) {
			me.printStackTrace();
			System.exit(0);
		}
		long postTime = System.currentTimeMillis(); 
		totalWriteTime += postTime-sendTimes[numPingsSent]; 
		numPingsSent++;
	}

	public void analyseResults(){
		double sum=0, sum2=0;
		for(int i=0; i < numPingsReceived; i++){
			long difference = receiveTimes[i]-sendTimes[i];
			sum += difference;
			sum2 += difference*difference;															 																			 
		}
		System.out.println("Average latency: " + (sum/numPingsReceived/2) + "ms"); 
		System.out.println("Messages per second = " + numPingsReceived*2.0*1000/(receiveTimes[numPingsReceived-1]-sendTimes[0]));
		System.out.println("Write latency = " + ((double) totalWriteTime)/numPingsReceived +"ms"); 
	}

	public void init(MicaProperties args) throws MicaException {
		at.connect("Pinger");
		at.register(MOB_TYPE_PINGREPLY);
		sendPing();
	}
	
	public void terminate() throws MicaException {
		at.disconnect();
	}

	public static void main(String[] args) throws MicaException {
		String server = args[0]; 
		int portNum = Integer.parseInt(args[1]);
		int numPingsExpected =Integer.parseInt(args[2]); 
		Pinger p = new Pinger(numPingsExpected); 
		new XMLOverTCPAgentTransport(p, server, portNum);
		p.init(null); 
	}
}
