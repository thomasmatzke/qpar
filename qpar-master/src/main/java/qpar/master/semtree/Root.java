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
package qpar.master.semtree;

import java.util.ArrayList;

public class Root extends ConnectingNode {

	private final int depth = 0;

	private int[] selectionOrder;
	private boolean generated = false;

	synchronized public void generateTree(int[] selectionOrder, ArrayList<Integer> eVars, ArrayList<Integer> aVars) {
		if (generated)
			throw new RuntimeException("Tree already generated.");

		this.selectionOrder = selectionOrder;

		generated = true;
	}

	synchronized public void mergeResult(String id, boolean result) {

	}

	synchronized public ArrayList<InterpretationNode> restartSubformula(String id) {
		return null;
	}

}
