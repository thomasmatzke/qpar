package main.java.master.Console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class Shell implements Runnable{

	private String prompt 	= ">";
	BufferedReader in 		= new BufferedReader(new InputStreamReader(System.in));
	boolean run				= true;
	
	
	public void run() {	
		String line;
		while(run) {
			put(prompt);
			line = read();
			if(line.length() < 1) continue;
			StringTokenizer token = new StringTokenizer(line);
			switch (Command.valueOf(token.nextToken().toUpperCase()))
			{
				case NEWJOB:
					newjob(token);
					break;
				case STARTJOB:
					startjob(token);
					break;
				case ABORTJOB:
					abortjob(token);
					break;
				case VIEWJOBS:
					viewjob(token);
					break;
				case KILLSLAVE:
					killslave(token);
					break;
				case HELP:
					help();
					break;
				case NOVALUE:
					puts("Command not found");
			        break;
			    default:
			        assert(false);
			}

		}
	}

	private void help() {
		puts("Allowed comands are NEWJOB, STARTJOB, ABORTJOB, VIEWJOBS, KILLSLAVE, HELP");
	}

	private void killslave(StringTokenizer token) {
		// TODO Auto-generated method stub
		
	}

	private void viewjob(StringTokenizer token) {
		// TODO Auto-generated method stub
		
	}

	private void abortjob(StringTokenizer token) {
		// TODO Auto-generated method stub
		
	}

	private void startjob(StringTokenizer token) {
		// TODO Auto-generated method stub
		
	}

	private void newjob(StringTokenizer token) {
		// TODO Auto-generated method stub
		
	}

	private void put(String s) {
		System.out.print(s);
	}
	
	private void puts(String s) {
		System.out.println(s);
	}
	
	private String read() {
		String line = null;
		try {
			line = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return line;
	}
	
}
