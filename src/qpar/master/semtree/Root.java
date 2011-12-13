package qpar.master.semtree;

import java.util.ArrayList;

public class Root extends ConnectingNode {

	private final int depth = 0;
	
	private int[] selectionOrder;
	private boolean generated = false;
	
	synchronized public void generateTree(int[] selectionOrder, ArrayList<Integer> eVars, ArrayList<Integer> aVars) {
		if(generated) throw new RuntimeException("Tree already generated.");
		
		this.selectionOrder = selectionOrder;
		
		
		
		generated = true;
	}
	
	synchronized public void mergeResult(String id, boolean result) {
		
	}
	
	synchronized public ArrayList<InterpretationNode> restartSubformula(String id) {
		
	}
	
}
