package qpar.common.rmi;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;


import org.apache.log4j.Logger;

import qpar.master.logic.parser.SimpleNode;

/**
 * Data needed for the slave to compute a interpretation
 * @author thomasm
 *
 */
public class InterpretationData implements Serializable {
	private static final long serialVersionUID = -1499438525247990907L;

	static Logger logger = Logger.getLogger(InterpretationData.class);
	
	private byte[] serializedFormula;
	private ArrayList<Integer> trueVars = new ArrayList<Integer>();
	private ArrayList<Integer> falseVars = new ArrayList<Integer>();
	
	private SimpleNode formulaRoot = null;
	
	public InterpretationData(byte[] serializedFormula, ArrayList<Integer> trueVars, ArrayList<Integer> falseVars) {
		this.setSerializedFormula(serializedFormula);
		this.setTrueVars(trueVars);
		this.setFalseVars(falseVars);
	}

	public SimpleNode getRootNode() {
		if(this.formulaRoot != null)
			return this.formulaRoot;
			
		if(this.serializedFormula == null) {
			return null;
		} else {
			try {
				ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(this.serializedFormula));
				this.formulaRoot = (SimpleNode) in.readObject();
				in.close();
			} catch (Exception e) {
				// this sucks...
				logger.error("Problem while deserializing formula", e);
				return null;
			}
		}
		this.serializedFormula = null;
		return this.formulaRoot;
	}
	
	private void setSerializedFormula(byte[] serializedFormula) {
		this.serializedFormula = serializedFormula;
	}

//	public byte[] getSerializedFormula() {
//		return serializedFormula;
//	}

	private void setTrueVars(ArrayList<Integer> trueVars) {
		this.trueVars = trueVars;
	}

	public ArrayList<Integer> getTrueVars() {
		return trueVars;
	}

	private void setFalseVars(ArrayList<Integer> falseVars) {
		this.falseVars = falseVars;
	}

	public ArrayList<Integer> getFalseVars() {
		return falseVars;
	}
	
	
}
