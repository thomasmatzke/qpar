package main.java;

/**
 * Program execution entry point
 * @author thomasm
 *
 */
public class QPar {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/* TODO Use only configuration file? java arguments suck...
		 		parse following options:
		 		"mode"					-> 	slave or master, 
		 									slave passes received qbf to qpro instance(s),
		 									master distributes sub-qbfs to slaves
		 		"num_local_processes" 	-> 	Number of processes to start locally
		 		"remote_server"			-> 	Multiple entries (denote location of slaves)
		 									(Autodiscovery?)
		*/
	}
	
	public static String join(String[] strings, String separator) {
	    StringBuffer sb = new StringBuffer();
	    for (int i=0; i < strings.length; i++) {
	        if (i != 0) sb.append(separator);
	  	    sb.append(strings[i]);
	  	}
	  	return sb.toString();
	}


}
