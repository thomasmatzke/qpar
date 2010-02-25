package main.java.master.Console;

public enum Command {
	NEWJOB, STARTJOB, ABORTJOB, VIEWJOBS,
	KILLSLAVE, VIEWSLAVES,
	HELP, QUIT,
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
