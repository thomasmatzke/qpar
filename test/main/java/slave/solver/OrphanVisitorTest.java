package main.java.slave.solver;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

import main.java.logic.parser.SimpleNode;
import main.java.logic.parser.SimpleNode.NodeType;

import org.junit.Before;
import org.junit.Test;

public class OrphanVisitorTest {
	ArrayList<Integer> quantifiedVars = new ArrayList<Integer>(Arrays.asList(1,2,3,4,5));
	
	SimpleNode root = null;
	
	@Before
	public void setUp() throws Exception {
		// setup small tree
		root = new SimpleNode();
		root.setNodeType(NodeType.AND);
		SimpleNode left = new SimpleNode();
		left.setNodeType(NodeType.VAR);
		left.setVar(1);
		SimpleNode right = new SimpleNode();
		right.setNodeType(NodeType.VAR);
		right.setVar(4);
		root.jjtAddChild(left, 0);
		root.jjtAddChild(right, 1);
	}

	@Test
	public void testOrphanVisitor() {
		OrphanVisitor o = new OrphanVisitor(quantifiedVars);
		o.visit(root);		
		assertEquals(new ArrayList<Integer>(Arrays.asList(2,3,5)), o.getOrpahns());
		//fail("Not yet implemented");
	}

}
