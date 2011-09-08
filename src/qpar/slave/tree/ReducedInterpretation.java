package qpar.slave.tree;

import java.util.ArrayDeque;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import qpar.common.rmi.InterpretationData;
import qpar.master.logic.parser.SimpleNode;

public class ReducedInterpretation {

	static Logger logger = Logger.getLogger(ReducedInterpretation.class);
	
	SimpleNode root;
	ArrayList<Integer> trueVars;
	ArrayList<Integer> falseVars;
	
	public ReducedInterpretation(InterpretationData data) {
		
		this.root = data.getRootNode();
		this.trueVars = data.getTrueVars();
		this.falseVars = data.getFalseVars();
		
		TruthAssigner t = new TruthAssigner(root, trueVars, falseVars);
		ArrayDeque<SimpleNode> assignedNodes = t.assign();
		
		ArrayDeque<SimpleNode> reducableNodes = new ArrayDeque<SimpleNode>();
		for(SimpleNode sn : assignedNodes) {
			reducableNodes.add((SimpleNode)sn.jjtGetParent());
		}
		
		
TreeStatistic preStat = new TreeStatistic(this.root);
		
		Reducer r = new Reducer(reducableNodes);
		r.reduce();
		
TreeStatistic postStat = new TreeStatistic(this.root);
		
int deltaAnd = postStat.getNumAnd() - preStat.getNumAnd();
int deltaOr = postStat.getNumOr() - preStat.getNumOr();
int deltaTrue = postStat.getNumTrue() - preStat.getNumTrue();
int deltaFalse = postStat.getNumFalse() - preStat.getNumFalse();
int deltaForall = postStat.getNumForall() - preStat.getNumForall();
int deltaExists = postStat.getNumExists() - preStat.getNumExists();
int deltaVar = postStat.getNumVar() - preStat.getNumVar();
int deltaNot = postStat.getNumNot() - preStat.getNumNot();
		
logger.debug("Reduce Statistic: DeltaAnd: " + deltaAnd + 
		", DeltaOr: " + deltaOr +
		", DeltaTrue: " + deltaTrue + 
		", DeltaFalse: " + deltaFalse + 
		", DeltaNot: " + deltaNot + 
		", DeltaForall: " + deltaForall + 
		", DeltaExists: " + deltaExists + 
		", DeltaVar: " + deltaVar);
		
		OrphanRemover ov = new OrphanRemover(this.root);
		ov.removeOrphans();
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
	
	public byte[] getTreeHash() {
		return TreeHash.treeHash(this.root);
	}
	
}
