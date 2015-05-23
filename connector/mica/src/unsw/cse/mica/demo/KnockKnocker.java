/*
 * Created on 20/05/2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package unsw.cse.mica.demo;

import unsw.cse.mica.MicaException;
import unsw.cse.mica.agent.DefaultAgent;
import unsw.cse.mica.data.Mob;
import unsw.cse.mica.util.MicaProperties;

/**
 * @author waleed
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class KnockKnocker extends DefaultAgent {

	/* (non-Javadoc)
	 * @see unsw.cse.mica.agent.Agent#handleMob(java.lang.String)
	 */
	public void handleNewMob(Mob m) {
			// System.out.println("Got an object called: " + objName); 
			System.out.println("I got " + m.toString());  
	}

	/* (non-Javadoc)
	 * @see unsw.cse.mica.agent.Agent#init()
	 */
	public void init(MicaProperties args) throws MicaException {
		at.connect("knockknocker");
		at.register("knocker"); 
		Mob knocker;
		for(int i=0; i < 10; i++){ 
//			knocker = new Mob("knocker", "k1");
			knocker = new Mob("knocker");
			knocker.addSlot("question", "knock knock");
			String knockerName = at.writeMob(knocker);
			System.out.println("Mob name = " + knockerName);
		}
		at.disconnect(); 
		System.exit(0);
	}
}
