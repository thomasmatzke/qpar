package main.java.messages;

import java.io.Serializable;
import java.util.Vector;

/**
 * Holds Information about the slave. Is the first message 
 * transmitted to the master, to register the slave. The hostname is used 
 * as a unique id for the slave.
 * @author thomasm
 *
 */
public class InformationMessage implements Serializable {
	private int cores;
	private Vector<String> toolIds;
	private String hostName;

	public InformationMessage(int cores, Vector<String> toolIds, String hostName) {
		this.cores 		= cores;
		this.toolIds 	= toolIds;
		this.hostName 	= hostName;
	}
	
	public String getHostName() {
		return hostName;
	}

//	public void setHostName(String hostName) {
//		this.hostName = hostName;
//	}

	public int getCores() {
		return cores;
	}

//	public void setCores(int cores) {
//		this.cores = cores;
//	}

	public Vector<String> getToolIds() {
		return toolIds;
	}

//	public void setToolIds(Vector<String> toolIds) {
//		this.toolIds = toolIds;
//	}
}
