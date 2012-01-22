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

import java.util.ArrayList;

public class HeuristicFactory {

	private static ArrayList<String> heuristics;

	public static ArrayList<String> getAvailableHeuristics() {
		if (heuristics == null) {
			heuristics = new ArrayList<String>();
			heuristics.add("simple");
			heuristics.add("rand");
			heuristics.add("litcount");
			heuristics.add("probnet");
			heuristics.add("shallow");
			heuristics.add("edgecount");
			// heuristics.add("htest");
		}
		return heuristics;
	}

	public static Heuristic getHeuristic(String id) {
		if (id.equals("simple"))
			return new SimpleHeuristic();
		if (id.equals("rand"))
			return new RandHeuristic();
		if (id.equals("litcount"))
			return new LCHeuristic();
		if (id.equals("probnet"))
			return new SimpleProbNetHeuristic();
		if (id.equals("shallow"))
			return new ShallowHeuristic();
		if (id.equals("edgecount"))
			return new EdgeCountHeuristic();
		if (id.equals("htest"))
			return new HTestHeuristic();
		return null;
	}
}
