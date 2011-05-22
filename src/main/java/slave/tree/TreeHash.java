package main.java.slave.tree;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import main.java.master.logic.parser.SimpleNode;

import org.apache.commons.lang3.ArrayUtils;

public class TreeHash {

	private static MessageDigest md;	
	
	
	public static byte[] treeHash(SimpleNode n) {
		switch(n.getNodeType()) {
			case START:
				return ArrayUtils.addAll("s".getBytes(), treeHash((SimpleNode)n.children[0]));
			case VAR:
				return ArrayUtils.addAll(Integer.valueOf(((n.getVar()))).toString().getBytes());
			case FORALL:
				return ArrayUtils.addAll("F".getBytes(), treeHash((SimpleNode)n.children[0]));
			case EXISTS:
				return ArrayUtils.addAll("E".getBytes(), treeHash((SimpleNode)n.children[0]));
			case AND:
				return ArrayUtils.addAll(ArrayUtils.addAll("a".getBytes(), treeHash((SimpleNode)n.children[0])), treeHash((SimpleNode)n.children[1]));
			case OR:
				return ArrayUtils.addAll(ArrayUtils.addAll("o".getBytes(), treeHash((SimpleNode)n.children[0])), treeHash((SimpleNode)n.children[1]));
			case NOT:
				return ArrayUtils.addAll("n".getBytes(), treeHash((SimpleNode)n.children[0]));
			case TRUE:
				return md().digest("t".getBytes());
			case FALSE:
				return md().digest("f".getBytes());
			default:
				throw new RuntimeException("Unknown Nodetype encountered: " + n.getNodeType());
		}
	}
	
	synchronized static private MessageDigest md() {
		if(TreeHash.md == null) {
			try {
				TreeHash.md = MessageDigest.getInstance("SHA");
			} catch (NoSuchAlgorithmException e) {}	
		}
		
		return TreeHash.md;
	}
	
//	public static <T> T[] concatAll(T[] first, T[]... rest) {
//	  int totalLength = first.length;
//	  for (T[] array : rest) {
//	    totalLength += array.length;
//	  }
//	  T[] result = Arrays.copyOf(first, totalLength);
//	  int offset = first.length;
//	  for (T[] array : rest) {
//	    System.arraycopy(array, 0, result, offset, array.length);
//	    offset += array.length;
//	  }
//	  return result;
//	}

	
}
