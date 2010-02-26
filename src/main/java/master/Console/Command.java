package main.java.master.Console;

public enum Command {
	NEWJOB, STARTJOB, ABORTJOB, VIEWJOBS,
	KILLSLAVE, VIEWSLAVES, WAITFORSLAVE, KILLALLSLAVES,
	HELP, QUIT,
	SOURCE, WAITFORRESULT,
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
