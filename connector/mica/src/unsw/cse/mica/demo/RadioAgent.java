package unsw.cse.mica.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import unsw.cse.mica.MicaException;
import unsw.cse.mica.TransportException;
import unsw.cse.mica.agent.GUIAgent;
import unsw.cse.mica.data.Mob;
import unsw.cse.mica.util.MicaProperties;

/**
 * A simple GUI that pretends to be a radio and changes its state in response to
 * various mobs.
 * 
 * @author mmcgill
 */
public class RadioAgent extends GUIAgent implements ListSelectionListener, ActionListener {
	final static String TYPE_STATION = "radio_station";
	final static String TYPE_RADIO = "radio";
	
	final static String SLOT_NAME = "name";
	final static String SLOT_NICKS = "nicks";
	final static String SLOT_FREQ = "freq";
	final static String SLOT_COMMAND = "command";
	final static String SLOT_CHANNEL = "channel";
	
	final static String VAL_OFF = "off";
	final static String VAL_ON = "on";
	final static String VAL_UP = "up";
	final static String VAL_DOWN = "down";
	final static String VAL_SWITCH = "switch";


	public static String formatSlot(String slot) {
		return slot;
	}
	
	public static String unformatSlot(String slot) {
		return slot;
	}
	
	public static String formatSlotS(String slot) {
		return "\"" + slot + "\"";
	}
	
	public static String unformatSlotS(String slot) {
		return slot.substring(1, slot.length() - 1);
	}
	
	JLabel currStation;
	String currSName;
	
	VolumePanel volumePane;
	
	JList stations;
	JTextField stationName;
	JTextField stationNicks;
	JTextField stationFreq;
	JButton newStation;
	JButton applyStation;
	
	int volume = 50;
	boolean isOn = true;
	Station currentStation;
	
