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

/**
 * @author waleed
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Pingee extends DefaultAgent implements PingConstants {
	
		
	public Pingee(){
	}


	/* (non-Javadoc)
	 * @see unsw.cse.mica.agent.Agent#handleMob(unsw.cse.mica.data.Mob)
	 */
	public void handleNewMob(Mob m) {
		try {
			if(m.getType().equals(MOB_TYPE_PINGREQUEST)){
				long currTime = System.currentTimeMillis(); 
				Mob m2 = new Mob(MOB_TYPE_PINGREPLY); 
				m2.addSlot(SLOT_RECEIVETIME, Long.toString(currTime));
				m2.addSlot(SLOT_ID, m.getSlot1(SLOT_ID)); 
				m2.addSlot(SLOT_SENDTIME, m.getSlot1(SLOT_SENDTIME)); 
				at.writeMob(m2);  				
			}
		} catch (MicaException me) {
			me.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see unsw.cse.mica.agent.Agent#init()
	 */
	public void init(MicaProperties args) throws MicaException {
		at.connect("Pingee");
		at.register(MOB_TYPE_PINGREQUEST);
	}
	
	public void terminate() throws MicaException {
		at.disconnect();
	}

	public static void main(String[] args) throws MicaException {
		String server = args[0]; 
		int portNum = Integer.parseInt(args[1]);
		Pingee p = new Pingee(); 
		new XMLOverTCPAgentTransport(p, server, portNum);
		p.init(null); 
	}
}
