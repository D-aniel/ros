package unsw.cse.mica.demo;


import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import unsw.cse.mica.MicaException;
import unsw.cse.mica.TransportException;
import unsw.cse.mica.agent.GUIAgent;
import unsw.cse.mica.data.Mob;
import unsw.cse.mica.util.MicaProperties;

/**
 * 
 * A simple GUI for creating and maintaining landmark mobs.
 * 
 * @author mmcgill
 */
public class LandmarkAgent extends GUIAgent {
	public static String TYPE_LANDMARK = "landmark";
	public static String TYPE_DRAWCLEAN = "drawClean";
	public static String TYPE_DRAWPOLYGON = "drawPolygon";
	
	public static String SLOT_NAME = "name";
	public static String SLOT_PATTERN = "pattern";
	public static String SLOT_DESCRIPTION = "description";
	public static String SLOT_COLOUR = "colour";
	public static String SLOT_XPOINTS = "xPoints";
	public static String SLOT_YPOINTS = "yPoints";
	public static String SLOT_CREATOR = "creator";
	
	JList landmarks;
	JTextField name, pattern, x, y;
	JTextArea description;
	JList bounds;
	
	JButton showAll, toFRS, clearMap, newLandmark, show, accept, apply,
		up, down, add, remove;
	
	String agentName;
	
	Landmark current = null;
	Point currentPoint = null;
	
	public void init(MicaProperties args) throws MicaException {
		super.init(args);
		String query = 
			"select * from mobs where typeOf(Mob,'" + TYPE_LANDMARK +
			"') order by getSlot1(Mob,'" + SLOT_NAME + "')";
		at.register(TYPE_LANDMARK);
		List landmarks = at.mobSearch(query);
		DefaultListModel dlm = (DefaultListModel)this.landmarks.getModel();
		for (int i = 0; i < landmarks.size(); i++) {
			dlm.addElement(new Landmark((Mob)landmarks.get(i)));
		}
		agentName = at.getAgentName();
	}

	public synchronized void handleNewMob(Mob m) {
		if (!agentName.equals(m.getSlot1(SLOT_CREATOR)))
			((DefaultListModel)landmarks.getModel()).addElement(new Landmark(m));
	}

