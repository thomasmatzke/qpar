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
package qpar.common;

import java.util.ArrayList;
import java.util.Set;

public class Permuter {

	ArrayList<?> v = null;

	public Permuter(final ArrayList<?> v) {
		this.v = v;
	}

	public Permuter(final Set<?> s) {
		this.v = new ArrayList<Object>(s);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList next() {
		ArrayList w = (ArrayList<?>) this.v.clone();

		for (int k = w.size() - 1; k > 0; k--) {
			int x = (int) Math.floor(Math.random() * (k + 1));
			Object temp = w.get(x);
			w.set(x, w.get(k));
			w.set(k, temp);
		}
		return w;
	}

}
