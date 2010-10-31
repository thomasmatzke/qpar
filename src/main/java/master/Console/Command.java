package main.java.master.Console;

public enum Command {
	NEWJOB, STARTJOB, ABORTJOB, VIEWJOBS, EVALUATE,
	KILLSLAVE, VIEWSLAVES, WAITFORSLAVE, KILLALLSLAVES, SHUTDOWNALLSLAVES,
	HELP, QUIT,
	MAIL, MAIL_EVALUATION_REPORT,
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
