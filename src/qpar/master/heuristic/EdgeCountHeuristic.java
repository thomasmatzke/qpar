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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import qpar.master.Qbf;
import qpar.master.logic.parser.Node;
import qpar.master.logic.parser.SimpleNode;

public class EdgeCountHeuristic extends Heuristic {

	static Logger logger = Logger.getLogger(EdgeCountHeuristic.class);
		
	@Override
	public LinkedHashSet<Integer> sortGroup(Set<Integer> group, Qbf qbf) {
		
		HashMap<Integer, Integer> coveredEdges = edgeCount(qbf.root);
		
		ArrayList<Integer> sorted = sortByCount(coveredEdges);
		
		LinkedHashSet<Integer> ret = new LinkedHashSet<Integer>();
		for(Integer i : sorted) {
			if(group.contains(i))
				ret.add(i);
		}	
		
		return ret;
	}
	
	private HashMap<Integer, Integer> edgeCount(Node n) {
		if(n.getNodeType() != SimpleNode.NodeType.VAR) {
			HashMap<Integer, Integer> combined = new HashMap<Integer, Integer>();
			for(int i = 0; i < n.jjtGetNumChildren(); i++) {
				for(Map.Entry<Integer, Integer> entry : edgeCount(n.jjtGetChild(i)).entrySet()) {
					if(combined.containsKey(entry.getKey()))
						combined.put(entry.getKey(), combined.get(entry.getKey()) + entry.getValue());
					else
						combined.put(entry.getKey(), entry.getValue());	
				}
			}
			if(n.getNodeType() != SimpleNode.NodeType.NOT) {
				for(Integer i : combined.values())
					i = i + 1;
			}
			return combined;
		}
		HashMap<Integer, Integer> ret = new HashMap<Integer, Integer>();
		ret.put(Integer.valueOf(n.getVar()), 1);
		return ret;
	}
	
	private ArrayList<Integer> sortByCount(Map<Integer,Integer> edgeCounts){
		Set 		set 		= edgeCounts.entrySet();
		Map.Entry[] entries 	= new Map.Entry[set.size()];
		Iterator 	iterator 	= set.iterator();
		int 		count 		= 0;
		ArrayList<Integer> res		= new ArrayList<Integer>();
		
		while(iterator.hasNext()) {
			entries[count++] = (Map.Entry) iterator.next();
		}
		
		Arrays.sort(entries, new Comparator() {
			public int compareTo(Object lhs, Object rhs) {
				Map.Entry le = (Map.Entry)lhs;
				Map.Entry re = (Map.Entry)rhs;
				return ((Comparable<Comparable>)le.getValue()).compareTo((Comparable)re.getValue());
			}

			@Override
			public int compare(Object lhs, Object rhs) {
				return compareTo(lhs, rhs);
			}
		});
		
		for(int i = entries.length-1; i >= 0; i--) {
			res.add((Integer) entries[i].getKey());
		}
		return res;
	}

	@Override
	public String getId() {
		return "edgecount";
	}

}
