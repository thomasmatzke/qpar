package main.java.logic.heuristic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DependencyNode {

	public static Map<Integer, DependencyNode> registry = new HashMap<Integer, DependencyNode>();
	
	public DependencyNode(Integer variable, NodeType type) {
		this.variable = variable;
		this.type = type;
		if(type == NodeType.ROOT) {
			this.depth = 0;
		}
		DependencyNode.registry.put(variable, this);
	}
	
	public enum NodeType { ROOT, UNIVERSAL, EXISTENTIAL }
	
	public Integer depth;
	public NodeType type;
	public Integer variable;
	public Set<DependencyNode> children = new HashSet<DependencyNode>(); 
	
	public void addChild(DependencyNode child) {
		child.depth = this.depth + 1;
		children.add(child);
	}
	
}
