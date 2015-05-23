package unsw.cse.mica.learner;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;

import javax.swing.JPanel;

import unsw.cse.mica.MicaException;
import unsw.cse.mica.agent.GUIAgent;
import unsw.cse.mica.data.Mob;
import unsw.cse.mica.util.Debug;
import unsw.cse.mica.util.MicaProperties;
import weka.classifiers.Classifier;
import weka.core.Drawable;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;

public class TreeView extends GUIAgent {
	String modelFile = "tree.mdl";
	Classifier classifier;
	JPanel cp;
	public void init(MicaProperties args) throws MicaException {
		super.init(args);
		if(args.getParam(0).equals("modelFile")){
			modelFile = args.getValue(0);
			Debug.dp(Debug.IMPORTANT, "ModelFile is " + modelFile);
			frame.setTitle("Learnt model from: " + modelFile); 
			try {
				reload();
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
//		at.connect("treeView");
		at.register("learnerUpdated");
	}

	public synchronized void handleNewMob(Mob m) {
		if(m.getType().equals("learnerUpdated")){
			try {
				reload();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	void reload() throws Exception {
		Debug.dp(Debug.IMPORTANT, "Rereading ...  ");
		TreeVisualizer tviz = null; 
		try {
			InputStream is = new FileInputStream(modelFile);  
			ObjectInputStream objectInputStream = new ObjectInputStream(is);
			classifier = (Classifier) objectInputStream.readObject();
			is.close();
		}
		catch(Exception e){
			Debug.dp(Debug.IMPORTANT, "Could not load tree at this time. Hit reload when ready."); 
		}
		if(classifier != null){
//			
			tviz = new TreeVisualizer(null,((Drawable) classifier).graph(),new PlaceNode2());
			cp.removeAll();
			cp.add(tviz, BorderLayout.CENTER);	
		}	
		cp.invalidate();
		cp.revalidate();
		if(tviz != null){ 
			tviz.fitToScreen();
		}
		frame.repaint();
	}
	
	public TreeView(){
		super();
	}
	
	public void createComponents(MicaProperties args) throws MicaException {
		Dimension cpSize = new Dimension(500, 500);
		frame.setTitle("Learnt model from: " + modelFile); 
		cp = new JPanel(new BorderLayout());
		cp.setSize(cpSize);
		cp.setPreferredSize(cpSize);
		frame.setContentPane(cp);
		frame.pack();
	}

}
