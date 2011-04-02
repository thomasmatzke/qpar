package main.java.tree;

import java.util.ArrayDeque;
import java.util.ArrayList;

import main.java.logic.parser.SimpleNode;
import main.java.rmi.InterpretationData;

public class ReducedInterpretation {

	SimpleNode root;
	ArrayList<Integer> trueVars;
	ArrayList<Integer> falseVars;
	
	public ReducedInterpretation(InterpretationData data) throws Exception {
		this.root = data.getRootNode().deepCopy();
		this.trueVars = data.getTrueVars();
		this.falseVars = data.getFalseVars();
		
		TruthAssigner t = new TruthAssigner(root, trueVars, falseVars);
		ArrayDeque<SimpleNode> assignedNodes = t.assign();
		
		Reducer r = new Reducer(assignedNodes);
		r.reduce();
	}

	public SimpleNode getInterpretation() {
		return root;
	}
	
	public boolean isTruthValue() {
		if(root.isTruthNode())
			return true;
		
		if(root.isStartNode() && ((SimpleNode)root.children[0]).isTruthNode()) {
			return true;
		} 
		
		return false;
	}
	
	public boolean getTruthValue() {
		if(root.isTruthNode()) {
			return root.getTruth();
		} else if(root.isStartNode() && ((SimpleNode)root.children[0]).isTruthNode()) {
			return ((SimpleNode)root.children[0]).getTruth();
		} else {
			throw new IllegalStateException("Not truth values found");
		}
	}
}
