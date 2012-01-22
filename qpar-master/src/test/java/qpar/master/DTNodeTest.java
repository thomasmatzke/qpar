package qpar.master;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import qpar.master.DTNode.DTNodeType;

public class DTNodeTest {

	public DTNode orRoot;
	public DTNode andRoot;

	@Before
	public void setUp() throws Exception {
		// Setup an or tree
		this.orRoot = new DTNode(DTNodeType.OR);
		this.orRoot.addChild(new DTNode(DTNodeType.FALSE));
		this.orRoot.addChild(new DTNode(DTNodeType.TRUE));

		// Setup an and tree
		this.andRoot = new DTNode(DTNodeType.AND);
		this.andRoot.addChild(new DTNode(DTNodeType.TRUE));
		this.andRoot.addChild(new DTNode(DTNodeType.FALSE));
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
