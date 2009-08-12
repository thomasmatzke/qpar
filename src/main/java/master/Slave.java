package main.java.master;

public class Slave {
	private int cores, speed, ram_free, ram_total;
	private String toolId;
	
	public int getCores() {
		return cores;
	}
	public void setCores(int cores) {
		this.cores = cores;
	}
	public int getSpeed() {
		return speed;
	}
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	public int getRam_free() {
		return ram_free;
	}
	public void setRam_free(int ram_free) {
		this.ram_free = ram_free;
	}
	public int getRam_total() {
		return ram_total;
	}
	public void setRam_total(int ram_total) {
		this.ram_total = ram_total;
	}
	public String getToolId() {
		return toolId;
	}
	public void setToolId(String toolId) {
		this.toolId = toolId;
	}
	
	
}
