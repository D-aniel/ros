package unsw.cse.mica.demo;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.overlay.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import unsw.cse.mica.MicaException;
import unsw.cse.mica.TransportException;
import unsw.cse.mica.agent.AgentTransport;
import unsw.cse.mica.agent.GUIAgent;
import unsw.cse.mica.agent.XMLOverTCPAgentTransport;
import unsw.cse.mica.data.Mob;
import unsw.cse.mica.util.ListProperties;
import unsw.cse.mica.util.MicaConstants;
import unsw.cse.mica.util.MicaProperties;

/**
 * Shows a given image and provides fields for writing/displaying text mobs. Responds
 * to draw mobs by drawing marks over the image.
 * 
 * @author mmcgill
 * 
 */
public class MapAgent extends GUIAgent implements MouseListener, MouseMotionListener {
	public static final String TYPE_DRAW = "draw";
	public static final String TYPE_DRAWCLEAN = "drawClean";
	public static final String TYPE_DRAWLINE = "drawLine";
	public static final String TYPE_DRAWRECT = "drawRect";
	public static final String TYPE_DRAWOVAL = "drawOval";
	public static final String TYPE_DRAWPOLYGON = "drawPolygon";
	public static final String TYPE_DRAWSTRING = "drawString";
	public static final String TYPE_GESTURE = "gesture";
	public static final String TYPE_CLICK = "click";
	public static final String TYPE_CIRCLEGESTURE = "circleGesture";
	public static final String TYPE_TEXT = "text";
	public static final String TYPE_TEXTFORUSER = "textForUser";
	public static final String TYPE_TEXTFROMUSER = "textFromUser";
	public static final String TYPE_LOCATION = "location";
	
	public static final String SLOT_X = "x";
	public static final String SLOT_Y = "y";
	public static final String SLOT_X2 = "x2";
	public static final String SLOT_Y2 = "y2";
	public static final String SLOT_WIDTH = "width";
	public static final String SLOT_HEIGHT = "height";
	public static final String SLOT_COLOUR = "colour";
	public static final String SLOT_FILL = "fill";
	public static final String SLOT_XPOINTS = "xPoints";
	public static final String SLOT_YPOINTS = "yPoints";
	public static final String SLOT_STRING = "string";
	public static final String SLOT_UTTERANCE = "utterance";
	
	
	public static String PARAM_NAME = "name";
	public static String PARAM_MAP = "map";
	Overlay overlay;

	ArrayList previous;
	int previousindex;
	
	boolean transientOutput = true;
	
	JTextPane history;
	JScrollPane historyscroll;
	StyledDocument doc;
	JTextField inputfield;
	Style response;
	Style input;
	Style otherInput;
	String agentName;
	Point location;
	
	public void init(MicaProperties args) throws MicaException {
		super.init(args);
		agentName = at.getAgentName();
		at.register(TYPE_DRAW);
		at.register(TYPE_TEXT);
		at.register(TYPE_LOCATION);
		String query = "select * from mobs where typeOf(Mob,'" + TYPE_LOCATION +
		"') order by getSlot1(Mob,'creationTime') desc";
		List l = at.mobSearch(query);
		if (l.size() > 0) {
			handleNewMob((Mob)l.get(0));
		}
	}
	
	void setColour(String colour) {
		colour = colour.toLowerCase();
		if (colour.equals("blue"))
			overlay.setColor(Color.BLUE);
		else if (colour.equals("cyan"))
			overlay.setColor(Color.CYAN);
		else if (colour.equals("dark gray"))
			overlay.setColor(Color.DARK_GRAY);
		else if (colour.equals("gray"))
			overlay.setColor(Color.GRAY);
		else if (colour.equals("green"))
			overlay.setColor(Color.GREEN);
		else if (colour.equals("light gray"))
			overlay.setColor(Color.LIGHT_GRAY);
		else if (colour.equals("magenta"))
			overlay.setColor(Color.MAGENTA);
		else if (colour.equals("orange"))
			overlay.setColor(Color.ORANGE);
		else if (colour.equals("pink"))
			overlay.setColor(Color.PINK);
		else if (colour.equals("red"))
			overlay.setColor(Color.RED);
		else if (colour.equals("white"))
			overlay.setColor(Color.WHITE);
		else if (colour.equals("yellow"))
			overlay.setColor(Color.YELLOW);
		else
			overlay.setColor(Color.BLACK);
	}
	
	void cleanOverlay() {
		overlay.cleanOverlay();
		drawLocation();
	}
	
	void drawLocation() {
		if (location != null) {
			overlay.setColor(new Color(128, 0, 128));
			overlay.fillOval(location.x-4, location.y-4, 9, 9);
		}
	}
	
