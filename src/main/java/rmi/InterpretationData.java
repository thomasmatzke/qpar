package main.java.rmi;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.log4j.Logger;

import main.java.logic.parser.SimpleNode;
import main.java.master.Master;

/**
 * Data needed for the slave to compute a interpretation
 * @author thomasm
 *
 */
public class InterpretationData implements Serializable {

	static Logger logger = Logger.getLogger(InterpretationData.class);
	
	private byte[] serializedFormula;
	private SimpleNode formulaRoot;
//	private Vector<Integer> eVars = new Vector<Integer>();
//	private Vector<Integer> aVars = new Vector<Integer>();
	private ArrayList<Integer> trueVars = new ArrayList<Integer>();
	private ArrayList<Integer> falseVars = new ArrayList<Integer>();
	
	public InterpretationData(byte[] serializedFormula, ArrayList<Integer> trueVars, ArrayList<Integer> falseVars) {
		this.setSerializedFormula(serializedFormula);
		this.setTrueVars(trueVars);
		this.setFalseVars(falseVars);
	}

	public SimpleNode getRootNode() {
		if(this.formulaRoot == null) {
			if(this.serializedFormula == null) {
				// This is weird...
				logger.error("TransmissionQbf didnt contain a formula tree or a serialized formula-tree");
				assert(false);
				return null;
			} else {
				// This means the root was nullified by transient while serialization
				// deserialize and write to root-variable
				try {
					ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(this.serializedFormula));
					this.formulaRoot = (SimpleNode) in.readObject();
					in.close();
					return formulaRoot;
				} catch (Exception e) {
					// this sucks...
					logger.error("Problem while deserializing formula", e);
					return null;
				}
			}
		} else {
			return this.formulaRoot;
		}
	}
	
	public void setSerializedFormula(byte[] serializedFormula) {
		this.serializedFormula = serializedFormula;
	}

	public byte[] getSerializedFormula() {
		return serializedFormula;
	}

	public void setTrueVars(ArrayList<Integer> trueVars) {
		this.trueVars = trueVars;
	}

	public ArrayList<Integer> getTrueVars() {
		return trueVars;
	}

	public void setFalseVars(ArrayList<Integer> falseVars) {
		this.falseVars = falseVars;
	}

	public ArrayList<Integer> getFalseVars() {
		return falseVars;
	}
	
	
}
