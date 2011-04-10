package main.java.common.rmi;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Observable;
import java.util.Observer;

import main.java.master.Master;

import org.apache.log4j.Logger;

public class WrappedObserver implements Observer, Serializable {
	static Logger logger = Logger.getLogger(WrappedObserver.class);

	private static final long serialVersionUID = 1L;

	private RemoteObserver ro = null;

	public WrappedObserver(RemoteObserver ro) {
		this.ro = ro;
	}

	@Override
	public void update(final Observable o, final Object arg) {
		 logger.info("Updating");
		final WrappedObserver ref = this;

		Master.globalThreadPool.execute(new Runnable() {
			public void run() {
				try {
					ro.update(o, arg);
				} catch (RemoteException e) {
					logger.error("Remote exception removing observer:" + this, e);
					o.deleteObserver(ref);
				}
			}
		});

		// ro.update(ro, arg);

	}

}