package qpar.master.heuristic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import qpar.master.Qbf;


public class LCHeuristic extends Heuristic {
		
	public LinkedHashSet<Integer> sortGroup(Set<Integer> group, Qbf qbf) {
		ArrayList<Integer> orderedVector = sortByCount(qbf.getLiteralCount(), qbf);
		LinkedHashSet<Integer> orderedGroup = new LinkedHashSet<Integer>();
		for(Integer i : orderedVector) {
			if(group.contains(i))
				orderedGroup.add(i);
		}
		return orderedGroup;
	}
	
	private ArrayList<Integer> sortByCount(Map<Integer,Integer> litCounts, Qbf qbf){
		Set 		set 		= litCounts.entrySet();
		Map.Entry[] entries 	= new Map.Entry[set.size()];
		Iterator 	iterator 	= set.iterator();
		int 		count 		= 0;
		ArrayList<Integer> res	= new ArrayList<Integer>();
		
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
		return "litcount";
	}

	
	
}
