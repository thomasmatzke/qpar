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
package qpar.slave.solver;

import java.util.HashMap;
import java.util.Map;

import qpar.common.parser.Node;
import qpar.common.parser.SimpleNode;
import qpar.slave.tree.ReducedInterpretation;

public class SimpleSolverPlugin implements SolverPlugin {

	private Node formula;
	private Boolean retResult = null;
	private Exception retException = null;
	private boolean run = true;
	private final Map<Integer, Boolean> currentAssignments = new HashMap<Integer, Boolean>();

	class KilledException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	public void run() {
		Node result;
		try {
			result = this.split(this.formula);
			if (result.isTrueNode()) {
				this.setResult(true);
			} else if (result.isFalseNode()) {
				this.setResult(false);
			} else {
				this.setException(new IllegalStateException("Truth node expected."));
			}
		} catch (KilledException e) {
			// We were killed. Do nothing.
		}
	}

	public Node split(final Node current) throws KilledException {
		if (!this.run) {
			throw new KilledException();
		}

		if (current.isTruthNode()) {
			return current;
		} else if (current.isOrNode()) {
			return this.disjunction(this.split(current.jjtGetChild(0)), this.split(current.jjtGetChild(1)));
		} else if (current.isAndNode()) {
			return this.conjunction(this.split(current.jjtGetChild(0)), this.split(current.jjtGetChild(1)));
		} else if (current.isExistsNode()) {

			this.currentAssignments.put(current.getVar(), true);
			this.simplify(current);
			Node resultTrue = this.split(current.jjtGetChild(0));
			this.currentAssignments.remove(current.getVar());
			this.saveSubtree(current);

			if (resultTrue.isTrueNode()) {
				return resultTrue;
			}

			this.currentAssignments.put(current.getVar(), false);
			this.simplify(current);
			Node resultFalse = this.split(current.jjtGetChild(0));
			this.currentAssignments.remove(current.getVar());

			return resultFalse;

		} else if (current.isForallNode()) {

			this.currentAssignments.put(current.getVar(), true);
			Node simplifyResult = this.simplify(current);
			Node resultTrue = this.split(current.jjtGetChild(0));

			this.restoreSubtree(current);

			this.currentAssignments.put(current.getVar(), false);
			this.simplify(current);
			Node resultFalse = this.split(current.jjtGetChild(0));

			this.currentAssignments.remove(current.getVar());

			if (resultTrue.getTruth() && resultFalse.getTruth()) {
				return resultTrue;
			}

			if (resultTrue.getTruth()) {
				return resultFalse;
			} else {
				return resultTrue;
			}

		} else if (current.isVarNode()) {
			Node truth = new SimpleNode();
			Boolean setTo = this.currentAssignments.get(current.getVar());
			if (setTo) {
				truth.setNodeType(Node.NodeType.TRUE);
			} else {
				truth.setNodeType(Node.NodeType.FALSE);
			}
			return truth;
		} else if (current.isNotNode()) {
			Node truth = this.split(current.jjtGetChild(0));
			if (truth.getTruth()) {
				truth.setNodeType(Node.NodeType.FALSE);
			} else {
				truth.setNodeType(Node.NodeType.TRUE);
			}
			return truth;
		} else if (current.isStartNode()) {
			return this.split(current.jjtGetChild(0));
		} else {
			throw new RuntimeException();
		}

	}

	public void initialize(final ReducedInterpretation ri) throws Exception {
		this.formula = ri.getInterpretation();
		this.saveSubtree(this.formula);
	}

	synchronized public void kill() {
		this.run = false;
		this.notifyAll();
	}

	synchronized public Boolean waitForResult() throws Exception {
		while (this.retResult == null && this.retException == null && this.run) {
			this.wait();
		}

		if (this.retException != null) {
			throw this.retException;
		} else {
			return this.retResult;
		}
	}

	synchronized private void setException(final Exception e) {
		this.retException = e;
		this.notifyAll();
	}

	synchronized private void setResult(final Boolean result) {
		this.retResult = result;
		this.notifyAll();
	}

	/**
	 * 
	 * @param formula
	 * @return
	 */
	private Node simplify(final Node formula) {
		// TODO: IMPLEMENT ALL THE THINGS!
		return formula;
	}

	private Node disjunction(final Node n1, final Node n2) {
		boolean result = n1.getTruth() || n2.getTruth();
		Node retNode = new SimpleNode();
		if (result) {
			retNode.setNodeType(Node.NodeType.TRUE);
		} else {
			retNode.setNodeType(Node.NodeType.FALSE);
		}
		return retNode;
	}

	private Node conjunction(final Node n1, final Node n2) {
		boolean result = n1.getTruth() && n2.getTruth();
		Node retNode = new SimpleNode();
		if (result) {
			retNode.setNodeType(Node.NodeType.TRUE);
		} else {
			retNode.setNodeType(Node.NodeType.FALSE);
		}
		return retNode;
	}

	private void saveSubtree(final Node root) {
		root.save();
		for (Node n : root.getChildren()) {
			this.saveSubtree(n);
		}
	}

	private void restoreSubtree(final Node root) {
		root.restore();
		for (Node n : root.getChildren()) {
			this.restoreSubtree(n);
		}
	}

}
