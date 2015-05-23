/*
 * Created on 22/05/2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package unsw.cse.mica.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import unsw.cse.mica.MicaException;
import unsw.cse.mica.agent.DefaultAgent2;
import unsw.cse.mica.data.Mob;
import unsw.cse.mica.util.MicaProperties;

/**
 * @author waleed
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Clock extends DefaultAgent2 {
	
	class ClockTimer extends TimerTask {
		
		String eventLabel; 

		public ClockTimer(String label) {
			super();
			eventLabel = label;
		}

		/* (non-Javadoc)
		 * @see java.util.TimerTask#run()
		 */
		public void run() {
			try {
				Mob m = new Mob("clockReply");
				m.addSlot("label", eventLabel);
				at.writeMob(m);
			} catch (MicaException me) {
				me.printStackTrace();
			}
		}
	}
	
	private Map clockTasks;
	private Timer myTimer;  
	
	public Clock(){
		clockTasks = new HashMap();
		myTimer	= new Timer(); 
	}
	

	/* (non-Javadoc)
	 * @see unsw.cse.mica.agent.Agent#handleMob(java.lang.String)
	 */
	public void handleNewMob(Mob m) {
		System.out.println("Clock: Got a request of type " + m.getType()); 
		if(m.getType().equals("clockRequest")){
			String label = m.getSlot1("label");
			long period = Long.parseLong(m.getSlot1("period"));			 
			ClockTimer ct = new ClockTimer(label);
			clockTasks.put(label, ct);
			myTimer.scheduleAtFixedRate(ct, 0, period);		
		} else if (m.getType().equals("clockCancel")) {
			String label = m.getSlot1("label");	
			ClockTimer ct = (ClockTimer) clockTasks.get(label);
			ct.cancel();
		}
	}

	/* (non-Javadoc)
	 * @see unsw.cse.mica.agent.Agent#init()
	 */
	public void init(MicaProperties args) throws MicaException {
		args.add(PARAM_NAME, "Clock");
		super.init(args);
		at.register("clockRequest");
		at.register("clockCancel");
	}
}
