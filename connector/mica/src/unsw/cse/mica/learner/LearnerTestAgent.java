/*
 * Created on 7/07/2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package unsw.cse.mica.learner;


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
public class LearnerTestAgent extends DefaultAgent {
	
	/* (non-Javadoc)
	 * @see unsw.cse.mica.agent.Agent#handleMob(unsw.cse.mica.data.Mob)
	 */
	public void handleNewMob(Mob m) {

	}

	/* (non-Javadoc)
	 * @see unsw.cse.mica.agent.Agent#init()
	 */
	public void init(MicaProperties args) throws MicaException {
		at.connect("LearnerTest"); 
		at.register("learnerReply"); 
		/*Mob m =*/ new Mob("learnerTrain"); 
	}
	
	public void terminate() throws MicaException {
		at.disconnect();
	}

	public static void main(String[] args) {
	}
}
