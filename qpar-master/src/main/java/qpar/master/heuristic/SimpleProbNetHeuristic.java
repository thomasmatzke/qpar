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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import qpar.common.dom.formula.Qbf;

public class SimpleProbNetHeuristic extends AbstractHeuristic {

	@Override
	public LinkedHashSet<Integer> sortGroup(Set<Integer> group, Qbf qbf) {
		Set<Integer> universals = new HashSet<Integer>();
		Set<Integer> existentials = new HashSet<Integer>();
		LinkedHashSet<Integer> sorted = new LinkedHashSet<Integer>();

		for (Integer i : group) {
			if (qbf.isUniversalQuantified(i))
				universals.add(i);
			else
				existentials.add(i);
		}

		if (qbf.root.getTruthProbability() > 0.5) {
			sorted.addAll(existentials);
			sorted.addAll(universals);
		} else {
			sorted.addAll(universals);
			sorted.addAll(existentials);
		}

		return sorted;
	}

	@Override
	public String getId() {
		return "probnet";
	}

}
