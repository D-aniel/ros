/*
 * Created on 9/12/2004
 */
package unsw.cse.mica.demo.timeteller;

import java.sql.Timestamp;

import unsw.cse.mica.MicaException;
import unsw.cse.mica.agent.DefaultAgent2;
import unsw.cse.mica.data.Mob;
import unsw.cse.mica.demo.BeepAgent;
import unsw.cse.mica.tools.timer.TimerUtils;
import unsw.cse.mica.util.MicaConstants;
import unsw.cse.mica.util.MicaProperties;

/**
 * An agent-based implementation of a time-teller. It uses
 * a <tt>TimerAgent</tt> to generate events, a <tt>BeepAgent</tt> to
 * generate the tones, and assumes there is another agent capable of
 * speaking text given a <tt>textforUser</tt> Mob.
 * For a standalone version of a time-teller see the <tt>TimeTellerStandalone</tt> class.
 * 
 * @author jhw
 */
public class TimeTellerAgent extends DefaultAgent2 implements ITimeTellerConstants {
		
	public TimeTellerAgent() {}
	
	public void init(MicaProperties args) throws MicaException {
		// connect
		args.add(PARAM_NAME, "TimeTeller");
		super.init(args);
		
		// register for the timer output
		at.register(TYPE_SAY);
		
		// and prep
		long now = System.currentTimeMillis();
		long time = now - (now % 10000);
		if(time < now) {
			time += 10000;
		}
		at.writeMob(createMob(TYPE_SAY, TYPE_SAY, time + SAY_OFFSET));
		
		// these can go directly to the BeepAgent
		Mob mob = createMob(BeepAgent.TYPE_BEEP, TYPE_BEEP1, time + BEEP1_OFFSET);
		mob.addSlot(BeepAgent.SLOT_DURATION, "" + BEEP_LENGTH);
		mob.addSlot(BeepAgent.SLOT_FREQUENCY, "" + BEEP1_FREQUENCY);
		at.writeMob(mob);
		
		mob = createMob(BeepAgent.TYPE_BEEP, TYPE_BEEP2, time + BEEP2_OFFSET);
		mob.addSlot(BeepAgent.SLOT_DURATION, "" + BEEP_LENGTH);
		mob.addSlot(BeepAgent.SLOT_FREQUENCY, "" + BEEP2_FREQUENCY);
		at.writeMob(mob);
		
		mob = createMob(BeepAgent.TYPE_BEEP, TYPE_BEEP3, time + BEEP3_OFFSET);
		mob.addSlot(BeepAgent.SLOT_DURATION, "" + BEEP_LENGTH);
		mob.addSlot(BeepAgent.SLOT_FREQUENCY, "" + BEEP3_FREQUENCY);
		at.writeMob(mob);
	}
	
	protected Mob createMob(String type, String name, long timestamp) {
		Mob mob = new Mob(TimerUtils.TYPE_timerRequest);
		mob.addSlot(TimerUtils.SLOT_eventType, type);
		mob.addSlot(TimerUtils.SLOT_eventName, name);
		mob.addSlot(TimerUtils.SLOT_absoluteTime, new Timestamp(timestamp).toString());
		mob.addSlot(TimerUtils.SLOT_period, "" + 10000);
		mob.setPersistence(MicaConstants.PERSISTENCE_TRANSIENT);
		return mob;
	}
	
	public void handleNewMob(Mob mob) {
		try {
			long timestamp = Timestamp.valueOf(mob.getSlot1(TimerUtils.SLOT_eventTime)).getTime();
			String str = PROMPT + TimeTellerUtils.getTimeString(timestamp - SAY_OFFSET + 10000);
			Mob tfu = new Mob("textForUser");
			tfu.addSlot("utterance", str);
			tfu.setPersistence(MicaConstants.PERSISTENCE_TRANSIENT);
			at.writeMob(tfu);
		} catch (MicaException me) {
			me.printStackTrace();
		}
	}
}