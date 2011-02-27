package main.java.scheduling;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

import main.java.master.Job;
import main.java.master.Master;
import main.java.master.Job.Status;
import main.java.master.SlaveRegistry;
import main.java.rmi.SlaveRemote;

public class BestFitScheduler implements Scheduler, Observer {
	
	static Logger logger = Logger.getLogger(BestFitScheduler.class);
	
	volatile State state = State.NEW;
	
//	Collection<SlaveRemote> slaves = SlaveRegistry.instance().getSlaves().values();
	List<Job> jobsTodo;
	List<Job> jobsDone = new LinkedList<Job>();
	List<Thread> threads = new ArrayList<Thread>();
	
	public BestFitScheduler(List<Job> jobs) {
		this.jobsTodo = jobs;
		SlaveRegistry.instance().addObserver(this);
	}
	
	@Override
	public void startExecution() {
		this.state = State.STARTED;
		Collections.sort(jobsTodo, new JobCoresComparator());
		
		while(state == State.STARTED) {
			int freeCores = SlaveRegistry.instance().freeCores();			
			if(jobsTodo.isEmpty())
				break;
			Job j = findBestFit(freeCores);
			if(j == null) {
				// Not enough free cores found probably... waiting for more
				try { synchronized(this){ wait(); } } catch (InterruptedException e) {}
				continue;
			}				

			threads.add(startJob(j));

			while(j.startedComputations < j.usedCores){
				try { synchronized(this){ wait(); } } catch (InterruptedException e) {}
			}
			jobsTodo.remove(j);

		}

		// waiting for the rest to finish
		while(!jobsTodo.isEmpty()) {
			try { synchronized(this){ wait(); } } catch (InterruptedException e) {}
			if(state == State.ABORTED)
				return;
		}
		
		// wait for all threads to finish
		try {
			for(Thread t : threads)
				t.join();
		} catch (InterruptedException e) {}
		
		state = State.FINISHED;
	}

	@Override
	public void abortExecution() {
		this.state = State.ABORTED;
		synchronized(this){ notifyAll(); }
	}
	
	public State waitFor() {
		while(state == State.STARTED) {
			synchronized(this) {
				if(state != State.STARTED)
					return state;
				try { synchronized(this){ wait(); } } catch (InterruptedException e) {}
			}
		}
		return state;
	}

	static class JobCoresComparator implements Comparator<Job>, Serializable {
		private static final long serialVersionUID = -2001093867166377914L;

		@Override
		public int compare(Job o1, Job o2) {
			return  o2.usedCores - o1.usedCores;
		}
	}
	
	

	@Override
	public void update(Observable arg0, Object arg1) {
		if(arg0 instanceof Job){
			Job j = (Job)arg0;
			if(j.status != Job.Status.RUNNING) {
				jobsDone.add(j);
			}
		}		
		
		synchronized(this){ notifyAll(); }
	}
	
	private Thread startJob(final Job job) {
		job.addObserver(this);
		Thread t = new Thread() {
            @Override
			public void run() {
                 job.startBlocking();
            }};
        t.start();
        return t;
	}
	
	private Job findBestFit(int freeCores) {
		
		for(Job job : jobsTodo) {
			if(job.usedCores <= freeCores)
				return job;
		}
		return null;
	}
	
}
