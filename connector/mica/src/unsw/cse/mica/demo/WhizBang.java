package unsw.cse.mica.demo;


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import unsw.cse.mica.MicaException;
import unsw.cse.mica.TransportException;
import unsw.cse.mica.agent.DefaultAgent;
import unsw.cse.mica.agent.XMLOverTCPAgentTransport;
import unsw.cse.mica.data.Mob;
import unsw.cse.mica.util.MicaProperties;

class LTPanel extends JPanel {
	JComboBox jcb; 
	JButton train; 
	JButton test;
	ActionListener al; 
	
	public LTPanel(ActionListener al){
		 	train = new JButton("Train"); 
		 	test = new JButton("Test"); 
		 	String [] classes = {"read", "display", "askuser"}; 
		 	jcb = new JComboBox(classes); 
			train.setActionCommand("Learn:Train"); 
			test.setActionCommand("Learn:Test");
			train.addActionListener(al); 
			test.addActionListener(al);
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Learning"));
			setLayout(new FlowLayout());
			jcb.setSelectedIndex(0);  
			add(jcb); 
			add(train); 
			add(test);  
			  
	}
	
	public String getComboState(){
		return (String) jcb.getSelectedItem(); 
	}
}

class NoiseSlider extends JPanel {
	ChangeListener cl; 
	JSlider slider; 
	
	public NoiseSlider(ChangeListener cl){

	this.cl = cl; 
	slider = new JSlider(SwingConstants.VERTICAL, 0, 10, 5);
	slider.addChangeListener(cl);
	slider.setMajorTickSpacing(5);
	slider.setMinorTickSpacing(1);
	slider.setPaintTicks(true);
	slider.setPaintLabels(true);
	add(slider);
	setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Noise")); 
	}
}

class OptionPanel extends JPanel {
	ActionListener al; 
	List buttons = new ArrayList(); 
	ButtonGroup bg = new ButtonGroup(); 
	String optionName; 
		
	void addOption(String name){
		JRadioButton newButton = new JRadioButton(name);
		newButton.setActionCommand(optionName + ":" + name);	 	
		newButton.addActionListener(al);
		buttons.add(newButton); 	 	
		bg.add(newButton); 
		add(newButton);
	}
	
	void select(int num){
		((JRadioButton) buttons.get(num)).doClick(); 
	}
	
	OptionPanel(ActionListener al, String optionName){
		this.optionName = optionName;
		this.al = al;
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), optionName));
		setLayout(new GridLayout(0,1)); 
	}
}

