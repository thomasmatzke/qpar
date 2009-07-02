package main.java.slave;

public interface Tool {

	public void setPath(String path);
	public String getPath();
	
	public void setArguments(String[] args);
	public String[] getArguments();
	
	public String exec();
	public int kill();
	
}
