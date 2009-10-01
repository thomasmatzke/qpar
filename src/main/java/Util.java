package main.java;

public class Util {

	public static String join(String[] strings, String separator) {
	    StringBuffer sb = new StringBuffer();
	    for (int i=0; i < strings.length; i++) {
	        if (i != 0) sb.append(separator);
	  	    sb.append(strings[i]);
	  	}
	  	return sb.toString();
	}
	
}
