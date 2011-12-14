/*
Copyright (c) 2011 Thomas Matzke

This file is part of qpar.

qpar is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package qpar.master;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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

	public TQbf getWork() throws InterruptedException {
		TQbf ret = null;
		ret = queue.poll(3, TimeUnit.SECONDS);
		return ret;
	}

}