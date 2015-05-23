/*
 * This file is a simple demonstration of how to write a simple MICA 
 * client. 
 */
package unsw.cse.mica.demo;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import unsw.cse.mica.MicaException;
import unsw.cse.mica.agent.Agent;
import unsw.cse.mica.agent.AgentTransport;
import unsw.cse.mica.agent.XMLOverTCPAgentTransport;
import unsw.cse.mica.data.Mob;
import unsw.cse.mica.util.Debug;
import unsw.cse.mica.util.MicaProperties;


/**
 * 
 * @author waleed
 * 
 * Nothing fancy here. Just a simple class that 
 * calls our shutdown function when people close the window. 
 * This ensures that the SharedPad client disconnects from the 
 * Blackboard cleanly.  
 */

class MyClosingListener extends WindowAdapter  {
	SharedPad sp; 
		MyClosingListener(SharedPad sp){
		this.sp = sp; 
	}
	public void windowClosing(WindowEvent e) {
		try {
			sp.terminate();
		} catch (MicaException me) {
			me.printStackTrace();
		}
		System.exit(0);
	}
}



class MyMouseMotionListener extends MouseMotionAdapter {
	JPanel p; 
	Graphics g; 
	SharedPad s; 
	int oldX, oldY;
	public MyMouseMotionListener(SharedPad s){
		this.s = s; 
	}
		
	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
		try {
			g = s.getGraphics(); 
			int newX = e.getX(); 
			int newY = e.getY();
			if(oldX == -1 || oldY == -1){
				oldY = newY; 
				oldX = newX;  
			}
			s.newLine(oldX, oldY, newX, newY);
			oldX = newX; 
			oldY = newY; 
			super.mouseDragged(e);
		} catch (MicaException me) {
			me.printStackTrace();
		}
	}
	public void mouseMoved(MouseEvent e){
		oldX = -1; 
		oldY = -1; 
	}
}

/**
 *  SharedPad is a simple demonstration agent for MICA. 
 * <p>
 * It can be used as a shared drawing pad. 
 * 
 * @author waleed
 *
 */
public class SharedPad extends JPanel implements Agent {

	private AgentTransport at; 
	private List drawnLines = new ArrayList(); 

	/**  
	 *
	 */
	public SharedPad() { 
		createComponents();	
	}
	
	/**
	 * Handle a line that the user has drawn on our local display. 
	 * <p>
	 * This method is typically called by the Swing code to handle
	 * mouse drags. It then writes these to the blackboard.   
	 * 
	 * @param oldX Starting X position of line.
	 * @param oldY Starting Y position of line. 
	 * @param newX Ending X position of line. 
	 * @param newY Endling Y position of line. 
	 */
	public void newLine(int oldX, int oldY, int newX, int newY) throws MicaException {
		Mob m = new Mob("sharedPadLine");
		m.addSlot("oldX", String.valueOf(oldX));
		m.addSlot("oldY", String.valueOf(oldY));
		m.addSlot("newX", String.valueOf(newX));
		m.addSlot("newY", String.valueOf(newY));
		at.writeMob(m); 
	}
	
	/** 
	 * Handle incoming mobs. Since we've only registered for mobs 
	 * of type sharedPadLine, we'll only respond to those.  
	 * 
	 * @see unsw.cse.mica.agent.Agent#handleNewMob(java.lang.String)
	 */
	public void handleNewMob(Mob m) {
		if(m.getType().equals("sharedPadLine")){
			drawSharedLine(m);
		}
	}

	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Dimension winSize = getSize();
		g.clearRect(0,0, winSize.width, winSize.height);
		for(Iterator i = drawnLines.iterator(); i.hasNext();){
			Mob m = (Mob) i.next(); 
			int startX = m.getSlot1AsInt("oldX"); 
			int startY = m.getSlot1AsInt("oldY");
			int endX =   m.getSlot1AsInt("newX");
			int endY =   m.getSlot1AsInt("newY"); 
		
			g.drawLine(startX, startY, endX, endY);
		} 
	}

	public void drawSharedLine(Mob m){
		drawnLines.add(m);
		int startX = m.getSlot1AsInt("oldX"); 
		int startY = m.getSlot1AsInt("oldY");
		int endX =   m.getSlot1AsInt("newX");
		int endY =   m.getSlot1AsInt("newY"); 
		Graphics g = getGraphics(); 
		g.drawLine(startX, startY, endX, endY); 
	}

	/**
	 * @see unsw.cse.mica.agent.Agent#setTransport(unsw.cse.mica.agent.AgentTransport)
	 */
	public void setTransport(AgentTransport at) {
		this.at = at; 

	}

	/** 
	 * @see unsw.cse.mica.agent.Agent#getTransport()
	 */
	public AgentTransport getTransport() {
		return at; 
	}

	/** 
	 * @see unsw.cse.mica.agent.Agent#init()
	 */
	public void init(MicaProperties args) throws MicaException {
		at.connect("sharedPad");
		List existingLines = at.mobSearch("select * from Mobs where typeOf(Mob,'sharedPadLine')");
		Debug.dp(Debug.FN_CALLS, "Results of search are: " + existingLines.toString());
		for(Iterator i = existingLines.iterator(); i.hasNext();){
			drawSharedLine((Mob) i.next()); 
			
		}
		at.register("sharedPadLine");
	}
	
	public void terminate() throws MicaException {
		at.disconnect(); 	
		System.out.println("Disconnecting ... ");
		
	}

	public static void main(String[] args) throws MicaException {
			String server = args[0]; 
			int portNum = Integer.parseInt(args[1]);
			SharedPad sp = new SharedPad();
			new XMLOverTCPAgentTransport(sp, server, portNum);
			sp.init(null); 
		}

	/**
	 * @return
	 */
	private Component createComponents() {
		JFrame frame = new JFrame("MICA Shared Pad");
		frame.getContentPane().add(this, BorderLayout.CENTER);
		frame.addWindowListener(new MyClosingListener(this));
		setPreferredSize(new Dimension(400,400));
		addMouseMotionListener(new MyMouseMotionListener(this));
		frame.pack();
		frame.setVisible(true); 
		return this;
	}

	/**
	 * Implement the deleted method, doesn't do anything yet. 
	 * 
	 * @see unsw.cse.mica.agent.Agent#handleDeletedMob(unsw.cse.mica.data.Mob)
	 */
	public void handleDeletedMob(Mob m) {}

	/**
	 * Implement the deleted method, doesn't do anything yet. 
	 * 
	 * @see unsw.cse.mica.agent.Agent#handleReplacedMob(unsw.cse.mica.data.Mob, unsw.cse.mica.data.Mob)
	 */
	public void handleReplacedMob(Mob m, Mob m2) {}
	
	/**
	 * Implement the typeManagerChanged method, doesn't do anything yet.
	 * 
	 *  @see unsw.cse.mica.agent.Agent#handleTypeManagerChanged()
	 */
	public void handleTypeManagerChanged() {}
}
