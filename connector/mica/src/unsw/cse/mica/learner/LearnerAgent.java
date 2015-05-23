/*
 * Created on 4/07/2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package unsw.cse.mica.learner;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import unsw.cse.mica.MicaException;
import unsw.cse.mica.agent.AgentTransport;
import unsw.cse.mica.agent.DefaultAgent;
import unsw.cse.mica.agent.XMLOverTCPAgentTransport;
import unsw.cse.mica.data.Mob;
import unsw.cse.mica.util.Debug;
import unsw.cse.mica.util.MicaConstants;
import unsw.cse.mica.util.MicaProperties;
import unsw.cse.mica.util.XMLFilter;

/**
 * @author waleed
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class LearnerAgent extends DefaultAgent {

	MobCache cache = new MobCache(); 
	AgentTransport at;
	Map learnerTasks = new HashMap();  
	
	public LearnerAgent(){
	}
	
	private void preInit(String micaHome) {
		try {
			String learntasks;
		
			Debug.dp(Debug.EMERGENCY, "Home directory is: " + micaHome);
			learntasks = "config/learntask";
			Debug.dp(Debug.INFORMATION, "Loading learning tasks from "+ micaHome + "/" + learntasks); 
			String taskPath = micaHome + "/" + learntasks;
			File taskDir = new File(taskPath);
			File[] files = taskDir.listFiles(new XMLFilter()); 
			for(int i=0; i < files.length; i++){
				Debug.dp(Debug.INFORMATION, "Reading learning task in " + files[i]);
				LearnTask l = new LearnTask(cache, files[i]); 
				learnerTasks.put(l.getTaskName(), l); 
			}
		}
		catch(Exception e){
			Debug.dp(Debug.IMPORTANT, "WARNING: Could not read type files.");
			e.printStackTrace(); 
		}
	}
	
	/* (non-Javadoc)
	 * @see unsw.cse.mica.agent.Agent#init()
	 */
	public void init(MicaProperties args) throws MicaException {
		preInit(args.getValue(MicaConstants.PARAM_MICA_HOME, MicaConstants.DEFAULT_MICA_HOME));
		at.connect("learnerAgent"); 
		at.register("learnerTrain");
		at.register("learnerTest");
		// Now refresh from data already existing on the blackboard, but before we connected.
		for(Iterator i = cache.getHotTypes().iterator(); i.hasNext(); ){
			String hotType = (String) i.next();
			// Gets the most recent mob of this type.  
			List retmobs = at.mobSearch("select top 1 * from mobs where typeof(mob,'" + hotType + "') order by getSlot1(mob, 'creationTime') desc");
			// Now, the above is guaranteed to have exactly one element. 
			// Put them in the cache. 
			if(retmobs.size() > 0){
				cache.cacheMob((Mob) retmobs.get(0));
			} 
			at.register(hotType);
		}
	}
					
	
	/* (non-Javadoc)
	 * @see unsw.cse.mica.agent.Agent#handleMob(unsw.cse.mica.data.Mob)
	 */
	public void handleNewMob(Mob m) {
		try {
			cache.cacheMob(m); 
			if(m.getType().equals("learnerTrain")){
				String task = m.getSlot1("task"); 
				String actualClass = m.getSlot1("actualClass"); 
				LearnTask l = (LearnTask) learnerTasks.get(task);
				l.addTrainingInstance(actualClass);
				l.saveData(); 
				l.learn(); 
				l.saveModel();   
				Mob updated = new Mob("learnerUpdated");
				at.writeMob(updated);
			}
			if(m.getType().equals("learnerTest")){
				String task = m.getSlot1("task"); 
				LearnTask l = (LearnTask) learnerTasks.get(task);
				String requestId = m.getSlot1("requestId"); 
				ClassResult returnResult = l.classify();
				Mob retval = new Mob("learnerReply"); 
				retval.addSlot("requestId", requestId); 
				retval.addSlot("predictedClass", returnResult.predictedClass);
				retval.addSlot("confidence", String.valueOf(returnResult.confidence));
				at.writeMob(retval);  
			}
		} catch (MicaException me) {
			me.printStackTrace();
		}
	}
	
	public void terminate() {
	}

	public static void main(String[] args) throws Exception {
		String server = args[0]; 
		int portNum = Integer.parseInt(args[1]);
		LearnerAgent la = new LearnerAgent(); 
		new XMLOverTCPAgentTransport(la, server, portNum);
		la.init(null); 
	}
	/**
	 * @return
	 */
	public AgentTransport getTransport() {
		return at;
	}

	/**
	 * @param transport
	 */
	public void setTransport(AgentTransport transport) {
		at = transport;
	}


}
