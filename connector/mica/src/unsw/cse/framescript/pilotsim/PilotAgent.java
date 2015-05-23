package unsw.cse.framescript.pilotsim;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import unsw.cse.mica.MicaException;
import unsw.cse.mica.TransportException;
import unsw.cse.mica.agent.DefaultAgent2;
import unsw.cse.mica.data.Mob;
import unsw.cse.mica.util.MicaProperties;

public class PilotAgent extends DefaultAgent2 implements NLPListener {
	public static final String TYPE_TEXTFORUSER = "textForUser";
	public static final String TYPE_TEXTFROMUSER = "textFromUser";
	public static final String SLOT_UTTERANCE = "utterance";
	public static final String SLOT_ALTERNATIVES = "alternatives";
	
	Pilot pilot;
	PilotUI ui;
	
	public synchronized void handleNewMob(Mob m) {
		if (isATypeOf(m , TYPE_TEXTFROMUSER)) {
			String input = m.getSlot1(SLOT_UTTERANCE);
			String [] alternatives = null;
			if (m.hasSlot(SLOT_ALTERNATIVES)) {
				List l = m.getSlot(SLOT_ALTERNATIVES);
				alternatives = new String [l.size()];
				for (int i = 0; i < l.size(); i++) {
					alternatives[i] = (String)l.get(i);
				}
			}
			ui.processInput(input, alternatives);
		}
	}

	public synchronized void init(MicaProperties args) throws MicaException {
		super.init(args);
		at.register(TYPE_TEXTFROMUSER);
		pilot = new Pilot();
		ui = new PilotUI(pilot);
		ui.nlp.addListener(this);
		ui.frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				uiClosing();
			}
		});
		pilot.startSimulation();
	}
	
	synchronized void uiClosing() {
		ui = null;
		try {
			terminate();
		} catch (MicaException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void terminate() throws MicaException {
		if (ui != null) {
			ui.closeWindow();
		}
		if (at.isConnected())
			at.disconnect();
	}

	public void responseGiven(String text) {
		Mob m = new Mob(TYPE_TEXTFORUSER);
		m.addSlot(SLOT_UTTERANCE, text);
		try {
			at.writeMob(m);
		} catch (TransportException e) {
			e.printStackTrace();
		}
	}
}
