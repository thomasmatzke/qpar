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

import java.util.ArrayDeque;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qpar.common.parser.jjtree.Node;
import qpar.common.rmi.InterpretationData;

public class ReducedInterpretation {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReducedInterpretation.class);

	Node root;
	ArrayList<Integer> trueVars;
	ArrayList<Integer> falseVars;

	public ReducedInterpretation(final InterpretationData data) {

		this.root = data.getRootNode();
		this.trueVars = data.getTrueVars();
		this.falseVars = data.getFalseVars();

		TruthAssigner t = new TruthAssigner(this.root, this.trueVars, this.falseVars);
		ArrayDeque<Node> assignedNodes = t.assign();

		ArrayDeque<Node> reducableNodes = new ArrayDeque<Node>();
		for (Node sn : assignedNodes) {
			reducableNodes.add(sn.jjtGetParent());
		}

		Reducer r = new Reducer(reducableNodes);
		r.reduce();

		OrphanRemover ov = new OrphanRemover(this.root);
		ov.removeOrphans();

	}

	public Node getInterpretation() {
		return this.root;
	}

	public boolean isTruthValue() {
		if (this.root.isTruthNode()) {
			return true;
		}

		if (this.root.isStartNode() && this.root.getChildren()[0].isTruthNode()) {
			return true;
		}

		return false;
	}

	public boolean getTruthValue() {
		if (this.root.isTruthNode()) {
			return this.root.getTruth();
		} else if (this.root.isStartNode() && this.root.getChildren()[0].isTruthNode()) {
			return this.root.getChildren()[0].getTruth();
		} else {
			throw new IllegalStateException("Not truth values found");
		}
	}

	public byte[] getTreeHash() {
		return TreeHash.treeHash(this.root);
	}

}
