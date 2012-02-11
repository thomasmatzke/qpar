import static org.junit.Assert.fail;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import qpar.master.Master;
import qpar.slave.Slave;

/**
 * 
 */

/**
 * @author thomasm
 * 
 */
public class SingleFormulaIT {

	ExecutorService masterExecutorService = Executors.newSingleThreadExecutor();

	Runnable masterRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				Master.main(new String[] { "-i", "src/test/resources/SingleFormulaIT.batch" });
			} catch (Throwable e) {
				fail(String.format("Exception was thrown: %s", e));
			}
		}
	};

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	@Test(timeout = 65000)
	public void test() {
		// Start master

		this.masterExecutorService.execute(this.masterRunnable);

		// Start slave
		try {
			Slave.main(new String[] {});
		} catch (Exception e) {
			fail(String.format("Exception was thrown: %s", e));
		}

	}

	@After
	public void tearDown() {
		this.masterExecutorService.shutdownNow();
	}
}
