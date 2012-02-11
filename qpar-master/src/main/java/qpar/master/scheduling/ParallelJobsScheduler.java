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
package qpar.master.scheduling;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qpar.master.Job;
import qpar.master.TQbf;

public class ParallelJobsScheduler extends AbstractJobScheduler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ParallelJobsScheduler.class);

	@Override
	public TQbf pullWork() throws InterruptedException {
		TQbf ret = null;
		ret = this.getQueue().poll(3, TimeUnit.SECONDS);
		return ret;
	}

	@Override
	public void scheduleJob(final Job j) {
		for (TQbf tqbf : j.subformulas) {
			try {
				this.getQueue().put(tqbf);
			} catch (InterruptedException e) {
				LOGGER.error("", e);
			}
			LOGGER.info("Added tqbf " + tqbf.getId() + " to distribution queue.");
		}
	}

}