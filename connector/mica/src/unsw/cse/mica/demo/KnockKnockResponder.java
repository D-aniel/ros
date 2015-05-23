/*
 * Created on 20/05/2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package unsw.cse.mica.demo;

import unsw.cse.mica.MicaException;
import unsw.cse.mica.TransportException;
import unsw.cse.mica.agent.AgentTransport;
import unsw.cse.mica.agent.DefaultAgent;
import unsw.cse.mica.data.Mob;
import unsw.cse.mica.util.MicaProperties;

/**
 * @author waleed
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class KnockKnockResponder extends DefaultAgent {

	private AgentTransport at; 

	/* (non-Javadoc)
	 * @see unsw.cse.mica.agent.Agent#handleMob(java.lang.String)
	 */
	 
	/* (non-Javadoc)
	 * @see unsw.cse.mica.agent.Agent#getTransport()
	 */
	public AgentTransport getTransport() {
		return at;
	}
	/* (non-Javadoc)
	 * @see unsw.cse.mica.agent.Agent#setTransport(unsw.cse.mica.agent.AgentTransport)
	 */
	public void setTransport(AgentTransport at) {
		this.at = at; 
	}
	public void handleNewMob(Mob m) {
		if(m.getType().equals("knocker")){
			System.out.println("Now I have to say who's there?");	
		}
		else{
			System.out.println("Unknown object received");
		}

	}

	/* (non-Javadoc)
	 * @see unsw.cse.mica.agent.Agent#init()
	 */
	public void init(MicaProperties args) throws MicaException {
		at.connect("knockresponder");
		at.register("knocker");
	}
	
	public void terminate() throws TransportException {
		at.disconnect();
	}

}
