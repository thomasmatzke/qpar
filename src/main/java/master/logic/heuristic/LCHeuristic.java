package main.java.master.logic.heuristic;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import main.java.master.logic.Qbf;

public class LCHeuristic extends Heuristic {

	private Vector<Integer> orderedVector;
	
	public LCHeuristic(Qbf qbf) {
		super(qbf);
		orderedVector = sortByCount(qbf.getLiteralCount());
	}
	
	public LinkedHashSet<Integer> sortGroup(Set<Integer> group) {
		LinkedHashSet<Integer> orderedGroup = new LinkedHashSet<Integer>();
		for(Integer i : orderedVector) {
			if(group.contains(i))
				orderedGroup.add(i);
		}
		return orderedGroup;
	}
	
	private Vector<Integer> sortByCount(Map<Integer,Integer> litCounts){
		Set 		set 		= litCounts.entrySet();
		Map.Entry[] entries 	= new Map.Entry[set.size()];
		Iterator 	iterator 	= set.iterator();
		int 		count 		= 0;
		Vector<Integer> res		= new Vector<Integer>();
		
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

	
	
}
