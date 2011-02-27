package main.java.scheduling;

public interface Scheduler {
	
	public enum State {NEW, STARTED, FINISHED, ABORTED};
	
	void startExecution();
	
	void abortExecution();
	
	public State waitFor();
}
