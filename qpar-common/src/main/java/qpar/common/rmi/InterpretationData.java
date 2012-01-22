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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qpar.common.parser.Node;

/**
 * Data needed for the slave to compute a interpretation
 * 
 * @author thomasm
 * 
 */
public class InterpretationData implements Serializable {
	private static final long serialVersionUID = -1499438525247990907L;

	private static final Logger LOGGER = LoggerFactory.getLogger(InterpretationData.class);

	private byte[] serializedFormula;
	private ArrayList<Integer> trueVars = new ArrayList<Integer>();
	private ArrayList<Integer> falseVars = new ArrayList<Integer>();

	private Node formulaRoot = null;

	public InterpretationData(final byte[] serializedFormula, final ArrayList<Integer> trueVars, final ArrayList<Integer> falseVars) {
		this.setSerializedFormula(serializedFormula);
		this.setTrueVars(trueVars);
		this.setFalseVars(falseVars);
	}

	public ArrayList<Integer> getFalseVars() {
		return this.falseVars;
	}

	public Node getRootNode() {
		if (this.formulaRoot != null) {
			return this.formulaRoot;
		}

		if (this.serializedFormula == null) {
			return null;
		} else {
			try {
				ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(this.serializedFormula));
				this.formulaRoot = (Node) in.readObject();
				in.close();
			} catch (Exception e) {
				// this sucks...
				LOGGER.error("Problem while deserializing formula", e);
				return null;
			}
		}
		this.serializedFormula = null;
		return this.formulaRoot;
	}

	public ArrayList<Integer> getTrueVars() {
		return this.trueVars;
	}

	private void setFalseVars(final ArrayList<Integer> falseVars) {
		this.falseVars = falseVars;
	}

	private void setSerializedFormula(final byte[] serializedFormula) {
		this.serializedFormula = serializedFormula;
	}

	private void setTrueVars(final ArrayList<Integer> trueVars) {
		this.trueVars = trueVars;
	}

}
