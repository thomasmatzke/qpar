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
package qpar.slave.tree;


import org.apache.log4j.Logger;

import qpar.master.logic.parser.Node;
import qpar.master.logic.parser.SimpleNode;

public class TreeStatistic {
	static Logger logger = Logger.getLogger(TreeStatistic.class);

	SimpleNode root = null;
	
	private int numVar, numAnd, numOr, numForall, numExists, numFalse, numTrue, numNot;
	
	public TreeStatistic(SimpleNode node) {
		this.root = node;
		count(this.root);
	}
			
	public int getNumNot() {
		return numNot;
	}

	public int getNumVar() {
		return numVar;
	}

	public int getNumAnd() {
		return numAnd;
	}

	public int getNumOr() {
		return numOr;
	}

	public int getNumForall() {
		return numForall;
	}

	public int getNumExists() {
		return numExists;
	}

	public int getNumFalse() {
		return numFalse;
	}

	public int getNumTrue() {
		return numTrue;
	}

	private void count(SimpleNode node) {
		switch(node.getNodeType()) {
			case FORALL:
				numForall++; break;
			case EXISTS:
				numExists++; break;
			case TRUE:
				numTrue++; break;
			case FALSE:
				numFalse++; break;
			case AND:
				numAnd++; break;
			case OR:
				numOr++; break;
			case VAR:
				numVar++; break;
			case NOT:
				numNot++; break;
			case START:
				break;
			default:
				logger.error("Encountered illegal NodeType: " + node.getNodeType());
				throw new IllegalStateException();
		}
		
		if(node.jjtGetNumChildren() != 0) {
			for(Node n : node.children) {
				count(((SimpleNode)n));
 			}
		} 		
	}
	
}
