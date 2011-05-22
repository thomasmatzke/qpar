package main.java.master;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

public class Distributor {

	private static final long serialVersionUID = -6606810196441096609L;

	static Logger logger = Logger.getLogger(Distributor.class);
	
	private volatile static Distributor instance;
	
	private BlockingQueue<TQbf> queue = new LinkedBlockingQueue<TQbf>(1000);
		
	synchronized public static Distributor instance() {
		if(instance == null)
			instance = new Distributor();
		
		return instance;
	}
	
	public void scheduleJob(Job j) {
		for(TQbf tqbf : j.subformulas) {		
			try { queue.put(tqbf); } catch (InterruptedException e) {logger.error("", e);}
			logger.info("Added tqbf " + tqbf.getId() + " to distribution queue.");
		}
	}

	public TQbf getWork() {
//		logger.info("Taking workunit. queue size: " + this.queue.size());
		TQbf ret = null;
		while(ret == null) {
			try { ret = queue.take(); } catch (InterruptedException e) {}
		}
//		logger.info("took workunit");
		return ret;
	}

}