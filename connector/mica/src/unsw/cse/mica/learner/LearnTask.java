/*
 * Created on 4/07/2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package unsw.cse.mica.learner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import unsw.cse.mica.util.Debug;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

/* Wow. A real use of inheritance. 
 * 
 * @author waleed
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
 
class ClassResult {
	String predictedClass; 
	double confidence; 
	
	public ClassResult(String predictedClass, double confidence){
		this.predictedClass = predictedClass; 
		this.confidence = confidence;	
	}
} 
class MicaAtt extends Attribute {
	String sourceMob; 
	String sourceSlot; 
	
	/**
	 *  The constructor for continuous attributes. 
	 * @param
	 */
	public MicaAtt(String name, String sourceMob, String sourceSlot) {
		super(name);
		this.sourceMob = sourceMob;
		this.sourceSlot = sourceSlot; 
	}
	
	/**
	 *  The constructor for discrete attributes. 
	 * @param name
	 * @param attVals
	 * @param sourceMob
	 * @param sourceSlot
	 */
	public MicaAtt(String name, FastVector attVals, String sourceMob, String sourceSlot) {
		super(name, attVals);
		this.sourceMob = sourceMob;
		this.sourceSlot = sourceSlot; 
	}
	
	public String myToString(){
		return "Mica att: sm = " + sourceMob + " sb = " + sourceSlot + " att = " + super.toString() + "\n"; 		
	}

	/**
	 * @return
	 */
	public String getSourceMob() {
		return sourceMob;
	}

	/**
	 * @return
	 */
	public String getSourceSlot() {
		return sourceSlot;
	}

	/**
	 * @param string
	 */
	public void setSourceMob(String string) {
		sourceMob = string;
	}

	/**
	 * @param string
	 */
	public void setSourceSlot(String string) {
		sourceSlot = string;
	}

}

public class LearnTask extends DefaultHandler {
	String taskName;
	String learner; 
	String modelFile; 
	String dataFile; 
	List attributes = new ArrayList(); // This includes the class as the last element.
	MobCache cache;
	Instances trainingSet;  
	Classifier classifier; 
	String classifierOptions="";
	//The following variables are temps while we read info from the xml description. 
	//The are prepended with tx (temporary XML) to indicate this. 
	MicaAtt txMicaAtt; 
	FastVector txAttVals;
	String txAttName;   
	String txSourceMob; 
	String txSourceSlot; 
	String txType; 
	
	
	public LearnTask(MobCache cache, String fileName){
		File f = new File(fileName); 
		init(cache, f); 
	}
	
	public LearnTask(MobCache cache, File file){
		init(cache, file);
	}

	private void init(MobCache cache, File file){
		this.cache = cache; 
		try {
			InputSource in = new InputSource(new FileReader(file));
			SAXParserFactory factory =  SAXParserFactory.newInstance(); 
			SAXParser parser = factory.newSAXParser();
			parser.parse(in, this);
		}
		catch(Exception e){
			Debug.dp(Debug.IMPORTANT, "Error: Could not parse learning task");
			e.printStackTrace(); 	
		}
		trainingSet = new Instances(taskName, toFV(attributes), 0); 
		trainingSet.setClassIndex(attributes.size()-1); 	
	}
	
	/**
	 * @param attributes
	 */
	private FastVector toFV(List attributes) {
		FastVector fv = new FastVector(attributes.size()); 
		for(Iterator i = attributes.iterator(); i.hasNext(); ){
			fv.addElement(i.next()); 
		}
		return fv; 
	}

	public String toString(){
		StringBuffer sb = new StringBuffer("Learning task: " + taskName + "\n"); 
		sb.append("Model file: " + modelFile + " DataFile: " + dataFile + "\n");
		sb.append("Attributes: \n");
		for(int i=0; i < attributes.size()-1; i++){
			sb.append(((MicaAtt) attributes.get(i)).myToString());
		}
		sb.append("Class: "); 
		sb.append(((MicaAtt) attributes.get(attributes.size()-1)).myToString());
		return sb.toString();  
		
	}
	
	public void addTrainingInstance(String className){
		Instance inst = getContextInstance(); 
		// Now set the class
		inst.setClassValue(className); 
		trainingSet.add(inst); 
	}
	
	public void saveData(){
		try {
			FileWriter fw = new FileWriter(dataFile);
			fw.write(trainingSet.toString());
			fw.flush();  
			fw.close(); 
		}
		catch(Exception e){
			Debug.dp(Debug.EMERGENCY, "Learner: Could not save training set");
			e.printStackTrace();   
		}
		
	}
	
	public void learn(){
		try {
			String[] splitOptions = Utils.splitOptions(classifierOptions);
			classifier = Classifier.forName(learner, splitOptions);
			classifier.buildClassifier(trainingSet);
		} catch (Exception e) {
			Debug.dp(Debug.EMERGENCY, "Learner: Could not learn"); 
			e.printStackTrace();
		} 
	}
	
