package qpar.common;

import java.util.Set;
import java.util.Vector;
public class Permuter {

	Vector v = null;
	
	public Permuter(Vector v) {
		this.v = v;
	}
	
	public Permuter(Set s) {
		this.v = new Vector(s);
	}
	
	public Vector next() {
		Vector w = (Vector) v.clone();
		
		for (int k = w.size() - 1; k > 0; k--) {
		    int x = (int)Math.floor(Math.random() * (k+1));
		    Object temp = w.get(x);
		    w.set(x, w.get(k)); 
		    w.set(k, temp);
		}		
		return w;
	}
	
}

