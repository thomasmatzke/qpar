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
package qpar.master.heuristic;

import java.util.LinkedHashSet;
import java.util.Set;

import qpar.common.dom.formula.Qbf;
import qpar.common.parser.jjtree.SimpleNode;

public class SimpleHeuristic extends AbstractHeuristic {

	LinkedHashSet<Integer> ret = new LinkedHashSet<Integer>();

	public LinkedHashSet<Integer> getVariableOrder(Qbf qbf) {

		traverse(qbf.root);

		return ret;
	}

	@Override
	public LinkedHashSet<Integer> sortGroup(Set<Integer> group, Qbf qbf) {
		assert (false);
		return null;
	}

	public void traverse(SimpleNode n) {
		switch (n.getNodeType()) {
		case START:
			assert (n.jjtGetNumChildren() == 1);
			traverse((SimpleNode) n.jjtGetChild(0));
			break;
		case FORALL:
		case EXISTS:
			ret.add(n.var);
			assert (n.jjtGetNumChildren() == 1);
			traverse((SimpleNode) n.jjtGetChild(0));
			break;
		case NOT:
			assert (n.jjtGetNumChildren() == 1);
			traverse((SimpleNode) n.jjtGetChild(0));
			break;
		case AND:
		case OR:
			assert (n.jjtGetNumChildren() == 2);
			traverse((SimpleNode) n.jjtGetChild(0));
			traverse((SimpleNode) n.jjtGetChild(1));
			break;
		case VAR:
			break;
		default:
			assert (false);
		}
	}

	@Override
	public String getId() {
		return "simple";
	}

}
