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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import qpar.master.Qbf;

public class HTestHeuristic extends Heuristic {

	@Override
	public LinkedHashSet<Integer> getVariableOrder(Qbf qbf) {
		//Integer[] order = new Integer[]{22, 45, 66, 43, 15, 60, 37, 55, 3, 50, 27, 34, 19, 29, 46, 23, 14, 8, 64, 44, 21, 20, 39, 41, 12, 5, 69, 63, 16, 53, 57, 40, 49, 25, 30, 17, 36, 38, 54, 35, 28, 4, 32, 42, 67, 56, 24, 48, 31, 51, 18, 52, 10, 13, 6, 26, 11, 7, 61, 59, 33, 58, 62, 65, 2, 47, 9, 68, 132, 102, 119, 126, 117, 110, 106, 85, 115, 101, 73, 125, 83, 105, 72, 113, 92, 122, 111, 130, 112, 70, 81, 99, 98, 96, 116, 94, 88, 76, 107, 91, 86, 74, 103, 93, 71, 95, 79, 108, 118, 97, 121, 84, 75, 123, 104, 90, 127, 124, 131, 80, 109, 87, 129, 78, 120, 100, 77, 133, 114, 89, 82, 128};
		Integer[] order = new Integer[]{66, 23, 14, 8, 64, 44, 37, 55, 3, 50, 27, 34, 43, 15, 60, 45, 22, 19, 29, 46, 21, 20, 39, 41, 12, 5, 69, 63, 16, 53, 57, 40, 49, 25, 30, 17, 36, 38, 54, 35, 28, 4, 32, 42, 67, 56, 24, 48, 31, 51, 18, 52, 10, 13, 6, 26, 11, 7, 61, 59, 33, 58, 62, 65, 2, 47, 9, 68, 132, 102, 119, 126, 117, 110, 106, 85, 115, 101, 73, 125, 83, 105, 72, 113, 92, 122, 111, 130, 112, 70, 81, 99, 98, 96, 116, 94, 88, 76, 107, 91, 86, 74, 103, 93, 71, 95, 79, 108, 118, 97, 121, 84, 75, 123, 104, 90, 127, 124, 131, 80, 109, 87, 129, 78, 120, 100, 77, 133, 114, 89, 82, 128};
		//Integer[] order = new Integer[]{65, 23, 14, 8, 64, 44, 37, 55, 3, 50, 27, 34, 43, 15, 60, 45, 22, 19, 29, 46, 21, 20, 39, 41, 12, 5, 69, 63, 16, 53, 57, 40, 49, 25, 30, 17, 36, 38, 54, 35, 28, 4, 32, 42, 67, 56, 24, 48, 31, 51, 18, 52, 10, 13, 6, 26, 11, 7, 61, 59, 33, 58, 62, 65, 2, 47, 9, 68, 132, 102, 119, 126, 117, 110, 106, 85, 115, 101, 73, 125, 83, 105, 72, 113, 92, 122, 111, 130, 112, 70, 81, 99, 98, 96, 116, 94, 88, 76, 107, 91, 86, 74, 103, 93, 71, 95, 79, 108, 118, 97, 121, 84, 75, 123, 104, 90, 127, 124, 131, 80, 109, 87, 129, 78, 120, 100, 77, 133, 114, 89, 82, 128};
		
		this.getDecisionGroups(qbf);
		
		return new LinkedHashSet<Integer>(Arrays.asList(order));
	}
	
	@Override
	public LinkedHashSet<Integer> sortGroup(Set<Integer> group, Qbf qbf) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId() {
		return "htest";
	}

}
