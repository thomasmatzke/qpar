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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Distributor {

	private static final Logger LOGGER = LoggerFactory.getLogger(Distributor.class);

	private static Distributor instance;

	synchronized public static Distributor getInstance() {
		if (instance == null) {
			instance = new Distributor();
		}

		return instance;
	}

	private final BlockingQueue<TQbf> queue = new LinkedBlockingQueue<TQbf>(1000);

	public TQbf getWork() throws InterruptedException {
		TQbf ret = null;
		ret = this.queue.poll(3, TimeUnit.SECONDS);
		return ret;
	}

	public void scheduleJob(final Job j) {
		for (TQbf tqbf : j.subformulas) {
			try {
				this.queue.put(tqbf);
			} catch (InterruptedException e) {
				LOGGER.error("", e);
			}
			LOGGER.info("Added tqbf " + tqbf.getId() + " to distribution queue.");
		}
	}

}