	public void createComponents(MicaProperties args) throws MicaException {
		JPanel cp = new JPanel(new BorderLayout());
		JToolBar tb = new JToolBar();
		tb.add(Box.createHorizontalGlue());
		showAll = new JButton("show all");
		showAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showAll();
			}
		});
		showAll.setMnemonic(KeyEvent.VK_A);
		tb.add(showAll);
		tb.add(Box.createHorizontalStrut(10));
		toFRS = new JButton("to FRS");
		toFRS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toFRS();
			}
		});
		toFRS.setMnemonic(KeyEvent.VK_F);
		tb.add(toFRS);
		tb.add(Box.createHorizontalStrut(10));
		clearMap = new JButton("clean map");
		clearMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					at.writeMob(new Mob(TYPE_DRAWCLEAN));
				} catch (TransportException ex) {
					ex.printStackTrace();
				}
			}
		});
		clearMap.setMnemonic(KeyEvent.VK_C);
		tb.add(clearMap);
		cp.add(tb, BorderLayout.NORTH);
		JPanel temp = new JPanel(new GridBagLayout());
		temp.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Landmarks"),
				BorderFactory.createEmptyBorder(2,2,2,2)));
		landmarks = new JList(new DefaultListModel());
		landmarks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		landmarks.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				current = (Landmark)landmarks.getSelectedValue();
				loadCurrent();
			}
		});
		newLandmark = new JButton("new");
		newLandmark.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newLandmark();
			}
		});
		newLandmark.setMnemonic(KeyEvent.VK_N);
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = c.weighty = c.fill = 1;
		c.gridwidth = 2;
		temp.add(new JScrollPane(landmarks), c);
		c.weighty = 0;
		c.gridwidth = 1;
		temp.add(Box.createHorizontalStrut(5), c);
		c.weightx = 0;
		c.gridy = 1;
		temp.add(newLandmark, c);
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		split.add(temp, JSplitPane.LEFT);
		temp = new JPanel(new GridBagLayout());
		temp.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Landmark"),
				BorderFactory.createEmptyBorder(2,2,2,2)));
		name = new JTextField(30);
		pattern = new JTextField(30);
		description = new JTextArea();
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		bounds = new JList(new DefaultListModel());
		bounds.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		bounds.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				currentPoint = (Point)bounds.getSelectedValue();
				loadPoint();
			}
		});
		show = new JButton("show");
		show.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				show();
			}
		});
		show.setMnemonic(KeyEvent.VK_S);
		apply = new JButton("apply");
		apply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				apply();
			}
		});
		apply.setMnemonic(KeyEvent.VK_ENTER);
		up = new JButton("up");
		up.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				up();
			}
		});
		down = new JButton("down");
		down.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				down();
			}
		});
		add = new JButton("add");
		add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				add();
			}
		});
		remove = new JButton("remove");
		remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				remove();
			}
		});
		x = new JTextField(4);
		y = new JTextField(4);
		accept = new JButton("accept");
		accept.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				accept();
			}
		});
		c.weightx = c.weighty = c.gridx = c.gridy = 0;
		temp.add(new JLabel("name:"), c);
		c.gridy++;
		temp.add(new JLabel("pattern:"), c);
		c.gridy++;
		temp.add(new JLabel("description:"), c);
		c.gridy++;
		temp.add(Box.createVerticalStrut(20), c);
		c.gridy++;
		temp.add(new JLabel("bounds:"),c );
		c.gridy++;
		temp.add(Box.createVerticalStrut(20), c);
		c.weightx = c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 3;
		temp.add(name, c);
		c.gridy++;
		temp.add(pattern, c);
		c.gridy++;
		c.gridheight = 2;
		c.weighty = 1;
		temp.add(new JScrollPane(description), c);
		JPanel boundsPane = new JPanel(new GridBagLayout());
		GridBagConstraints c2 = new GridBagConstraints();
		c2.gridx = c2.gridy = 0;
		c2.weightx = c2.weighty = c2.fill = 1;
		c2.gridheight = 5;
		boundsPane.add(new JScrollPane(bounds), c2);
		c2.weightx = c2.weighty = 0;
		c2.gridheight = c2.gridx = 1;
		boundsPane.add(up, c2);
		c2.gridy++;
		boundsPane.add(down, c2);
		c2.gridy++;
		boundsPane.add(add, c2);
		c2.gridy++;
		boundsPane.add(remove, c2);
		c2.gridy++;
		c2.weighty = 1;
		boundsPane.add(Box.createVerticalStrut(5), c2);
		JPanel pointPane = new JPanel(new GridBagLayout());
		pointPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Point"),
				BorderFactory.createEmptyBorder(2,2,2,2)));
		c2.gridx++;
		c2.weighty = c2.gridy = 0;
		c2.gridheight = 5;
		boundsPane.add(pointPane, c2);
		c2.gridx = c2.gridy = 0;
		c2.gridheight = c2.gridwidth = 1;
		pointPane.add(new JLabel("x:"), c2);
		c2.gridy++;
		pointPane.add(new JLabel("y:"), c2);
		c2.gridx++;
		c2.gridy = 0;
		c2.gridwidth = 2;
		pointPane.add(x, c2);
		c2.gridy++;
		pointPane.add(y, c2);
		c2.gridy++;
		c2.gridwidth = 1;
		pointPane.add(Box.createHorizontalStrut(5), c2);
		c2.gridx++;
		pointPane.add(accept, c2);
		c2.gridy++;
		c2.weighty = 1;
		pointPane.add(Box.createVerticalStrut(5), c2);
		c.gridy += 2;
		temp.add(boundsPane, c);
		c.gridy += 2;
		c.weightx = c.gridwidth = 1;
		c.weighty = 0;
		temp.add(Box.createHorizontalStrut(20), c);
		c.gridx++;
		c.weightx = 0;
		temp.add(show, c);
		c.gridx++;
		temp.add(Box.createHorizontalStrut(5), c);
		c.gridx++;
		temp.add(apply, c);
		split.add(temp, JSplitPane.RIGHT);
		cp.add(split, BorderLayout.CENTER);
		frame.setContentPane(cp);
		frame.pack();
		loadCurrent();
	}
	
	void loadCurrent() {
		if (current == null) {
			name.setText("");
			pattern.setText("");
			description.setText("");
			bounds.clearSelection();
			((DefaultListModel)bounds.getModel()).removeAllElements();
			name.setEnabled(false);
			pattern.setEnabled(false);
			description.setEnabled(false);
			bounds.setEnabled(false);
			add.setEnabled(false);
			show.setEnabled(false);
			apply.setEnabled(false);
		} else {
			name.setText(current.name.trim());
			pattern.setText(current.pattern.trim());
			description.setText(current.description.trim());
			bounds.clearSelection();
			DefaultListModel dlm = (DefaultListModel)bounds.getModel();
			dlm.removeAllElements();
			for (int i = 0; i < current.bounds.size(); i++) {
				dlm.addElement(current.bounds.get(i));
			}
			name.setEnabled(true);
			pattern.setEnabled(true);
			description.setEnabled(true);
			bounds.setEnabled(true);
			add.setEnabled(true);
			show.setEnabled(true);
			apply.setEnabled(true);
		}
		currentPoint = null;
		loadPoint();
	}
	
	void loadPoint() {
		if (currentPoint == null) {
			x.setText("");
			y.setText("");
			x.setEnabled(false);
			y.setEnabled(false);
			accept.setEnabled(false);
			up.setEnabled(false);
			down.setEnabled(false);
			remove.setEnabled(false);
		} else {
			x.setText(currentPoint.x + "");
			y.setText(currentPoint.y + "");
			x.setEnabled(true);
			y.setEnabled(true);
			accept.setEnabled(true);
			remove.setEnabled(true);
			if (bounds.getSelectedIndex() == 0)
				up.setEnabled(false);
			else
				up.setEnabled(true);
			if (bounds.getSelectedIndex() == bounds.getModel().getSize() - 1)
				down.setEnabled(false);
			else
				down.setEnabled(true);
		}
	}
	
	void showAll() {
		DefaultListModel dlm = (DefaultListModel)landmarks.getModel();
		for (int i = 0; i < dlm.size(); i++) {
			((Landmark)dlm.get(i)).show();
		}
	}
	
	void toFRS() {
		String outputString = "make_landmarks() = (\n\tvar landmarkMob;\n";
		DefaultListModel dlm = (DefaultListModel)landmarks.getModel();
		for (int i = 0; i < dlm.size(); i++) {
			outputString += ((Landmark)dlm.get(i)).toFRS();
		}
		outputString += "\ttrue\n);;\n\nmake_landmarks();;";
		FrameScriptDialog dialog = new FrameScriptDialog(frame, outputString);
		dialog.setVisible(true);
	}
	
	void newLandmark() {
		// newLandmark()
		DefaultListModel dlm = (DefaultListModel)landmarks.getModel();
		dlm.addElement(new Landmark(null));
		landmarks.repaint();
		landmarks.setSelectedValue(dlm.get(dlm.getSize() - 1), true);
		name.requestFocus();
	}
	
	void show() {
		// show()
		Mob m = new Mob(TYPE_DRAWPOLYGON);
		m.addSlot(SLOT_COLOUR, "red");
		DefaultListModel dlm = (DefaultListModel)bounds.getModel();
		for (int i = 0; i < dlm.size(); i++) {
			Point p = (Point)dlm.get(i);
			m.addSlot(SLOT_XPOINTS, p.x + "");
			m.addSlot(SLOT_YPOINTS, p.y + "");
		}
		try {
			at.writeMob(m);
		} catch (TransportException e) {
			e.printStackTrace();
		}
	}
	
	void apply() {
		if (current == null)
			return;
		if (name.getText().trim().equals("") || pattern.getText().trim().equals("")) {
			JOptionPane.showMessageDialog(frame, "A landmark needs a name and a pattern.",
					"Invalid Landmark", JOptionPane.WARNING_MESSAGE);
			return;
		}
		current.name = name.getText().trim();
		current.pattern = pattern.getText().trim();
		current.description = description.getText().trim();
		DefaultListModel dlm = (DefaultListModel)bounds.getModel();
		current.bounds.removeAllElements();
		for (int i = 0; i < dlm.size(); i++)
			current.bounds.add(dlm.get(i));
		current.write();
		landmarks.repaint();
	}
	
	void up() {
		if (currentPoint == null)
			return;
		DefaultListModel dlm = (DefaultListModel)bounds.getModel();
		int i = bounds.getSelectedIndex();
		//dlm.removeElement(currentPoint);
		dlm.add(i-1, currentPoint);
		dlm.removeElementAt(i+ 1);
		bounds.repaint();
		bounds.setSelectedValue(dlm.get(i-1), true);
	}
	
	void down() {
		if (currentPoint == null)
			return;
		DefaultListModel dlm = (DefaultListModel)bounds.getModel();
		int i = bounds.getSelectedIndex();
		//dlm.removeElement(currentPoint);
		dlm.add(i+2, currentPoint);
		dlm.removeElementAt(i);
		bounds.repaint();
		bounds.setSelectedValue(dlm.get(i+1), true);
	}
	
	void add() {
		if (current == null)
			return;
		Point p = new Point(0, 0);
		DefaultListModel dlm = (DefaultListModel)bounds.getModel();
		dlm.addElement(p);
		bounds.setSelectedValue(dlm.get(dlm.getSize() - 1), true);
		x.requestFocus();
	}
	
	void remove() {
		if (current == null || currentPoint == null)
			return;
		DefaultListModel dlm = (DefaultListModel)bounds.getModel();
		int i = bounds.getSelectedIndex();
		dlm.removeElementAt(i);
		bounds.setSelectedValue(dlm.get((i == dlm.size())?i-1:i), true);
	}
	
	void accept() {
		if (currentPoint == null)
			return;
		try {
			int xi = Integer.parseInt(x.getText().trim());
			int yi = Integer.parseInt(y.getText().trim());
			currentPoint.x = xi;
			currentPoint.y = yi;
			bounds.repaint();
		} catch (NumberFormatException e) {
		}
	}

	static String format(String str) {
		return str;
	}
	
	static String unformat(String str) {
		return str;
	}
	
	class Landmark {
		String name, pattern, description;
		Vector bounds;
		String mobName;
		
		Landmark(Mob mob) {
			bounds = new Vector();
			if (mob == null) {
				name = pattern = description = " ";
			} else {
				mobName = mob.getName();
				name = unformat(mob.getSlot1(SLOT_NAME));
				pattern = unformat(mob.getSlot1(SLOT_PATTERN));
				description = "";
				if (mob.hasSlot(SLOT_DESCRIPTION))
					description = unformat(mob.getSlot1(SLOT_DESCRIPTION));
				if (mob.hasSlot(SLOT_XPOINTS)) {
					List xs = mob.getSlot(SLOT_XPOINTS);
					List ys = mob.getSlot(SLOT_YPOINTS);
					int min = (xs.size() < ys.size())?xs.size():ys.size();
					for (int i = 0; i < min; i++) {
						try {
							bounds.add(new Point(Integer.parseInt((String)xs.get(i)),
									Integer.parseInt((String)ys.get(i))));
						} catch (NumberFormatException e) {
						}
					}
				}
			}
		}
		
		void write() {
			Mob m = new Mob(TYPE_LANDMARK);
			m.addSlot(SLOT_NAME, format(name));
			m.addSlot(SLOT_PATTERN, format(pattern));
			if (!description.trim().equals(""))
				m.addSlot(SLOT_DESCRIPTION, format(description));
			for (int i = 0; i < bounds.size(); i++) {
				Point p = (Point)bounds.get(i);
				m.addSlot(SLOT_XPOINTS, p.x + "");
				m.addSlot(SLOT_YPOINTS, p.y + "");
			}
			String oldName = mobName;
			try {
				mobName = at.writeMob(m);
				if (oldName != null)
					at.deleteMob(oldName);
			} catch (MicaException e) {
				e.printStackTrace();
			}
		}
		
		void show() {
			// show()
			if (bounds.size() == 0)
				return;
			Mob m = new Mob(TYPE_DRAWPOLYGON);
			m.addSlot(SLOT_COLOUR, "red");
			for (int i = 0; i < bounds.size(); i++) {
				Point p = (Point)bounds.get(i);
				m.addSlot(SLOT_XPOINTS, p.x + "");
				m.addSlot(SLOT_YPOINTS, p.y + "");
			}
			try {
				at.writeMob(m);
			} catch (TransportException e) {
				e.printStackTrace();
			}
		}
		
		public String toString() {
			return name;
		}
		
		String toFRS() {
			if (name.trim().equals("") || pattern.trim().equals(""))
				return "";
			String rval = "\tlandmarkMob = new landmark;\n" + 
				"\tput(landmarkMob, name, quote(" + format(name.trim())
				+ "));\n" + "\tput(landmarkMob, pattern, quote(" +
				format(pattern.trim()) + "));\n";
			if (!description.trim().equals(""))
				rval += "\tput(landmarkMob, description, quote(" +
					format(description.trim()) + "));\n";
			if (bounds.size() > 0) {
				Point p = (Point)bounds.get(0);
				String xPoints = "[ " + p.x;
				String yPoints = "[ " + p.y;
				for (int i = 1; i < bounds.size(); i++) {
					p = (Point)bounds.get(i);
					xPoints += " " + p.x;
					yPoints += " " + p.y;
				}
				xPoints += " ]";
				yPoints += " ]";
				rval += "\tput(landmarkMob, xPoints, " + xPoints + ");\n";
				rval += "\tput(landmarkMob, yPoints, " + yPoints + ");\n";
			}
			rval += "\tmica_write_mob(landmarkMob);\n";
			return rval;
		}
	}
	
	class Point {
		int x, y;
		
		Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public String toString() {
			return "(" + x + ", " + y + ")";
		}
	}
	
	class FrameScriptDialog extends JDialog {
		String text;
		JTextArea textArea;
		JButton button;
		FrameScriptDialog(Frame owner, String text) {
			super(owner, "FrameScript", true);
			this.text = text;
			textArea = new JTextArea();
			textArea.setText(text);
			textArea.setEditable(false);
			textArea.setWrapStyleWord(true);
			textArea.setLineWrap(true);
			textArea.setColumns(50);
			button = new JButton("OK");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
			JPanel cp = new JPanel(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.weightx = c.weighty = c.fill = 1;
			c.gridwidth = 3;
			cp.add(new JScrollPane(textArea), c);
			c.gridy = c.gridx = c.gridwidth = 1;
			c.weightx = c.weighty = 0;
			cp.add(button, c);
			c.weightx = 1;
			c.gridx = 0;
			cp.add(Box.createHorizontalStrut(50), c);
			c.gridx = 2;
			cp.add(Box.createHorizontalStrut(50), c);
			setContentPane(cp);
			pack();
			button.requestFocus();
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		}
	}
}
