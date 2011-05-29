package main.java.logic;

import static org.junit.Assert.*;


import org.junit.Before;
import org.junit.Test;

import qpar.master.DTNode;
import qpar.master.DTNode.DTNodeType;

public class DTNodeTest {

	public DTNode orRoot;
	public DTNode andRoot;
	
	@Before
	public void setUp() throws Exception {
		// Setup an or tree
		orRoot = new DTNode(DTNodeType.OR);
		orRoot.addChild(new DTNode(DTNodeType.FALSE));
		orRoot.addChild(new DTNode(DTNodeType.TRUE));
		
		// Setup an and tree
		andRoot = new DTNode(DTNodeType.AND);
		andRoot.addChild(new DTNode(DTNodeType.TRUE));
		andRoot.addChild(new DTNode(DTNodeType.FALSE));
	}

	@Test
	public void testReduceAnd() {
		this.andRoot.getLeftChild().reduce();
		assertEquals(DTNodeType.FALSE, this.andRoot.type);
	}
	
	@Test
	public void testReduceOr() {
		this.orRoot.getRightChild().reduce();
		assertEquals(DTNodeType.TRUE, this.orRoot.type);
	}

}
