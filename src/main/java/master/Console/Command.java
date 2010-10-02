package main.java.master.Console;

public enum Command {
	NEWJOB, STARTJOB, ABORTJOB, VIEWJOBS, EVALUATE,
	KILLSLAVE, VIEWSLAVES, WAITFORSLAVE, KILLALLSLAVES,
	HELP, QUIT,
	MAIL,
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
