/*
 * Created on 9/07/2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package unsw.cse.mica.demo;

import java.util.Random;

import unsw.cse.mica.MicaException;
import unsw.cse.mica.agent.DefaultAgent2;
import unsw.cse.mica.agent.XMLOverTCPAgentTransport;
import unsw.cse.mica.data.Mob;
import unsw.cse.mica.util.Debug;
import unsw.cse.mica.util.MicaProperties;

/**
 * @author waleed
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class EmailAgent extends DefaultAgent2 {
	
	/** (non-Javadoc)
	 * @see unsw.cse.mica.agent.Agent#handleNewMob(unsw.cse.mica.data.Mob)
	 */
	public void handleNewMob(Mob m) {
		try {
			if(m.getType().equals("emailListRequest")){
				Debug.dp(Debug.FN_CALLS, "Email agent: Got request"); 
				Mob reply = new Mob("emailListReply"); 
				Random r = new Random(); 
				int numEmails = Math.abs(r.nextInt()) % 30;
				reply.addSlot("count", String.valueOf(numEmails));
				for(int i=0; i < numEmails; i++){
					reply.addSlot("from", "Test Sender " + i); 
					reply.addSlot("subject", "Test Subject " + i); 
				}
				at.writeMob(reply); 
			}
		} catch (MicaException me) {
			me.printStackTrace();
		}
	}
	/* (non-Javadoc)
	 * @see unsw.cse.mica.agent.Agent#init()
	 */
	public void init(MicaProperties args) throws MicaException {
		args.add(PARAM_NAME, "emailAgent");
		super.init(args); 
		at.register("emailListRequest");  
	}
	
	public static void main(String[] args) throws MicaException {
		String server = args[0]; 
		int portNum = Integer.parseInt(args[1]);
		EmailAgent ea = new EmailAgent(); 
		new XMLOverTCPAgentTransport(ea, server, portNum);
		ea.init(null); 
	}
}