/**
 * @author waleed
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class WhizBang extends DefaultAgent implements ActionListener, ChangeListener {
	public static String DEFAULTSPEAKER = "Juliette";
	private OptionPanel activity;
	JTextField input;
	JTextArea logInside;
	JScrollPane log; 
	OptionPanel location;
	LTPanel lt;  
	NoiseSlider ns; 
	 
	/* (non-Javadoc)
	 * @see unsw.cse.mica.agent.Agent#handleMob(unsw.cse.mica.data.Mob)
	 */
	 
	 JFrame mainWindow;
	 
	 public WhizBang(){
	 	
	 }
	 	 
	public void handleNewMob(Mob m) {
		if(m.getType().equals("textForUser")){
			String text = m.getSlot1("utterance");
			String speaker = m.getSlot1("speaker");
			if (speaker == null)
				speaker = DEFAULTSPEAKER; 
			logInside.append(speaker + " said: " + text + "\n"); 
			logInside.getCaret().setDot(logInside.getText().length() );
			log.scrollRectToVisible(logInside.getVisibleRect() );
		}
		if(m.getType().equals("learnerReply")){
			//String requestId = m.getSlot1("requestId"); 
			//String finalClass = m.getSlot1("predictedClass"); 
			//String confidence = m.getSlot1("confidence"); 
			// logInside.append("[Learner] Re: " + requestId + " -- I think I should " + finalClass + " your email with confidence " + confidence + "\n"); 
		}

	}

	/* (non-Javadoc)
	 * @see unsw.cse.mica.agent.Agent#init()
	 */
	 
	public void init(MicaProperties arg) throws MicaException {
		mainWindow = new JFrame("WhizBang Interface");
		input = new JTextField(50);
		input.addActionListener(this);
		input.setActionCommand("inputReturn");	
		logInside = new JTextArea(10, 50);
		logInside.setEditable(false);
		logInside.setLineWrap(true); 
		log = new JScrollPane(logInside,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				
		log.setAutoscrolls(true);
		location = new OptionPanel(this, "Location"); 
		location.addOption("Home");
		location.addOption("Car");
		location.addOption("Office");
		activity = new OptionPanel(this,"Who is Around");
		activity.addOption("Alone");
		activity.addOption("WithFriends");
		activity.addOption("WithStrangers");
		lt =new LTPanel(this);  
		JPanel textPanel = new JPanel(); 
		GridBagLayout gridBag = new GridBagLayout();
		textPanel.setLayout(gridBag);
		GridBagConstraints c = new GridBagConstraints();

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy=1; 
		gridBag.setConstraints(input, c);
		textPanel.add(input);
		
		c.gridy=0; 
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		gridBag.setConstraints(log, c);
		textPanel.add(log);
		textPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Conversation"));   
		JPanel envPanel = new JPanel();
		envPanel.setLayout(new BoxLayout(envPanel, BoxLayout.Y_AXIS));
		envPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Environment"));   
		envPanel.add(location); 
		envPanel.add(activity); 
		ns = new NoiseSlider(this);
		envPanel.add(ns);
		// envPanel.add(lt);  
		mainWindow.getContentPane().add(envPanel, BorderLayout.WEST);
		mainWindow.getContentPane().add(textPanel, BorderLayout.CENTER);
		mainWindow.addWindowListener(new WindowAdapter() {
							public void windowClosing(WindowEvent e) {
								System.exit(0);
							}
						});
						
		mainWindow.pack();
		mainWindow.setVisible(true);
		at.connect("TextDevice");
		at.register("textForUser");
		at.register("learnerReply"); 
		//List record = at.mobSearch("select e from e in text order by e.creationTime desc limit 3");
		List record = at.mobSearch("select top 3 * from Mobs where typeOf(Mob,'text') order by getSlot1(Mob,'creationTime') desc");
		
		// System.out.println("REsults were: " + record.toString()); 
		if(record.size() > 0){ 
			logInside.append("This is what we were talking about last time: \n");
			for(Iterator i = record.iterator(); i.hasNext(); ){
				Mob m = (Mob) i.next(); 
				if(m.getType().equals("textFromUser")){
					logInside.append("You said: " + m.getSlot1("utterance") + "\n"); 
				}
				else if(m.getType().equals("textForUser")){
					String speaker = m.getSlot1("speaker");
					if (speaker == null)
						speaker = DEFAULTSPEAKER;
					logInside.append(speaker + " said: " + m.getSlot1("utterance") + "\n"); 	
				}
			}
			logInside.append("And now ... let's continue! \n \n");
		} 
		// System.out.println("REcord finished.");
		location.select(0); 
		activity.select(0); 
		ns.slider.setValue(5); 
	}
	
	public void terminate() throws TransportException {
		at.disconnect();
	}

	public static void main(String[] args) throws MicaException {
		try {
		UIManager.setLookAndFeel(
			UIManager.getCrossPlatformLookAndFeelClassName());
	} catch (Exception e) { 
		e.printStackTrace(); 
	}
	
		String server = args[0]; 
		int portNum = Integer.parseInt(args[1]);
		WhizBang wb = new WhizBang(); 
		new XMLOverTCPAgentTransport(wb, server, portNum);
		wb.init(null); 
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		try {
			// 	System.out.println("Command is: " + e.getActionCommand()); 
			if(e.getActionCommand().equals("inputReturn")){
				String text = input.getText();
				logInside.append("You said: " + text + "\n");
				Mob m = new Mob("textFromUser"); 
				m.addSlot("utterance", text); 
				at.writeMob(m); 
				input.selectAll();		
			}
			if(e.getActionCommand().startsWith("Location:")){
				String value = e.getActionCommand().substring("Location:".length()).toLowerCase(); 
				Mob m = new Mob("envLocation"); 
				m.addSlot("location", value); 
				at.writeMob(m); 
			}
			if(e.getActionCommand().startsWith("Who is Around:")){
				String value = e.getActionCommand().substring("Who is Around:".length()).toLowerCase(); 
				Mob m = new Mob("envWhosAround"); 
				m.addSlot("whosAround", value); 
				at.writeMob(m); 
			}
			if(e.getActionCommand().startsWith("Learn:")){
				String value = e.getActionCommand().substring("Learn:".length()).toLowerCase();
				if(value.equals("train")){ 
					Mob m = new Mob("learnerTrain");
					m.addSlot("task", "readOrDisplayEmail"); 
					m.addSlot("actualClass", lt.getComboState()); 
					at.writeMob(m); 								 
				}
				if(value.equals("test")){
					Mob m = new Mob("learnerTest");
					m.addSlot("task", "readOrDisplayEmail"); 
					m.addSlot("requestId", "foo"); 
					at.writeMob(m); 								 
				}
			}
		} catch (MicaException me) {
			me.printStackTrace();
		}
	}
	
	
	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
		try {
			JSlider source = (JSlider) e.getSource(); 
			if(!source.getValueIsAdjusting()){
				// System.out.println("Change Event received. Value is: " + source.getValue()); 
				Mob m = new Mob("envNoise"); 
				m.addSlot("noiseLevel", String.valueOf(source.getValue())); 
				at.writeMob(m); 
			}
		} catch (MicaException me) {
			me.printStackTrace();
		}
	}
	

}
