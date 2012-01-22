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
package qpar.slave.solver;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qpar.slave.tree.QproRepresentation;
import qpar.slave.tree.ReducedInterpretation;

public class CirqitPlugin implements SolverPlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(CirqitPlugin.class);

	ReducedInterpretation ri;
	Lock killLock = new ReentrantLock();
	ExecuteWatchdog watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
	boolean killed = false, started = false, terminated = false;
	DefaultExecutor executor = new DefaultExecutor();

	Boolean returnValue = null;
	Exception errorValue = null;

	private QproRepresentation qproRepresentation;

	private File inputFile = null;
	private String inputFilePath = null;

	private String qproInputString;

	public void initialize(final ReducedInterpretation ri) throws Exception {
		this.ri = ri;
		this.inputFile = File.createTempFile(UUID.randomUUID().toString(), null);
		this.inputFilePath = this.inputFile.getAbsolutePath();
	}

	public void kill() {
		this.killLock.lock();
		try {

			if (this.started) {
				this.watchdog.destroyProcess();
			}
			this.killed = true;

		} finally {
			this.killLock.unlock();
		}
	}

	public void run() {
		try {
			this.executor.setWatchdog(this.watchdog);
			CommandLine command = new CommandLine("CirQit2.1");
			DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
			command.addArgument(this.inputFilePath);

			this.qproRepresentation = new QproRepresentation(this.ri);
			FileWriter fw = new FileWriter(this.inputFile);
			this.qproInputString = this.qproRepresentation.getQproRepresentation();
			fw.write(this.qproInputString);
			fw.flush();
			fw.close();

			ByteArrayOutputStream output = new ByteArrayOutputStream();
			// logger.debug("QPROINPUT: \n" +
			// qproRepresentation.getQproRepresentation());

			this.killLock.lock();
			if (this.killed) {
				this.killLock.unlock();
				return;
			} else {
				this.executor.execute(command, resultHandler);
				this.started = true;
			}
			this.killLock.unlock();

			while (!resultHandler.hasResult()) {
				try {
					resultHandler.waitFor();
					this.watchdog.destroyProcess();
				} catch (InterruptedException e1) {
					LOGGER.error("", e1);
				}

			}

			this.returnValue = this.interpretCirqitOutput(resultHandler.getExitValue(), output.toString("ISO-8859-1"));

		} catch (Exception e) {
			// ri.getInterpretation().dump("++");
			this.errorValue = e;
		} finally {
			synchronized (this) {
				this.terminated = true;
				this.notifyAll();
			}
		}

	}

	synchronized public Boolean waitForResult() throws Exception {
		while (!this.terminated) {
			this.wait();
		}
		if (this.errorValue != null) {
			throw this.errorValue;
		}

		return this.returnValue;
	}

	private Boolean interpretCirqitOutput(final int retVal, final String output) throws Exception {
		this.killLock.lock();
		try {
			if (this.killed) {
				return null;
			}

			if (retVal == 10) {
				return true;
			} else if (retVal == 20) {
				return false;
			} else {
				LOGGER.error(String.format("Cirqit reported an error. Treating as error. \nString returned: %s \nQPro input: %s", output,
						this.qproInputString));
				throw new Exception(output);
			}

		} finally {
			this.killLock.unlock();
		}
	}

}