	public void saveModel(){
		try {
			// JHW: first create the directory if necessary...
			File file = new File(modelFile);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(modelFile));
			os.writeObject(classifier); 
			os.flush(); 
			os.close(); 
		} catch (Exception e) {
			Debug.dp(Debug.EMERGENCY, "Learner: Could not save model file");
			e.printStackTrace();
		} 
		
	}
	
	/**
	 * Given the current situation, ask what the expected classification result is. 
	 *
	 */
	public ClassResult classify(){
		try {
			Instance inst = getContextInstance();
			double predicted = classifier.classifyInstance(inst);
			Debug.dp(Debug.TRACE, "Predicted is: " + predicted); 
			// Now double check with the attribute to get the return value. 
			String value = ((MicaAtt) attributes.get(attributes.size()-1)).value((int) predicted);
			double confidence; 
			double[] confs = classifier.distributionForInstance(inst); 
			confidence = confs[(int) predicted]; 
			Debug.dp(Debug.TRACE, "Predicted is: " + value + " with confidence"); 
			return new ClassResult(value, confidence); 
						
		} catch (Exception e) {
			Debug.dp(Debug.EMERGENCY, "Learner: Could not classify instance.");
			e.printStackTrace();
		} 
		return new ClassResult("unknown",0); 
		
	
	}

	/**
	 *  Gets the current context as an instance. Note: Does not get the current 
	 *  class; but sets all the other instances up. 
	 * @return
	 */
	public Instance getContextInstance() {
		Instance inst = new Instance(attributes.size());
		inst.setDataset(trainingSet);
		for(int i=0; i < attributes.size()-1; i++){
			 MicaAtt thisAtt = (MicaAtt) attributes.get(i); 
			 // First extract the information. 
			 try {
			 	String value = cache.getMob(thisAtt.getSourceMob()).getSlot1(thisAtt.getSourceSlot());
				if(thisAtt.isNominal()){
					Debug.dp(Debug.TRACE, "Setting to " + value); 
				   inst.setValue(thisAtt, value); 
				}
				else { // Attribute is continuous 
				   inst.setValue(thisAtt, Double.parseDouble(value)); 
				}

			 }
			 catch(Exception e){
			 	Debug.dp(Debug.FN_CALLS, "Unknown value -- setting to unknown");
				inst.setMissing(thisAtt); 			 	
			 }
		}
		Debug.dp(Debug.TRACE, "Instance is: " + inst); 
		return inst; 
	}
	
	public static void main(String[] args) {
		Debug.dp(Debug.INFORMATION, "Reading in config file");
		MobCache cache = new MobCache(); 
		LearnTask lt = new LearnTask(cache, "/home/waleed/eclipse/workspace/Mica/config/learntask/readordisplayemail.xml");
		System.out.println(lt.toString()); 
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName)
		throws SAXException {
		
		if(qName.equals("attribute")){
			if(txType.equals("discrete")){
				txMicaAtt = new MicaAtt(txAttName, txAttVals, txSourceMob, txSourceSlot);
				attributes.add(txMicaAtt); 	
			}
			if(txType.equals("continuous")){
				txMicaAtt = new MicaAtt(txAttName, txSourceMob, txSourceSlot);
				attributes.add(txMicaAtt); 
			}
			// Now let the cache know we are interested in them. 
			cache.addHotType(txSourceMob); 
		}
		else if(qName.equals("class")){
			txMicaAtt = new MicaAtt(txAttName, txAttVals,"", "");
			attributes.add(txMicaAtt); 
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	public void startDocument() throws SAXException {
		super.startDocument();
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(
		String uri,
		String localName,
		String qName,
		Attributes atts)
		throws SAXException {
			Debug.dp(Debug.FN_CALLS, "learntask: parsing " + qName); 
			if(qName.equals("learntask")){
				taskName = atts.getValue("name"); 
				learner = atts.getValue("learner"); 
				dataFile = atts.getValue("datafile"); 
				modelFile = atts.getValue("modelfile"); 
				if(atts.getValue("options") != null){
					classifierOptions = atts.getValue("options");
				}
			}
			else if(qName.equals("attribute")){
				txAttName = atts.getValue("name"); 
				txType = atts.getValue("type");
				txSourceMob = atts.getValue("sourcemob"); 
				txSourceSlot = atts.getValue("sourceslot"); 
				txAttVals= new FastVector(); 
			}
			else if(qName.equals("value")){
				txAttVals.addElement(atts.getValue("label")); 	
			}
			else if(qName.equals("class")){
				txAttName = atts.getValue("name");
				txAttVals = new FastVector(); 
			}
		
	}
	/**
	 * @return
	 */
	public String getDataFile() {
		return dataFile;
	}

	/**
	 * @return
	 */
	public String getLearner() {
		return learner;
	}

	/**
	 * @return
	 */
	public String getTaskName() {
		return taskName;
	}

}
