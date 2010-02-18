package main.java.master.Console;

public enum Command {
	NEWJOB, STARTJOB, ABORTJOB, VIEWJOBS,
	KILLSLAVE,
	HELP,
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
