/*
 * Created on Feb 3, 2004
 * 
 * @author mmcgill
 * 
 */
package unsw.cse.mica.demo;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.*;

import unsw.cse.mica.MicaException;
import unsw.cse.mica.agent.Agent;
import unsw.cse.mica.agent.AgentTransport;
import unsw.cse.mica.agent.XMLOverTCPAgentTransport;
import unsw.cse.mica.data.Mob;
import unsw.cse.mica.util.Debug;
import unsw.cse.mica.util.MicaProperties;

/**
 * 
 * A more complete take on the idea of a a shared drawing pad.
 * 
 * @author mmcgill
 * 
 */
public class SharedPad2 implements Agent,ActionListener {
	
	List sessions = null;
	String session = null;
	
	int maxX = 640;
	int maxY = 480;
	
	public void handleNewMob(Mob m) {
		try {
			if (m.getType().equals("sharedPadSession")) {
				newSession(m);
			} else {
				if (session != null && session.equals(m.getSlot1("session")))
					drawObject(m);
					page.repaint();
			}
		} catch (MicaException me) {
			me.printStackTrace();
		}
	}

	private void newSession(Mob m) throws MicaException {
		sessions.add(m);
		String sname = m.getSlot1("name");
		int n = JOptionPane.showConfirmDialog(
			frame,
			"A new session \"" + sname + "\" has been created. Do you wish to join it?",
			"New Session",
			JOptionPane.YES_NO_OPTION);
		//System.err.println("-" + sname);
		if (n == JOptionPane.YES_OPTION) {
			setSession(sname);
		}
	}

	synchronized private void drawObject(Mob m) {
		objects.add(m);
	}

	private void setSession(String sess) throws MicaException {
		if (sess != null) {
			objects = new LinkedList();
			session = sess;
			frame.setTitle("SharedPad2 - " + session);
			//List objects = at.mobSearch("select object from object in sharedPadObject order by object.creationTime;");
			List objects = at.mobSearch("select * from mobs where typeOf(Mob,'sharedPadObject') and getSlot1(Mob, 'session') = '" + session + "' order by getSlot1(Mob,'creationTime')");
			for(Iterator i = objects.iterator(); i.hasNext();){
				Mob m = (Mob)i.next();
				drawObject(m);
			}
			page.revalidate();
			page.repaint();
			enable();
		}
	}

	AgentTransport at;
	
	public void setTransport(AgentTransport at) {
		this.at = at; 
	}

	public AgentTransport getTransport() {
		return at;
	}
	
	JFrame frame;
	JPanel page;
	List objects;
	JPanel cp;
	
	JMenuBar menubar;
	
	JMenu filemenu;
	JMenuItem createSession;
	JMenuItem switchSession;
	JMenuItem importSession;
	JMenuItem exportSession;
	JMenuItem exitmi;
		
	JMenu toolsmenu;
	
	JMenu helpmenu;
	JMenuItem topics;
	JMenuItem about;
	
	JToolBar tools;
	
	JToggleButton select;
	JToggleButton text;
	JToggleButton write;
	JToggleButton line;
	JToggleButton circle;
	JToggleButton fcircle;
	JToggleButton rect;
	JToggleButton frect;

	JPanel lineThickness;
	JToggleButton thick1;
	JToggleButton thick2;
	JToggleButton thick3;
	JToggleButton thick4;
	
	JPanel textFormat;
	JComboBox textFont;
	JComboBox textSize;
	JComboBox textStyle;
	String [] defaultSizes = {"10", "12", "14", "16", "20", "32"};
	
	Component toolglue; 
	
	JToolBar colours;
	Color colour;
	JPanel colourpanel;
	
	Color [] colourpots = {Color.BLACK, Color.WHITE, Color.DARK_GRAY,
		Color.GRAY, Color.LIGHT_GRAY, Color.YELLOW, Color.ORANGE, Color.RED,
		Color.BLUE, Color.GREEN, Color.CYAN, Color.MAGENTA};
	
