package unsw.cse.framescript.pilotsim;

import java.awt.EventQueue;
import java.io.File;
import java.net.URL;
import java.util.Vector;

import unsw.cse.framescript.Atom;
import unsw.cse.framescript.Conversation;
import unsw.cse.framescript.Domain;
import unsw.cse.framescript.FSException;
import unsw.cse.framescript.FSInteger;
import unsw.cse.framescript.FrameScript;
import unsw.cse.framescript.Generic;
import unsw.cse.framescript.Instance;
import unsw.cse.framescript.Pattern;
import unsw.cse.framescript.PilotFrameBridge;
import unsw.cse.framescript.StackFrame;
import unsw.cse.framescript.Subr;
import unsw.cse.framescript.Term;
import unsw.cse.framescript.Utils;

public class PilotNLP implements PilotListener, Conversation{
	public final static String DOMAIN_PILOTSIM = "pilot_sim";
	public final static String GENERIC_COURSEALTERATION = "courseAlteration";
	public final static String SLOT_REASON = "reason";
	public final static String SLOT_ELEVATION = "altitude";
	public final static String SLOT_HEADING = "heading";
	public final static String SLOT_SPEED = "speed";
	public final static String SCRIPT_FILE = "../scripts/pilot.frs";
	Pilot pilot;
	
	Domain pilotDomain;
	Generic alterationGeneric;
		
	Vector listeners;
	
	static PilotNLP currentNLP;
	
	PilotNLP(Pilot pilot) {
		init();
		this.pilot = pilot;
		pilot.addListener(this);
		pilotDomain = PilotFrameBridge.getDomain(DOMAIN_PILOTSIM);
		alterationGeneric = PilotFrameBridge.getGeneric(GENERIC_COURSEALTERATION);
		listeners = new Vector();
	}
	
