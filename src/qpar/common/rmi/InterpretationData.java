/*
Copyright (c) 2011 Thomas Matzke

This file is part of qpar.

qpar is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
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
