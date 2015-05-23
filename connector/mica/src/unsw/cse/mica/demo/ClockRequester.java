/*
 * Created on 20/05/2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package unsw.cse.mica.demo;

import unsw.cse.mica.MicaException;
import unsw.cse.mica.agent.DefaultAgent2;
import unsw.cse.mica.data.Mob;
import unsw.cse.mica.util.MicaProperties;

/**
 * @author Waleed Kadous
 *
 */
public class ClockRequester extends DefaultAgent2 {
	
	private int replyCount = 0; 
	
	/**
	 *  
	 * @see unsw.cse.mica.agent.Agent#handleNewMob(java.lang.String)
	 */
	public void handleNewMob(Mob m) {
		try {
			if(m.getType().equals("clockReply")){
				replyCount++; 
				if(replyCount > 5){
					System.out.println("Reply > 5 ... calling for a stop");
//					Mob newMob = new Mob("clockCancel", "cc");
					Mob newMob = new Mob("clockCancel");
					newMob.addSlot("label", "every5secs");
					at.writeMob(newMob);
				}
			
			}
		} catch (MicaException me) {
			me.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see unsw.cse.mica.agent.Agent#init()
	 */
	public void init(MicaProperties args) throws MicaException {
		args.add(PARAM_NAME, "clockrequester");
		super.init(args);
		at.register("clockReply");
		
//		Mob clockReq = new Mob("clockRequest", "every5secs");
		Mob clockReq = new Mob("clockRequest");
		clockReq.addSlot("label", "every5secs");
		clockReq.addSlot("period", "5000");
		at.writeMob(clockReq);
	}
}