	public void processInput(String input, String [] alternatives) {
		try {
			suffix = null;
			String response = processInput(this, Utils.getPattern(input));
			if (response.equals(AlternateRequest)) {
				for (int i = 0; alternatives != null && i < alternatives.length
						&& response.equals(AlternateRequest); i++) {
					response = processInput(this, Utils.getPattern(alternatives[i]));
				}
				if (response.equals(AlternateRequest)) {
					Pattern noAlts = Utils.getPattern(NoMoreAlternatesResponse);
					if (suffix != null)
						noAlts = noAlts.append(new Pattern(suffix));
					response = processInput(this, noAlts);
				}
			}
			respond(response);
		} catch (Exception e) {
			respond(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void processInput(String input) {
		processInput(input, null);
	}
	
	public void alteringCourse(String reason) {
		try {
			Instance i = PilotFrameBridge.getInstance(alterationGeneric);
			PilotFrameBridge.addSlot(i, SLOT_REASON, Utils.getPattern(reason));
			if (pilot.currentAltitude != pilot.desiredAltitude)
				PilotFrameBridge.addSlot(i, SLOT_ELEVATION, new FSInteger(pilot.desiredAltitude));
			if (pilot.currentHeading != pilot.desiredHeading)
				PilotFrameBridge.addSlot(i, SLOT_HEADING, new FSInteger(pilot.desiredHeading));
			if (pilot.currentSpeed != pilot.desiredSpeed)
				PilotFrameBridge.addSlot(i, SLOT_SPEED, new FSInteger(pilot.desiredSpeed));
			String response = processInput(this, new Pattern(i));
			respond(response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void courseChanged() {}
	
	synchronized static String processInput(PilotNLP nlp, Pattern input) {
		PilotNLP oldNLP = currentNLP;
		currentNLP = nlp;
		String rval = "";
		try {
			Pattern p = nlp.pilotDomain.getResponse(input);
			rval = Utils.getMessage(p);
		} catch (FSException e) {
			e.printStackTrace();
		}
		currentNLP = oldNLP;
		return rval;
	}
	
	synchronized public void addListener(NLPListener listener) {
		listeners.add(listener);
	}
	
	synchronized public void removeListener(NLPListener listener) {
		listeners.remove(listener);
	}
	
	class ListenerNotifier implements Runnable {
		NLPListener listener;
		String response;
		
		ListenerNotifier(NLPListener listener, String response) {
			this.listener = listener;
			this.response = response;
		}
		
		public void run() {
			listener.responseGiven(response);
		}
	}
	
	synchronized void respond(String response) {
		if (response == null || response.trim().equals(""))
			return;
		for (int i = 0; i < listeners.size(); i++) {
			EventQueue.invokeLater(new ListenerNotifier((NLPListener)listeners.get(i), response));
		}
	}
	
	static FSException NoCurrentNLP(String functor) throws FSException {
		String msg = functor + " - No active pilot found.";
		throw new FSException(msg);
	}
	
	static boolean initialised = false;
	static void init() {
		if (initialised)
			return;
		
		try {
			FrameScript.init();
			new Subr("currentHeading", 0) {
				public Term apply(Instance currentObject, Term[] args, StackFrame frame) throws FSException {
					if (currentNLP == null)
						throw NoCurrentNLP("currentHeading");
					return new FSInteger(currentNLP.pilot.currentHeading);
				}
			};

			new Subr("currentAltitude", 0) {
				public Term apply(Instance currentObject, Term[] args, StackFrame frame) throws FSException {
					if (currentNLP == null)
						throw NoCurrentNLP("currentAltitude");
					return new FSInteger(currentNLP.pilot.currentAltitude);
				}
			};

			new Subr("currentSpeed", 0) {
				public Term apply(Instance currentObject, Term[] args, StackFrame frame) throws FSException {
					if (currentNLP == null)
						throw NoCurrentNLP("currentSpeed");
					return new FSInteger(currentNLP.pilot.currentSpeed);
				}
			};

			new Subr("desiredHeading", 0) {
				public Term apply(Instance currentObject, Term[] args, StackFrame frame) throws FSException {
					if (currentNLP == null)
						throw NoCurrentNLP("desiredHeading");
					return new FSInteger(currentNLP.pilot.desiredHeading);
				}
			};

			new Subr("desiredAltitude", 0) {
				public Term apply(Instance currentObject, Term[] args, StackFrame frame) throws FSException {
					if (currentNLP == null)
						throw NoCurrentNLP("desiredAltitude");
					return new FSInteger(currentNLP.pilot.desiredAltitude);
				}
			};

			new Subr("desiredSpeed", 0) {
				public Term apply(Instance currentObject, Term[] args, StackFrame frame) throws FSException {
					if (currentNLP == null)
						throw NoCurrentNLP("desiredSpeed");
					return new FSInteger(currentNLP.pilot.desiredSpeed);
				}
			};

			new Subr("changeHeading", 1) {
				public Term apply(Instance currentObject, Term[] args, StackFrame frame) throws FSException {
					FSInteger i = Utils.check_integer("changeHeading", currentObject, args, 1, frame);
					if (currentNLP == null)
						throw NoCurrentNLP("changeHeading");
					currentNLP.pilot.setHeading((int)i.iVal);
					return Atom._null;
				}
			};

			new Subr("changeAltitude", 1) {
				public Term apply(Instance currentObject, Term[] args, StackFrame frame) throws FSException {
					FSInteger i = Utils.check_integer("changeAltitude", currentObject, args, 1, frame);
					if (currentNLP == null)
						throw NoCurrentNLP("changeAltitude");
					currentNLP.pilot.setAltitude((int)i.iVal);
					return Atom._null;
				}
			};

			new Subr("changeSpeed", 1) {
				public Term apply(Instance currentObject, Term[] args, StackFrame frame) throws FSException {
					FSInteger i = Utils.check_integer("changeSpeed", currentObject, args, 1, frame);
					if (currentNLP == null)
						throw NoCurrentNLP("changeSpeed");
					currentNLP.pilot.setSpeed((int)i.iVal);
					return Atom._null;
				}
			};
			
			File f = findScriptFile();
			if (f != null) {
				Utils.loadFile(f);
			} else
				System.err.println("Unable to locate script file.");
			initialised = true;
		} catch (FSException e) {
			e.printStackTrace();
		}
	}
	
	static File findScriptFile() {
		File f = new File(SCRIPT_FILE);
		if (f.exists() && f.isFile() && f.canRead())
			return f;
		URL url = PilotNLP.class.getClassLoader().getResource(SCRIPT_FILE);
		if (url != null)
			return new File(url.getFile());
		return null;
	}

	public void processInput(Pattern input) throws FSException {
		try {
			suffix = null;
			String response = processInput(this, input);
			if (response.equals(AlternateRequest)) {
				if (response.equals(AlternateRequest)) {
					Pattern noAlts = Utils.getPattern(NoMoreAlternatesResponse);
					if (suffix != null)
						noAlts = noAlts.append(new Pattern(suffix));
					response = processInput(this, noAlts);
				}
			}
			respond(response);
		} catch (Exception e) {
			respond(e.getMessage());
			e.printStackTrace();
		}
	}
	
	Term suffix = null;
	public void setNoMoreAltsSuffix(Term suffix) {
		this.suffix = suffix;
	}
}