	class VolumePanel extends JLabel {
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(Color.WHITE);
			Rectangle r = g.getClipBounds();
			g.fillRect(r.x, r.y, r.width, r.height);
			if (isOn) {
				int [] x = {r.x, r.width * volume / 100, r.width * volume / 100};
				int [] y = {r.height, r.height, r.y + ((r.height - r.y) * (100 - volume) / 100)};
				g.setColor(Color.GREEN);
				g.fillPolygon(x, y, 3);
			}
		}
	}
	
	class Station {
		String mName = null;
		String name = "";
		String nicks = "";
		String freq = "";
		
		Station(Mob m) {
			if (m != null) {
				mName = m.getName();
				name = unformatSlot(m.getSlot1(SLOT_NAME));
				nicks = unformatSlot(m.getSlot1(SLOT_NICKS));
				freq = unformatSlot(m.getSlot1(SLOT_FREQ));
			}
		}
		
		void save() {
			String oldName = mName;
			Mob m = new Mob(TYPE_STATION);
			m.addSlot(SLOT_NAME, formatSlot(name));
			m.addSlot(SLOT_NICKS, formatSlot(nicks));
			m.addSlot(SLOT_FREQ, formatSlot(freq));
			try {
				at.writeMob(m);
				at.deleteMob(oldName);
			} catch (MicaException e) {
				e.printStackTrace();
			}
		}
		
		public String toString() {
			if (mName == null)
				return "*"+name;
			return name;
		}
	}
	
	public void createComponents(MicaProperties args) throws MicaException {
		JPanel cp = new JPanel(new BorderLayout());
		JPanel temp = new JPanel(new GridBagLayout());
		currStation = new JLabel();
		Font f = currStation.getFont();
		currStation.setFont(f.deriveFont((float)f.getSize()*2));
		volumePane = new VolumePanel();
		currStation.setSize(240, 80);
		currStation.setPreferredSize(new Dimension(240, 80));
		volumePane.setSize(80, 80);
		volumePane.setPreferredSize(new Dimension(80, 80));
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = c.weighty = c.fill = 1;
		temp.add(currStation, c);
		c.weightx = 0; c.gridx = 1;
		temp.add(volumePane, c);
		cp.add(temp, BorderLayout.NORTH);
		
		temp = new JPanel(new BorderLayout());
		JSplitPane split = new JSplitPane();
		temp.add(split, BorderLayout.CENTER);
		cp.add(temp, BorderLayout.CENTER);
		temp = new JPanel(new GridBagLayout());
		c.weightx = c.weighty = 1;
		c.gridx = c.gridy = 0;
		c.gridwidth = 2;
		stations = new JList(new DefaultListModel());
		stations.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		stations.addListSelectionListener(this);
		newStation = new JButton("new");
		newStation.addActionListener(this);
		temp.add(new JScrollPane(stations), c);
		c.weightx = c.weighty = 0;
		c.gridy = c.gridwidth = 1;
		temp.add(newStation, c);
		c.weightx = 1;
		c.gridx = 1;
		temp.add(Box.createHorizontalStrut(5), c);
		split.add(temp, JSplitPane.LEFT);
		temp = new JPanel(new GridBagLayout());
		stationName = new JTextField(20);
		stationName.setEnabled(false);
		stationNicks = new JTextField(20);
		stationNicks.setEnabled(false);
		stationFreq = new JTextField(20);
		stationFreq.setEnabled(false);
		applyStation = new JButton("apply");
		applyStation.setEnabled(false);
		applyStation.addActionListener(this);
		c.weightx = c.weighty = c.gridx = c.gridy = 0;
		temp.add(new JLabel("name:"), c);
		c.gridy++;
		temp.add(new JLabel("nicks:"), c);
		c.gridy++;
		temp.add(new JLabel("freq:"), c);
		c.gridy = 0;
		c.weightx = c.gridx = 1;
		c.gridwidth = 2;
		temp.add(stationName, c);
		c.gridy++;
		temp.add(stationNicks, c);
		c.gridy++;
		temp.add(stationFreq, c);
		c.gridy++;
		c.gridwidth = 1;
		temp.add(Box.createHorizontalStrut(5), c);
		c.gridx++;
		c.weightx = 0;
		temp.add(applyStation, c);
		split.add(temp, JSplitPane.RIGHT);
		frame.setContentPane(cp);
		frame.pack();
	}
	
	public void valueChanged(ListSelectionEvent e) {
		currentStation = (Station)stations.getSelectedValue();
		if (currentStation == null) {
			stationName.setText("");
			stationName.setEnabled(false);
			stationNicks.setText("");
			stationNicks.setEnabled(false);
			stationFreq.setText("");
			stationFreq.setEnabled(false);
			applyStation.setEnabled(false);
		} else {
			stationName.setText(currentStation.name);
			stationName.setEnabled(true);
			stationNicks.setText(currentStation.nicks);
			stationNicks.setEnabled(true);
			stationFreq.setText(currentStation.freq);
			stationFreq.setEnabled(true);
			applyStation.setEnabled(true);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("new")) {
			Station s = new Station(null);
			((DefaultListModel)stations.getModel()).addElement(s);
			stations.setSelectedValue(s, true);
		} else if (e.getActionCommand().equals("apply") && currentStation != null) {
			currentStation.name = stationName.getText();
			currentStation.nicks = stationNicks.getText();
			currentStation.freq = stationFreq.getText();
			currentStation.save();
		}
		stations.repaint();
	}

	public void init(MicaProperties args) throws MicaException {
		super.init(args);
		at.register(TYPE_RADIO);
		String findStations = "select * from mobs where typeOf(Mob,'" + TYPE_STATION +
		"') order by getSlot1(Mob,'" + SLOT_NAME + "')";
		List l = at.mobSearch(findStations);
		if (l.size() == 0) {
			createStations();
			l = at.mobSearch(findStations);
		}
		DefaultListModel dlm = (DefaultListModel)stations.getModel();
		for (Iterator i = l.iterator(); i.hasNext();)
			dlm.addElement(new Station((Mob)i.next()));
		if (dlm.size() > 0) {
			Station s = (Station)dlm.get(0);
			currStation.setText(currSName = s.name + "(" + s.freq + ")");
		} else {
			currStation.setText(currSName = "000.0 FM");
		}
		frame.repaint();
	}
	
	String [][] DefaultStations = {
			{"MMM", "{ triple m | m m m | mmm }", "104.9 FM"},
			{"JJJ", "{ triple j | j j j | jjj }", "105.7 FM"},
			{"Radio National", "radio national", "576 AM"},
			{"ABC News", "{ abc | a b c | news }", "630 AM"},
			{"Nova", "nova", "96.9 FM"},
			{"2UE", "2 { ue | u e }", "954 AM"}};
	
	void createStations() throws TransportException {
		for (int i = 0; i < DefaultStations.length; i++) {
			Mob m = new Mob("radio_station");
			m.addSlot("name", DefaultStations[i][0]);
			m.addSlot("nicks", DefaultStations[i][1]);
			m.addSlot("freq", DefaultStations[i][2]);
			at.writeMob(m);
		}
	}
	
	public synchronized void handleNewMob(Mob m) {
		super.handleNewMob(m);
		if (m.getSlot1(SLOT_COMMAND).equals(VAL_ON)) {
			if (!isOn)
				currStation.setText(currSName);
			isOn = true;
		} else if (m.getSlot1(SLOT_COMMAND).equals(VAL_OFF)) {
			currStation.setText("");
			isOn = false;
		} else if (m.getSlot1(SLOT_COMMAND).equals(VAL_UP) && volume < 100)
			volume += 10;
		else if (m.getSlot1(SLOT_COMMAND).equals(VAL_DOWN) && volume > 0)
			volume -= 10;
		else if (m.getSlot1(SLOT_COMMAND).equals(VAL_SWITCH)) {
			if (m.hasSlot(SLOT_CHANNEL))
				currSName = unformatSlot(m.getSlot1(SLOT_CHANNEL)) +
				"(" + unformatSlot(m.getSlot1(SLOT_FREQ)) + ")";
			else
				currSName = unformatSlot(m.getSlot1(SLOT_FREQ));
			currStation.setText(currSName);
			isOn = true;
		}
		frame.repaint();
	}
}
