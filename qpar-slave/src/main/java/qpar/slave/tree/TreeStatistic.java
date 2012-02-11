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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qpar.common.parser.jjtree.Node;

public class TreeStatistic {
	private static final Logger LOGGER = LoggerFactory.getLogger(TreeStatistic.class);

	Node root = null;

	private int numVar, numAnd, numOr, numForall, numExists, numFalse, numTrue, numNot;

	public TreeStatistic(final Node node) {
		this.root = node;
		this.count(this.root);
	}

	public int getNumNot() {
		return this.numNot;
	}

	public int getNumVar() {
		return this.numVar;
	}

	public int getNumAnd() {
		return this.numAnd;
	}

	public int getNumOr() {
		return this.numOr;
	}

	public int getNumForall() {
		return this.numForall;
	}

	public int getNumExists() {
		return this.numExists;
	}

	public int getNumFalse() {
		return this.numFalse;
	}

	public int getNumTrue() {
		return this.numTrue;
	}

	private void count(final Node node) {
		switch (node.getNodeType()) {
		case FORALL:
			this.numForall++;
			break;
		case EXISTS:
			this.numExists++;
			break;
		case TRUE:
			this.numTrue++;
			break;
		case FALSE:
			this.numFalse++;
			break;
		case AND:
			this.numAnd++;
			break;
		case OR:
			this.numOr++;
			break;
		case VAR:
			this.numVar++;
			break;
		case NOT:
			this.numNot++;
			break;
		case START:
			break;
		default:
			LOGGER.error("Encountered illegal NodeType: " + node.getNodeType());
			throw new IllegalStateException();
		}

		if (node.jjtGetNumChildren() != 0) {
			for (Node n : node.getChildren()) {
				this.count(n);
			}
		}
	}

	public String compare(final Node other) {
		TreeStatistic otherStatistic = new TreeStatistic(other);
		int deltaAnd = otherStatistic.getNumAnd() - this.getNumAnd();
		int deltaOr = otherStatistic.getNumOr() - this.getNumOr();
		int deltaTrue = otherStatistic.getNumTrue() - this.getNumTrue();
		int deltaFalse = otherStatistic.getNumFalse() - this.getNumFalse();
		int deltaForall = otherStatistic.getNumForall() - this.getNumForall();
		int deltaExists = otherStatistic.getNumExists() - this.getNumExists();
		int deltaVar = otherStatistic.getNumVar() - this.getNumVar();
		int deltaNot = otherStatistic.getNumNot() - this.getNumNot();

		String report = String
				.format("Reduce Statistic: DeltaAnd: %d, DeltaOr: %d, DeltaTrue: %d, DeltaFalse: %d, DeltaNot: %d, DeltaForall: %d, DeltaExists: %d, DeltaVar: %d",
						deltaAnd, deltaOr, deltaTrue, deltaFalse, deltaNot, deltaForall, deltaExists, deltaVar);

		return report;
	}

}
