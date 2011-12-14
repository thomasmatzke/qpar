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

import java.util.HashSet;

import org.apache.log4j.Logger;

import qpar.master.logic.parser.Node;
import qpar.master.logic.parser.SimpleNode;

public class OrphanRemover {

	static Logger logger = Logger.getLogger(OrphanRemover.class);
	
	SimpleNode root = null;
	HashSet<Integer> variables;
	
	
	public OrphanRemover(SimpleNode node) {
		this.root = node;
		this.variables = this.root.getVariableSet();	
	}
	
	public void removeOrphans() { 
		this.removeOrphans(this.root);
	}
	
	private void removeOrphans(SimpleNode n) {
		if(n.isQuantifierNode() && !variables.contains(n.getVar()))
				n.cutOutQuantifierNode();
		
		if(n.jjtGetNumChildren() == 0)
			return;
		
		for(Node child : n.children) {
			removeOrphans(((SimpleNode)child));
		}
	}
			
}