	Dimension colourSize = new Dimension(60,60);
	Dimension potsize = new Dimension(30,30);
	
	MouseEvent newLocation;
	
	boolean drawTool = false;
	
    public void terminate()
    {
	shutdown();
    }

	public void init(MicaProperties mp) throws MicaException {
		at.connect("sharedPad");
		
		frame = new JFrame("SharedPad2");
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
					shutdown();
				}
		});
		
		filemenu = new JMenu("File");
		createSession = new JMenuItem("New Session");
		createSession.addActionListener(this);
		switchSession = new JMenuItem("Switch Session");
		switchSession.addActionListener(this);
		switchSession.setEnabled(false);
		importSession = new JMenuItem("Import");
		importSession.addActionListener(this);
		importSession.setEnabled(false);
		exportSession = new JMenuItem("Export");
		exportSession.addActionListener(this);
		exportSession.setEnabled(false);
		exitmi = new JMenuItem("Exit");
		exitmi.addActionListener(this);
		filemenu.add(createSession);
		filemenu.add(switchSession);
		filemenu.addSeparator();
		filemenu.add(importSession);
		filemenu.add(exportSession);
		filemenu.addSeparator();
		filemenu.add(exitmi);
		
		toolsmenu = new JMenu("Tools");
		toolsmenu.setEnabled(false);
		
		topics = new JMenuItem("Contents");
		topics.addActionListener(this);
		topics.setEnabled(false);
		about = new JMenuItem("About SharedPad2");
		about.addActionListener(this);
		about.setEnabled(false);
		helpmenu = new JMenu("Help");
		helpmenu.add(topics);
		helpmenu.addSeparator();
		helpmenu.add(about);
		
		menubar = new JMenuBar();
		menubar.add(filemenu);
		menubar.add(toolsmenu);
		menubar.add(Box.createHorizontalGlue());
		menubar.add(helpmenu);
		
		cp = new JPanel();
		cp.setLayout(new BorderLayout());
		
		tools = new JToolBar(SwingConstants.VERTICAL);
		//tools.setLayout(new BoxLayout(tools, BoxLayout.Y_AXIS));
		//tools.setAlignmentX((float)0);
		Debug.dp(Debug.IMPORTANT, "The URL is:" + SharedPad2.class.getResource("images/select.png"));
		
		select = new JToggleButton(new ImageIcon(SharedPad2.class.getResource("images/select.png")));
		select.addActionListener(this);
		select.setEnabled(false);
		text = new JToggleButton(new ImageIcon(SharedPad2.class.getResource("images/text.png")));
		text.addActionListener(this);
		text.setEnabled(false);
		write = new JToggleButton(new ImageIcon(SharedPad2.class.getResource("images/write.png")));
		write.addActionListener(this);
		write.setEnabled(false);
		line = new JToggleButton(new ImageIcon(SharedPad2.class.getResource("images/line.png")));
		line.addActionListener(this);
		line.setEnabled(false);
		circle = new JToggleButton(new ImageIcon(SharedPad2.class.getResource("images/circle.png")));
		circle.addActionListener(this);
		circle.setEnabled(false);
		fcircle = new JToggleButton(new ImageIcon(SharedPad2.class.getResource("images/fcircle.png")));
		fcircle.addActionListener(this);
		fcircle.setEnabled(false);
		rect = new JToggleButton(new ImageIcon(SharedPad2.class.getResource("images/rect.png")));
		rect.addActionListener(this);
		rect.setEnabled(false);
		frect = new JToggleButton(new ImageIcon(SharedPad2.class.getResource("images/frect.png")));
		frect.addActionListener(this);
		frect.setEnabled(false);
		
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
		JPanel tmp = new JPanel();
		tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
		tmp.add(select);
		tmp.add(text);
		buttons.add(tmp);
		tmp = new JPanel();
		tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
		tmp.add(write);
		tmp.add(line);
		buttons.add(tmp);
		tmp = new JPanel();
		tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
		tmp.add(circle);
		tmp.add(fcircle);
		buttons.add(tmp);
		tmp = new JPanel();
		tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
		tmp.add(rect);
		tmp.add(frect);
		buttons.add(tmp);
		
		tools.add(buttons);
		tools.addSeparator();
		
		lineThickness = new JPanel();
		lineThickness.setLayout(new BoxLayout(lineThickness, BoxLayout.Y_AXIS));
		thick1 = new JToggleButton(new ImageIcon(SharedPad2.class.getResource("images/thick1.png")));
		thick1.addActionListener(this);
		thick1.setSelected(true);
		thickness = thick1;
		lineThickness.add(thick1);
		thick2 = new JToggleButton(new ImageIcon(SharedPad2.class.getResource("images/thick2.png")));
		thick2.addActionListener(this);
		lineThickness.add(thick2);
		thick3 = new JToggleButton(new ImageIcon(SharedPad2.class.getResource("images/thick3.png")));
		thick3.addActionListener(this);
		lineThickness.add(thick3);
		thick4 = new JToggleButton(new ImageIcon(SharedPad2.class.getResource("images/thick4.png")));
		thick4.addActionListener(this);
		lineThickness.add(thick4);
		lineThickness.setAlignmentX((float)0.5);
		
		
		textFormat = new JPanel();
		textFormat.setLayout(new BoxLayout(textFormat, BoxLayout.Y_AXIS));
		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String [] fonts = genv.getAvailableFontFamilyNames();
		textFont = new JComboBox(fonts);
		textFont.setSelectedIndex(0);
		textFont.setMaximumSize(new Dimension(100, 30));
		textFont.setPreferredSize(new Dimension(100, 30));
		textFormat.add(textFont);
		textSize = new JComboBox(defaultSizes);
		textSize.setSelectedIndex(0);
		textSize.setEditable(true);
		textSize.setMaximumSize(new Dimension(100, 30));
		textSize.setPreferredSize(new Dimension(100, 30));
		textFormat.add(textSize);
		String [] styles = {"Plain", "Bold", "Italic", "Bold+Italic"};
		textStyle = new JComboBox(styles);
		textStyle.setSelectedIndex(0);
		textStyle.setMaximumSize(new Dimension(100, 30));
		textStyle.setPreferredSize(new Dimension(100, 30));
		textFormat.add(textStyle);
		
		toolglue = Box.createVerticalGlue();
		tools.add(toolglue);
		
		cp.add(tools, BorderLayout.WEST);
		
		colours = new JToolBar(SwingConstants.VERTICAL);
		colour = Color.BLACK;
		colourpanel = new JPanel();
		colourpanel.setBackground(colour);
		colourpanel.setPreferredSize(colourSize);
		colourpanel.setMinimumSize(colourSize);
		colourpanel.setMaximumSize(colourSize);
		colourpanel.setVisible(true);
		colourpanel.setOpaque(true);
		colourpanel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				selectColour();
			}
		});
		tmp = new JPanel();
		tmp.setLayout(new BoxLayout(tmp, BoxLayout.Y_AXIS));
		tmp.add(colourpanel);
		colours.add(tmp);
		
		colours.addSeparator();
		
		JPanel pots = new JPanel();
		pots.setLayout(new BoxLayout(pots, BoxLayout.Y_AXIS));
		for (int i = 0; i < colourpots.length; i++) {
			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			JPanel tmp2 = new JPanel();
			tmp2.setBackground(colourpots[i]);
			tmp2.setPreferredSize(potsize);
			tmp2.setMinimumSize(potsize);
			tmp2.setMaximumSize(potsize);
			tmp2.setVisible(true);
			tmp2.setOpaque(true);
			tmp2.addMouseListener(new ColourListener(colourpots[i], this));
			tmp.add(tmp2);
			i++;
			if (i < colourpots.length) {
				tmp2 = new JPanel();
				tmp2.setBackground(colourpots[i]);
				tmp2.setPreferredSize(potsize);
				tmp2.setMinimumSize(potsize);
				tmp2.setMaximumSize(potsize);
				tmp2.setVisible(true);
				tmp2.setOpaque(true);
				tmp2.addMouseListener(new ColourListener(colourpots[i], this));
				tmp.add(tmp2);
			} else {
				tmp2 = new JPanel();
				tmp2.setPreferredSize(potsize);
				tmp2.setMinimumSize(potsize);
				tmp2.setMaximumSize(potsize);
				tmp2.setVisible(true);
				tmp2.setOpaque(true);
				tmp.add(tmp2);
			}
			pots.add(tmp);
		}
		
		colours.add(pots);
		colours.add(Box.createVerticalGlue());
		
		cp.add(colours, BorderLayout.EAST);
		
		page = new JPanel() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				paintPage(g);
			}
		};
		
		page.setPreferredSize(new Dimension(maxX, maxY));
		page.setOpaque(true);
		page.setBackground(Color.WHITE);
		page.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				oldX = e.getX(); 
				oldY = e.getY();
			}
			
			public void mouseReleased(MouseEvent e) {
				try {
					if (oldX > -1 && oldY > -1 && oldX < maxX && oldY < maxY &&
							e.getX() > -1 && e.getY() > -1 && e.getX() < maxX &&
							e.getY() < maxY) {
						if (tool == line) {
							writeLine(e);
						} else if (tool == text) {
							writeText(e);
						} else if (tool == circle || tool == fcircle) {
							writeElipse(e);
						} else if (tool == rect || tool == frect) {
							writeRectangle(e);
						}
					}
					oldX = -1;
					oldY = -1;
				} catch (MicaException me) {
					me.printStackTrace();
				}
			}
		});
		page.addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseDragged(MouseEvent e) {
				if (tool == write) {
					if (oldX > -1 && oldY > -1 && oldX < maxX && oldY < maxY &&
						e.getX() > -1 && e.getY() > -1 && e.getX() < maxX &&
						e.getY() < maxY) {
						writeLine(e);
					} else if (e.getX() > -1 && e.getY() > -1 &&
								e.getX() < maxX && e.getY() < maxY) {
						oldX = e.getX();
						oldY = e.getY();
						writeLine(e);
					}
				} else if (tool == line || tool == circle || tool == fcircle ||
							tool == rect || tool == frect) {
					if (oldX > -1 && oldY > -1 && oldX < maxX && oldY < maxY &&
						e.getX() > -1 && e.getY() > -1 && e.getX() < maxX &&
						e.getY() < maxY) {
						drawTool = true;
						newLocation = e;
						page.repaint();
					} else {
						drawTool = false;
						page.repaint();
					}
				} else {
					drawTool = false;
				}
			}
		});
		
		cp.add(new JScrollPane(page), BorderLayout.CENTER);
		
		frame.setContentPane(cp);
		frame.setJMenuBar(menubar);
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
		
		at.register("sharedPadSession");
		at.register("sharedPadObject");
		// sessions = at.mobSearch("select session from session in sharedPadSession order by session.creationTime;");
		sessions = at.mobSearch("select * from mobs where typeOf(Mob,'sharedPadSession') order by getSlot1(Mob,'creationTime')");
		
		if (sessions == null || sessions.size() == 0) {
			JOptionPane.showMessageDialog(frame,
				"No open sessions found on the blackboard.",
				"Inane warning",
				JOptionPane.WARNING_MESSAGE);
		} else if (sessions.size() == 1) {
			switchSession.setEnabled(true);
			Mob m = (Mob) sessions.get(0);
			setSession(m.getSlot1("name"));
		} else {
			switchSession.setEnabled(true);
			Object[] possibilities = new Object [sessions.size()];
			for (int i = 0; i < sessions.size(); i++) {
				possibilities[i] = ((Mob)sessions.get(i)).getSlot1("name"); 
			}
			
			String s = (String)JOptionPane.showInputDialog(
								frame,
								"Select the session you'd like to connect to.",
								"Sessions",
								JOptionPane.PLAIN_MESSAGE,
								null,
								possibilities,
								possibilities[0]);
			if (s == null) {
		 		//setSession((String)possibilities[0]);
			} else {
		 		setSession(s);
			}
		}
	}
	
	public void handleTypeManagerChanged() {
		
	}
	
	JToggleButton tool = null;
	JToggleButton thickness = null;
	
	public void actionPerformed(ActionEvent e) {
		try {
			if (e.getSource() instanceof JMenuItem) {
				JMenuItem source = (JMenuItem)e.getSource();
				if (source == exitmi) {
					shutdown();
				} else if (source == createSession) {
					createSession();
				} else if (source == switchSession) {
					changeSession();
				} else if (source == importSession) {
					importSession();
				} else if (source == exportSession) {
					exportSession();
				} else if (source == topics) {
					openHelp();
				} else if (source == about) {
					about();
				}
			} else {
				JToggleButton source = (JToggleButton)e.getSource();
				if (source == select || source == text || source == write ||
						source == line || source == circle || source == fcircle ||
						source == rect || source == frect) {

					if (tool == write || tool == line)
						tools.remove(lineThickness);
					else if (tool == text)
						tools.remove(textFormat);
					if (source == tool) {
						tool = null;
					} else {
						if (tool != null)
							tool.setSelected(false);
						tool = source;
						if (tool == write || tool == line) {
							tools.remove(toolglue);
							tools.add(lineThickness);
							tools.add(toolglue);
						} else if (tool == text) {
							tools.remove(toolglue);
							tools.add(textFormat);
							tools.add(toolglue);
						}
					}
					tools.revalidate();
					tools.repaint();
				} else {
					if (!source.isSelected()) {
						thick1.setSelected(true);
						thickness = thick1;
					} else {
						thickness.setSelected(false);
						thickness = source;
					}
				}
			}
		} catch (MicaException me) {
			me.printStackTrace();
		}
	}
	
	int oldX = -1;
	int oldY = -1;
	
	public void writeLine(MouseEvent e) {
		try {
			Mob m = new Mob("sharedPadLine");
			m.addSlot("session", session);
			m.addSlot("colour", String.valueOf(colour.getRGB()));
			if (thickness == thick4) {
				m.addSlot("thickness", "4");
			} else if (thickness == thick3) {
				m.addSlot("thickness", "3");
			} else if (thickness == thick2) {
				m.addSlot("thickness", "2");
			} else {
				m.addSlot("thickness", "1");
			}
			m.addSlot("oldX", String.valueOf(oldX));
			m.addSlot("oldY", String.valueOf(oldY));
			m.addSlot("newX", String.valueOf(e.getX()));
			m.addSlot("newY", String.valueOf(e.getY()));
			at.writeMob(m);
		
			oldX = e.getX();
			oldY = e.getY();
		} catch (MicaException me) {
			me.printStackTrace();
		}
	}
	
	public void writeText(MouseEvent e) throws MicaException {
		String s = (String)JOptionPane.showInputDialog(
					frame,
					"What text would you like to add?",
					"Enter text",
					JOptionPane.PLAIN_MESSAGE,
					null,
					null,
					null);
		if (s != null && !s.trim().equals("")) {
			Mob m = new Mob("sharedPadText");
			m.addSlot("session", session);
			m.addSlot("colour", String.valueOf(colour.getRGB()));
			m.addSlot("oldX", String.valueOf(e.getX()));			m.addSlot("oldY", String.valueOf(e.getY()));
			m.addSlot("font", (String) textFont.getSelectedItem());
			m.addSlot("size", (String) textSize.getSelectedItem());
			m.addSlot("utterance", s);
			m.addSlot("style", (String) textStyle.getSelectedItem());
			at.writeMob(m);
		}
	}
	
	public void writeElipse(MouseEvent e) throws MicaException {
		Mob m = new Mob("sharedPadElipse");
		m.addSlot("session", session);
		m.addSlot("colour", String.valueOf(colour.getRGB()));
		if (oldX < e.getX()) {
			m.addSlot("oldX", String.valueOf(oldX));
			m.addSlot("newX", String.valueOf(e.getX()));
		} else {
			m.addSlot("oldX", String.valueOf(e.getX()));
			m.addSlot("newX", String.valueOf(oldX));
		}
		if (oldY < e.getY()) {
			m.addSlot("oldY", String.valueOf(oldY));
			m.addSlot("newY", String.valueOf(e.getY()));
		} else {
			m.addSlot("oldY", String.valueOf(e.getY()));
			m.addSlot("newY", String.valueOf(oldY));
		}
		m.addSlot("fill", tool == fcircle ? "yes": "no");
		at.writeMob(m);
	}
	
	public void writeRectangle(MouseEvent e) throws MicaException {
		Mob m = new Mob("sharedPadRectangle");
		m.addSlot("session", session);
		m.addSlot("colour", String.valueOf(colour.getRGB()));
		if (oldX < e.getX()) {
			m.addSlot("oldX", String.valueOf(oldX));
			m.addSlot("newX", String.valueOf(e.getX()));
		} else {
			m.addSlot("oldX", String.valueOf(e.getX()));
			m.addSlot("newX", String.valueOf(oldX));
		}
		if (oldY < e.getY()) {
			m.addSlot("oldY", String.valueOf(oldY));
			m.addSlot("newY", String.valueOf(e.getY()));
		} else {
			m.addSlot("oldY", String.valueOf(e.getY()));
			m.addSlot("newY", String.valueOf(oldY));
		}
		m.addSlot("fill", tool == frect ? "yes": "no");
		at.writeMob(m);
	}
	
	private void shutdown() {
		try {
			at.disconnect();
		} catch (MicaException me) {
			me.printStackTrace();
		}
		//at.unregister("sharedPadSession");
		//at.unregister("sharedPadObject");
		System.exit(0);
	}
	
	private void createSession() throws MicaException {
		String s = (String)JOptionPane.showInputDialog(
			frame,
			"What name would you like to give to the new session",
			"New Session",
			JOptionPane.PLAIN_MESSAGE,
			null,
			null,
			null);
		if (s == null || s.trim().equals("")) {
			JOptionPane.showMessageDialog(frame,
				"You didn't supply a name for the session.",
				"Invalid session name",
				JOptionPane.WARNING_MESSAGE);
		} else {
			s = s.trim();
			boolean found = false;
			for (int i = 0; !found && i < sessions.size();i++) {
				if (s.equals(((Mob)sessions.get(i)).getSlot1("name")))
					found = true;
			}
			if (found) {
				int n = JOptionPane.showConfirmDialog(
					frame,
					"That session already exists. Do you wish to join it?",
					"New Session Exists",
					JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.YES_OPTION) {
					setSession(s);
				}
			} else {
				Mob m = new Mob("sharedPadSession");
				m.addSlot("name", s);
				at.writeMob(m);
			}
		}
	}
	
	private void changeSession() throws MicaException {
		if (sessions != null && sessions.size() > 1) {
			Object[] possibilities = new Object [sessions.size()];
			for (int i = 0; i < sessions.size(); i++) {
				possibilities[i] = ((Mob)sessions.get(i)).getSlot1("name"); 
			}
	
			String s = (String)JOptionPane.showInputDialog(
								frame,
								"Select the session you'd like to connect to.",
								"Sessions",
								JOptionPane.PLAIN_MESSAGE,
								null,
								possibilities,
								possibilities[0]);
			if (s != null)
				setSession(s);
		}
	}
	
	final JFileChooser fc = new JFileChooser();
	
	private void importSession() {
		int returnVal = fc.showOpenDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (file.exists()) {
				try {
					ObjectInputStream is = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file))); 
					List l = (List) is.readObject();
					//System.err.println(l);
					LinkedList ll = new LinkedList();
					ll.add(session);
					LinkedList ll2 = new LinkedList();
					for (Iterator i = l.iterator(); i.hasNext();) {
						Mob m = (Mob) i.next();
						m.setSlot("session", ll);
						m.setSlot("creationTime", ll2);
						at.writeMob(m);
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(frame,
						"An exception occured in trying to read from the file \"" + file.getPath() + "\".",
						"Unable to Read File",
						JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
	
	private void exportSession() {
		int returnVal = fc.showOpenDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (file.exists()) {
				int n = JOptionPane.showConfirmDialog(
					frame,
					"The file \"" + file.getPath() + "\" already exists. Do you wish to overwrite it?",
					"File Exists",
					JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.NO_OPTION) {
					return;
				}
			}
			try {
				writeFile(file);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(frame,
					"An exception occured in trying to write to the file \"" + file.getPath() + "\".",
					"Unable to Write File",
					JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	synchronized private void writeFile(File f) throws IOException {
		ObjectOutputStream os = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(f)));
		os.writeObject(objects);
		os.flush(); 
		os.close(); 
	}
	
	private void openHelp() {
		// TODO help
	}
	
	private void about() {
		// TODO about
	}
	
	private boolean enabled = false;
	private void enable() {
		if (!enabled) {
			switchSession.setEnabled(true);
			importSession.setEnabled(true);
			exportSession.setEnabled(true);
			text.setEnabled(true);
			write.setEnabled(true);
			line.setEnabled(true);
			circle.setEnabled(true);
			fcircle.setEnabled(true);
			rect.setEnabled(true);
			frect.setEnabled(true);
			enabled = true;
		}
	}
	
	synchronized public void paintPage(Graphics g) {
		if (objects != null) {
			for (Iterator i = objects.listIterator(); i.hasNext();) {
				Mob m = (Mob)i.next();
				g.setColor(new Color(Integer.parseInt(m.getSlot1("colour"))));
				if (m.getType().equals("sharedPadLine")) {
					int startX = Integer.parseInt(m.getSlot1("oldX")); 
					int startY = Integer.parseInt(m.getSlot1("oldY"));
					int endX = Integer.parseInt(m.getSlot1("newX"));
					int endY = Integer.parseInt(m.getSlot1("newY"));
					g.drawLine(startX, startY, endX, endY);
					int thick = Integer.parseInt(m.getSlot1("thickness"));
					int diffX = endX > startX ? endX - startX : startX - endX;
					int diffY = endY > startY ? endY - startY : startY - endY;
					if (diffX > diffY) {
						for (int j = 1; j < thick; j++) {
							if (j % 2 == 0) {
								g.drawLine(startX, startY - j/2, endX, endY - j/2);
							} else {
								g.drawLine(startX, startY + (j+1)/2, endX, endY + (j+1)/2);
							}
						}
					} else {
						for (int j = 1; j < thick; j++) {
							if (j % 2 == 0) {
								g.drawLine(startX - j/2, startY, endX - j/2, endY);
							} else {
								g.drawLine(startX + (j+1)/2, startY, endX + (j+1)/2, endY);
							}
						}
					}
				} else if (m.getType().equals("sharedPadElipse")) {
					int startX = Integer.parseInt(m.getSlot1("oldX")); 
					int startY = Integer.parseInt(m.getSlot1("oldY"));
					int endX = Integer.parseInt(m.getSlot1("newX"));
					int endY = Integer.parseInt(m.getSlot1("newY"));
					if (m.getSlot1("fill").equals("yes")) {
						g.fillOval(startX, startY, endX - startX, endY - startY);
					} else {
						g.drawOval(startX, startY, endX - startX, endY - startY);
					}
				}  else if (m.getType().equals("sharedPadRectangle")) {
					int startX = Integer.parseInt(m.getSlot1("oldX")); 
					int startY = Integer.parseInt(m.getSlot1("oldY"));
					int endX = Integer.parseInt(m.getSlot1("newX"));
					int endY = Integer.parseInt(m.getSlot1("newY"));
					if (m.getSlot1("fill").equals("yes")) {
						g.fillRect(startX, startY, endX - startX, endY - startY);
					} else {
						g.drawRect(startX, startY, endX - startX, endY - startY);
					}
				} else if (m.getType().equals("sharedPadText")) {
					int startX = Integer.parseInt(m.getSlot1("oldX")); 
					int startY = Integer.parseInt(m.getSlot1("oldY"));
					String font = m.getSlot1("font");
					int size = Integer.parseInt(m.getSlot1("size"));
					String text = m.getSlot1("utterance");
					int style = Font.PLAIN;
					String sstyle = m.getSlot1("style");
					if (sstyle.equals("Bold")) {
						style = Font.BOLD;
					} else if (sstyle.equals("Italic")) {
						style = Font.ITALIC;
					} else if (sstyle.equals("Bold+Italic")) {
						
						style = Font.BOLD + Font.ITALIC;
					}
					g.setFont(new Font(font, style, size));
					g.drawString(text, startX, startY);
				}
			}
		}
		if (drawTool) {
			g.setColor(Color.BLACK);
			if (tool == line) {
				g.drawLine(oldX, oldY, newLocation.getX(), newLocation.getY());
			} else {
				int newX = newLocation.getX(), newY = newLocation.getY();  
				if (tool == circle || tool == fcircle) {
					if (oldX < newX) {
						if (oldY < newY) {
							g.drawOval(oldX, oldY, newX - oldX, newY - oldY);
						} else {
							g.drawOval(oldX, newY, newX - oldX, oldY - newY);
						}
					} else {
						if (oldY < newY) {
							g.drawOval(newX, oldY, oldX - newX, newY - oldY);
						} else {
							g.drawOval(newX, newY, oldX - newX, oldY - newY);
						}
					}
				} else if (tool == rect || tool == frect) {
					if (oldX < newX) {
						if (oldY < newY) {
							g.drawRect(oldX, oldY, newX - oldX, newY - oldY);
						} else {
							g.drawRect(oldX, newY, newX - oldX, oldY - newY);
						}
					} else {
						if (oldY < newY) {
							g.drawRect(newX, oldY, oldX - newX, newY - oldY);
						} else {
							g.drawRect(newX, newY, oldX - newX, oldY - newY);
						}
					}
				}
			}
			drawTool = false;
		}
	}
	
	public void selectColour() {
		Color c = JColorChooser.showDialog(frame, "Select Colour", colour);
		if (c != null)
			setColour(c);
	}
	
	public void setColour(Color c) {
		colour = c;
		colourpanel.setBackground(colour);
		colourpanel.repaint();
	}
	
	static String defaultServer = "localhost";
	static int defaultPort = 8500;
	
	public static void main(String[] args) throws MicaException {
		String server; 
		int portNum;
		if (args.length == 0) {
			server = defaultServer;
		} else {
			server = args[0];
		}
		if (args.length < 2) {
			portNum = defaultPort;
		} else {
			portNum = Integer.parseInt(args[1]);
		}
		try {
			UIManager.setLookAndFeel(
				UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) { }
			
		SharedPad2 sp2 = new SharedPad2();
		new XMLOverTCPAgentTransport(sp2, server, portNum);
		sp2.init(null); 
	}

	/* (non-Javadoc)
	 * @see unsw.cse.mica.agent.Agent#handleDeletedMob(unsw.cse.mica.data.Mob)
	 */
	public void handleDeletedMob(Mob m) {
	}

	/**
	 * Implement the deleted method, doesn't do anything yet. 
	 * 
	 * @see unsw.cse.mica.agent.Agent#handleReplacedMob(unsw.cse.mica.data.Mob, unsw.cse.mica.data.Mob)
	 */
	public void handleReplacedMob(Mob m, Mob m2) {}
}

class ColourListener extends MouseAdapter {
	
	Color colour;
	SharedPad2 sp;
	
	ColourListener(Color c, SharedPad2 sp2) {
		colour = c;
		sp = sp2;
	}
	
	public void mouseClicked(MouseEvent e) {
		sp.setColour(colour);
	}
}
