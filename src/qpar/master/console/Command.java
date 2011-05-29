package qpar.master.console;

public enum Command {
	NEWJOB, STARTJOB, ABORTJOB, VIEWJOBS, EVALUATE,
	KILLSLAVE, VIEWSLAVES, WAITFORSLAVE, KILLALLSLAVES, SHUTDOWNALLSLAVES,
	HELP, QUIT,
	MAIL,
	SOURCE,
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
