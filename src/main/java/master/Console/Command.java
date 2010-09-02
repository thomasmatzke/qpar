package main.java.master.Console;

public enum Command {
	NEWJOB, STARTJOB, ABORTJOB, VIEWJOBS, EVALUATE,
	KILLSLAVE, VIEWSLAVES, WAITFORSLAVE, KILLALLSLAVES,
	HELP, QUIT,
	SOURCE, WAITFORRESULT,
	TESTPARSER,
	NOVALUE;
	
	public static Command toCommand(String str)
    {
        try {
            return valueOf(str);
        } 
        catch (Exception ex) {
            return NOVALUE;
        }
    }   

}
