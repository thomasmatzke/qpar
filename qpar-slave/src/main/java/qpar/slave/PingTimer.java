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
package qpar.slave;

import java.rmi.RemoteException;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingTimer extends TimerTask {

	private static final Logger LOGGER = LoggerFactory.getLogger(PingTimer.class);

	int interval;

	Slave slave = null;

	public PingTimer(final int interval, final Slave slave) {
		this.slave = slave;
		this.interval = interval;
		Timer t = new Timer();
		t.schedule(this, 0, interval * 1000);
	}

	@Override
	public void run() {
		// Make a call to the master. look for exceptions to see if
		// the master died. then try to reconnect

		try {
			this.slave.getMaster().ping();
		} catch (RemoteException e) {
			if (this.slave.connected) {
				LOGGER.error("Ping to master failed");
				this.slave.connect();
			}
		}

	}

}
