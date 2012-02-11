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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import qpar.common.Configuration;
import qpar.master.Master;
import qpar.master.TQbf;

/**
 * @author thomasm
 * 
 */
public abstract class AbstractJobScheduler implements Scheduler {

	private final BlockingQueue<TQbf> queue = new LinkedBlockingQueue<TQbf>(Master.configuration.getProperty(
			Configuration.SCHEDULING_QUEUE_SIZE, Integer.class));

	public BlockingQueue<TQbf> getQueue() {
		return this.queue;
	}

}