	public synchronized void handleNewMob(Mob m) {
//		System.err.println("got mob: " + m.getType());
		String type = m.getType();
		if (type.equals(TYPE_DRAWCLEAN))
			cleanOverlay();
		else if (type.equals(TYPE_LOCATION)) {
			location = new Point(m.getSlot1AsInt(SLOT_X), m.getSlot1AsInt(SLOT_Y));
			drawLocation();
		} else if (type.equals(TYPE_TEXTFORUSER) || type.equals(TYPE_TEXTFROMUSER)) {
			String utterance = m.getSlot1(SLOT_UTTERANCE);
			try {
				Style style = response;
				if (m.getType().equals("textFromUser")) {
					if (agentName.equals(m.getSlot1("creator")))
						style = input;
					else
						style = otherInput;
				}
				doc.insertString(doc.getLength(), utterance + "\n", style);
				history.scrollRectToVisible(new Rectangle(0, history.getSize().height, 1, 20));
			} catch (BadLocationException e) {
				showException(e);
			}
			frame.repaint();
		} else {
			if (m.hasSlot(SLOT_COLOUR))
				setColour(m.getSlot1(SLOT_COLOUR));
			boolean fill = false;
			int x = 0, y = 0;
			if (m.hasSlot(SLOT_FILL))
				fill = m.getSlot1(SLOT_FILL).toLowerCase().equals("true");
			if (m.hasSlot(SLOT_X))
				x = m.getSlot1AsInt(SLOT_X);
			if (m.hasSlot(SLOT_Y))
				y = m.getSlot1AsInt(SLOT_Y);
			if (type.equals(TYPE_DRAWOVAL)) {
				int width = m.getSlot1AsInt(SLOT_WIDTH);
				int height = m.getSlot1AsInt(SLOT_HEIGHT);
				if (fill)
					overlay.fillOval(x, y, width, height);
				else
					overlay.drawOval(x, y, width, height);
			} else if (type.equals(TYPE_DRAWPOLYGON)) {
				List xs = m.getSlot(SLOT_XPOINTS);
				List ys = m.getSlot(SLOT_YPOINTS);
				int numPoints = (xs.size() < ys.size())?xs.size():ys.size();
				int [] xPoints = new int [numPoints];
				int [] yPoints = new int [numPoints];
				for (int i = 0; i < numPoints; i++) {
					xPoints[i] = Integer.parseInt((String)xs.get(i));
					yPoints[i] = Integer.parseInt((String)ys.get(i));
				}
				if (fill)
					overlay.fillPolygon(xPoints, yPoints, numPoints);
				else
					overlay.drawPolygon(xPoints, yPoints, numPoints);
			} else if (type.equals(TYPE_DRAWRECT)) {
				int width = m.getSlot1AsInt(SLOT_WIDTH);
				int height = m.getSlot1AsInt(SLOT_HEIGHT);
				if (fill)
					overlay.fillRect(x, y, width, height);
				else
					overlay.drawRect(x, y, width, height);
			} else if (type.equals(TYPE_DRAWLINE)) {
				int x2 = m.getSlot1AsInt(SLOT_X2);
				int y2 = m.getSlot1AsInt(SLOT_Y2);
				overlay.drawLine(x, y, x2, y2);
			} else if (type.equals(TYPE_DRAWSTRING)) {
				String str = m.getSlot1(SLOT_STRING);
				overlay.drawString(str, x, y);
			}
		}
	}

