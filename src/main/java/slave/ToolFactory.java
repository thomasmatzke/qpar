package main.java.slave;

public class ToolFactory {
	
	public Tool getToolByName(String name) {
		if(name == "qpro") {
			return new QProTool();
		} // TODO: Add code for more tools here
		return null;
	}
	
}
