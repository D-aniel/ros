package unsw.cse.framescript.pilotsim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public class PilotUI implements NLPListener {
	static final Color skyBlue = new Color(100, 200, 255);
	static final Color groundGreen = new Color(0, 150, 0);
	static final Color mountainColor = new Color(115, 84, 0);
	static final Color houseWall = new Color(255, 0, 0);
	static final Color houseRoof = new Color(200, 200, 0);
	static final Color textColor = new Color(0, 0, 0);
	
	Pilot pilot;
	PilotNLP nlp;
	
	JFrame frame;
	PilotCanvas canvas;
	JTextPane history;
	JScrollPane historyScroll;
	StyledDocument doc;
	JTextField inputField;
	
	Style instructionsStyle;
	Style inputStyle;
	Style responseStyle;
	
	ArrayList previous;
	int previousindex = -1;
	
	PilotUI(Pilot aPilot) {
		this.pilot = aPilot;
		nlp = new PilotNLP(aPilot);
		nlp.addListener(this);
		frame = new JFrame("Dodgy Pilot Sim");
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				closeWindow();
			}
		});
		
		canvas = new PilotCanvas(pilot);
		history = new JTextPane();
		history.setEditable(false);
		history.setPreferredSize(new Dimension(550, 150));
		doc = history.getStyledDocument();
		
		inputStyle = doc.addStyle("input", StyleContext.getDefaultStyleContext().
							getStyle(StyleContext.DEFAULT_STYLE));
		StyleConstants.setFontSize(inputStyle, 20);
		responseStyle = doc.addStyle("response", inputStyle);
		StyleConstants.setForeground(responseStyle, Color.BLUE);
		instructionsStyle = doc.addStyle("instructions", inputStyle);
		StyleConstants.setForeground(instructionsStyle, new Color(0, 190, 0));
		
		historyScroll = new JScrollPane(history);
		historyScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		history.addComponentListener(new ComponentAdapter(){
			public void componentResized(ComponentEvent e) {
				history.scrollRectToVisible(new Rectangle(0, history.getHeight() - 1, 1 ,1));
			}
		});
		inputField = new JTextField();
		inputField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String str = inputField.getText().trim();
				if (!str.equals("")) {
					processInput(str);
					inputField.setText("");
				}
				previous.add(str);
				previousindex = -1;
			}
		});
		inputField.addKeyListener(new KeyListener() {
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
		inputField.setFont(inputField.getFont().deriveFont((float)20));
		previous = new ArrayList();
		JPanel cp = new JPanel();
		cp.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.fill = 1;
		c.weightx = 1;
		c.weighty = 2;
		cp.add(canvas, c);
		c.weighty = 1;
		c.gridy++;
		cp.add(historyScroll, c);
		c.weighty = 0;
		c.gridy++;
		cp.add(inputField, c);
		frame.setContentPane(cp);
		frame.pack();
		frame.setVisible(true);
		inputField.requestFocus();
		
		displayMessage("Understood commands:", instructionsStyle);
		displayMessage("   change heading to <heading>", instructionsStyle);
		displayMessage("   increase/decrease altitude to <altitude>", instructionsStyle);
		displayMessage("   increase/decrease speed to <speed>", instructionsStyle);
		displayMessage("   set course <heading> altitude <altitude> speed <speed>", instructionsStyle);
	}
	
	synchronized void displayMessage(String message, Style style) {
		try {
			doc.insertString(doc.getLength(), message + "\n", style);
		} catch (BadLocationException e) {
		}
	}
	
	public void processInput(String input) {
		processInput(input, null);
	}
	
	public void processInput(String input, String [] alternatives) {
		displayMessage(input, inputStyle);
		nlp.processInput(input, alternatives);
	}

	public void responseGiven(String text) {
		displayMessage(text, responseStyle);
	}
	
	synchronized void onKeyUp() {
		if (previous.size() == 0) {
			return;
		}
		if (previousindex == -1 || previousindex == 0) {
			previousindex = previous.size();
		}
		previousindex--;
		inputField.setText((String) previous.get(previousindex));
	}
	
	synchronized void onKeyDown() {
		if (previous.size() == 0) {
			return;
		}
		previousindex++;
		if (previousindex == previous.size()) {
			previousindex = 0;
		}
		inputField.setText((String) previous.get(previousindex));
	}
	
	void closeWindow() {
		pilot.terminate();
		frame.setVisible(false);
		frame.dispose();
	}
	
	public static void main(String [] args) {
		Pilot p = new Pilot();
		new PilotUI(p);
		p.startSimulation();
	}
	
	class PilotCanvas extends JComponent implements PilotListener {
		Pilot pilot;
		Font font;
		PilotCanvas(Pilot pilot) {
			this.pilot = pilot;
			pilot.addListener(this);
			setPreferredSize(new Dimension(550, 300));
		}
		
		public void paint(Graphics g) {
			super.paint(g);
			int height = getHeight();
			int width = getWidth();
			int horizon = (int)(height * (1 + (1 - Math.exp(-(pilot.currentAltitude / 7000.0)))) / 2);
//			System.err.println("height - " + height + ", horizon - " + horizon);
			g.setColor(skyBlue);
			g.fillRect(0, 0, width, height);
			g.setColor(groundGreen);
			g.fillRect(0, horizon, width, height - horizon);
			
			// Paint obstacles
			int i = 0, j = 0;
			Pilot.Mountain [] mounts = pilot.mountains;
			Pilot.House [] houses = pilot.houses;
			while (i < mounts.length || j < houses.length) {
				if (i == mounts.length) {
					paintHouse(houses[j], g, horizon);
					j++;
				} else if (j == houses.length) {
					paintMount(mounts[i], g, horizon);
					i++;
				} else if (mounts[i].distance < houses[j].distance) {
					paintHouse(houses[j], g, horizon);
					j++;
				} else {
					paintMount(mounts[i], g, horizon);
					i++;
				}
			}
			
			// Paint status
			if (font == null) {
				font = g.getFont();
				font = font.deriveFont(Font.BOLD, 24);
			}
			g.setColor(textColor);
			g.setFont(font);
			FontMetrics fm = g.getFontMetrics(font);
			String str = pilot.currentHeading + "";
			Rectangle2D r = fm.getStringBounds(str, g);
			g.drawString(str, (int)(width - r.getWidth()) / 2, (int)r.getHeight() + 5);
			str = pilot.currentAltitude + "";
			r = fm.getStringBounds(str, g);
			g.drawString(str, width - (int)r.getWidth() - 5, (height + (int)r.getHeight()) / 2);
			str = pilot.currentSpeed + "";
			r = fm.getStringBounds(str, g);
			g.drawString(str, 5, height - 5);
		}
		
		void paintHouse(Pilot.House house, Graphics g, int horizon) {
			int height = getHeight();
			int hLoc = (360 + 30 + house.heading - pilot.currentHeading) % 360;
			if (hLoc <= 60) {
				hLoc = (hLoc * getWidth()) / 60;
				int vLoc = (int)(height - house.distance * (height - horizon));
				g.setColor(houseWall);
				g.fillRect(hLoc - 10, vLoc - 21, 21, 21);
				int [] xs = {hLoc - 15, hLoc, hLoc + 15};
				int [] ys = {vLoc - 21, vLoc - 30, vLoc - 21};
				g.setColor(houseRoof);
				g.fillPolygon(xs, ys, 3);
			}
		}
		
		void paintMount(Pilot.Mountain mount, Graphics g, int horizon) {
			int height = getHeight();
			int width = getWidth();
			int hLoc = (360 + 30 + mount.heading - pilot.currentHeading) % 360;
			if (hLoc > 180)
				hLoc -= 360;
			hLoc = (hLoc * width) / 60;
			int baseLoc = (int)(height - mount.distance * (height - horizon));
			int topLoc = height - height * mount.height / ((pilot.currentAltitude == 0?1:pilot.currentAltitude) * 2);
			int baseLen = width * mount.width / 60;
			if (topLoc >= baseLoc - 2)
				topLoc = baseLoc - 3;
			int [] xs = {hLoc - baseLen / 2, hLoc, hLoc + baseLen / 2};
			int [] ys = {baseLoc, topLoc, baseLoc};
//			System.err.println("height - " + height + ", horizon - " + horizon + ", baseloc - " + baseLoc + ", topLoc - " + topLoc);
			g.setColor(mountainColor);
			g.fillPolygon(xs, ys, 3);
		}
		
		public void courseChanged() {
			repaint();
		}
		
		public void alteringCourse(String reason) {}
	}
}