	public void createComponents(MicaProperties args) throws MicaException {
		OverlayPanel mapPanel = new OverlayPanel(new BorderLayout());
		URL mapURL;
		if (args.hasParam(PARAM_MAP))
			mapURL = getClass().getClassLoader().getResource(args.getValue(PARAM_MAP));
		else
			mapURL = getClass().getClassLoader().getResource(
					"unsw/cse/mica/demo/images/kensington_campus.png");
		if (mapURL != null)
			mapPanel.add(new JLabel(new ImageIcon(mapURL)), BorderLayout.CENTER);
		overlay = new Overlay(mapPanel);
		mapPanel.setOverlay(overlay);
		mapPanel.addMouseListener(this);
		mapPanel.addMouseMotionListener(this);
		
		previous = new ArrayList();
		previousindex = -1;
		history = new JTextPane();
		history.setEditable(false);
		doc = history.getStyledDocument();
		historyscroll = new JScrollPane(history);
		historyscroll.setPreferredSize(new Dimension(450, 250));
		inputfield = new JTextField();
		inputfield.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				newSentence();
			}			
		});
		
		inputfield.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {}
		    public void keyPressed(KeyEvent e) {
		    	
		    	if (e.getKeyCode() == KeyEvent.VK_UP) {
					onKeyUp();
				} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					onKeyDown();
				}
		    }
		    
		    public void keyReleased(KeyEvent e) {}
		});
		
		response = doc.addStyle("response", StyleContext.getDefaultStyleContext().
							getStyle(StyleContext.DEFAULT_STYLE));
		StyleConstants.setFontSize(response, 12);
		input = doc.addStyle("input", response);
		StyleConstants.setForeground(input, Color.BLUE);
		otherInput = doc.addStyle("otherInput", input);
		StyleConstants.setForeground(otherInput, Color.GREEN);
		
		JPanel cp = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = 1;
		cp.add(mapPanel, c);
		c.weightx = c.weighty = c.gridy = 1;
		cp.add(historyscroll, c);
		c.weighty = 0;
		c.gridy = 2;
		cp.add(inputfield, c);
		frame.setContentPane(cp);
		if (!args.hasParam(PARAM_NAME))
			frame.setTitle("Map Agent");
		frame.pack();
		inputfield.requestFocus();
	}
	
	public void mouseClicked(MouseEvent e) {
		System.err.println("click " + e.getX() + " " + e.getY());
		if (e.getButton() == MouseEvent.BUTTON1) {
			overlay.setColor(Color.RED);
			overlay.fillOval(e.getX() - 3, e.getY() - 3, 7, 7);
			Mob m = new Mob(TYPE_CLICK);
			m.addSlot("x", e.getX() + "");
			m.addSlot("y", e.getY() + "");
			try {
				at.writeMob(m);
			} catch (TransportException e1) {
				showException(e1);
			}
		}
	}
	
	int dragX, dragY;
	Vector points = new Vector();
	public void mouseDragged(MouseEvent e) {
		
		if ((e.getModifiersEx() | MouseEvent.BUTTON1_DOWN_MASK) != 0) {
			overlay.setColor(Color.BLUE);
			overlay.drawLine(dragX, dragY, e.getX(), e.getY());
			dragX = e.getX();
			dragY = e.getY();
			points.add(new Point(dragX, dragY));
		}
	}
	
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			dragX = e.getX();
			dragY = e.getY();
			points.removeAllElements();
			points.add(new Point(dragX, dragY));
		}
	}
	
	public void mouseReleased(MouseEvent e) {
		int X = 0, Y = 0, numPoints;
		for (numPoints = 0; numPoints < points.size(); numPoints++) {
			Point p = (Point)points.get(numPoints);
			X += p.x;
			Y += p.y;
		}
		if (numPoints > 1) {
			X = X / numPoints;
			Y = Y / numPoints;
			Mob m = new Mob(TYPE_CIRCLEGESTURE);
			m.addSlot(SLOT_X, X + "");
			m.addSlot(SLOT_Y, Y + "");
			m.addSlot("numPoints", numPoints + "");
			try {
				at.writeMob(m);
			} catch (TransportException e1) {
				showException(e1);
			}
//			overlay.setColor(Color.RED);
//			overlay.drawOval(X - 4, Y - 4, 9, 9);
		}
	}
	
	public void mouseEntered(MouseEvent e) {
	}
	
	public void mouseExited(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}
	
	synchronized void newSentence() {
		String str = inputfield.getText().trim();
		if (!str.equals(""))
		{
			Mob m = new Mob("textFromUser");
			m.addSlot("utterance", str);
			if (transientOutput)
				m.setPersistence(MicaConstants.PERSISTENCE_TRANSIENT);
			try {
				at.writeMob(m);
			} catch (MicaException e) {
				showException(e);
			}
			inputfield.setText("");
			previous.add(str);
			previousindex = -1;
		}
	}
	
	synchronized void onKeyUp() {
		if (previous.size() == 0) {
			return;
		}
		if (previousindex == -1 || previousindex == 0) {
			previousindex = previous.size();
		}
		previousindex--;
		inputfield.setText((String) previous.get(previousindex));
	}
	
	synchronized void onKeyDown() {
		if (previous.size() == 0) {
			return;
		}
		previousindex++;
		if (previousindex == previous.size()) {
			previousindex = 0;
		}
		inputfield.setText((String) previous.get(previousindex));
	}
	
	void showException(Exception e) {
		JOptionPane.showMessageDialog(frame, e.getMessage(),
				e.getClass().getSimpleName(),
				JOptionPane.ERROR_MESSAGE);
	}
	
	static String server = "localhost";
	static int port = 8500;
	
	public static void main(String [] args) throws MicaException {
		if (args.length > 2) {
			System.err.println("usage: java MapAgent [server [port]]");
			System.exit(0);
		}
		if (args.length > 0) {
			server = args[0];
			if (args.length == 2) {
				port = Integer.parseInt(args[1]);
			}
		}
		MapAgent ma = new MapAgent();
		AgentTransport xat = new XMLOverTCPAgentTransport(ma, server, port);
		ma.setTransport(xat);
		try {
			ma.init(new ListProperties());
		} catch (MicaException e) {
			e.printStackTrace();
		}
	}

}

