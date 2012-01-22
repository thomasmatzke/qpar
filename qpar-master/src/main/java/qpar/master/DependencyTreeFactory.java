/**
 * 
 */
package qpar.master;

import java.util.ArrayList;

import qpar.common.parser.Node;
import qpar.master.heuristic.DependencyNode;

/**
 * @author thomasm
 * 
 */
public class DependencyTreeFactory {

	public static DependencyNode getDependencyTree(final Node node) {
		return dependencyTree(node)[0];
	}

	/**
	 * Returns a dependency Tree
	 */
	static private DependencyNode[] dependencyTree(final Node node) {
		DependencyNode dn = null;
		switch (node.getNodeType()) {
		case START:
			dn = new DependencyNode(0, DependencyNode.NodeType.ROOT);
		case FORALL:
			if (dn == null) {
				dn = new DependencyNode(node.getVar(), DependencyNode.NodeType.UNIVERSAL);
			}
		case EXISTS:
			if (dn == null) {
				dn = new DependencyNode(node.getVar(), DependencyNode.NodeType.EXISTENTIAL);
			}

			DependencyNode[] kids = dependencyTree(node.getChildren()[0]);
			assert (kids.length <= 2);

			for (DependencyNode d : kids) {
				dn.addChild(d);
			}

			if (dn.type == DependencyNode.NodeType.ROOT) {
				dn.setDepth(0);
			}
			DependencyNode[] ret = { dn };
			return ret;
		case NOT:
			return dependencyTree(node.getChildren()[0]);
		case VAR:
			return new DependencyNode[0];
		case AND:
		case OR:
			ArrayList<DependencyNode> ret1 = new ArrayList<DependencyNode>();

			DependencyNode[] d1 = dependencyTree(node.getChildren()[0]);
			DependencyNode[] d2 = dependencyTree(node.getChildren()[1]);
			if (d1.length > 0) {
				ret1.add(d1[0]);
			}
			if (d2.length > 0) {
				ret1.add(d2[0]);
			}

			return ret1.toArray(new DependencyNode[ret1.size()]);
		default:
			assert (false);
			return null;
		}
	}
}
