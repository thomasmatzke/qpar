package main.java.slave.solver;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;

import main.java.QPar;
import main.java.logic.TransmissionQbf;
import main.java.master.Mailer;
import main.java.rmi.Result;
import main.java.slave.Slave;

public class SimpleSolver extends Solver {

	boolean run = true;
	Result r = new Result();
	
	public SimpleSolver(Slave slave) {
		super(slave);
	}

	@Override
	public void kill() {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		formula.assignTruthValues();
		formula.reduceFast();
				
		try {
			if (formula.rootIsTruthNode()) {
				if (formula.rootGetTruthValue()) {
					r.type = Result.Type.TRUE;
					this.slave.master.returnResult(r);
					return;
				}
				r.type = Result.Type.FALSE;
				this.slave.master.returnResult(r);
				return;
			}
		
			// Prepare formula for further use
			ArrayList<Integer> assignedVars = new ArrayList<Integer>();
			assignedVars.addAll(formula.getFalseVars());
			assignedVars.addAll(formula.getTrueVars());
			formula.getAVars().removeAll(assignedVars);
			formula.getEVars().removeAll(assignedVars);
			formula.getVars().removeAll(assignedVars);
			
			TruthtableIterator it = new TruthtableIterator(formula.getVars().size());
			while(run) {
				boolean[] currentInterpretation = null;
				if(it.hasNext()) {
					currentInterpretation = it.next();
				} else {
					r.type = Result.Type.FALSE;
					this.slave.master.returnResult(r);
					return;
				}
				
				logger.info("Trying interpretation: " + Arrays.toString(currentInterpretation));
				
				TransmissionQbf clonedFormula;
				try {
					clonedFormula = formula.deepClone();
				} catch (Exception e) {
					logger.error(e);
					r.exception = e;
					r.type = Result.Type.ERROR;
					this.slave.master.returnResult(r);
					return;
				}
				
				for(int i = 0; i < clonedFormula.getVars().size(); i++) {
					if(currentInterpretation[i]) {
						clonedFormula.getTrueVars().add(clonedFormula.getVars().get(i));
					} else {
						clonedFormula.getFalseVars().add(clonedFormula.getVars().get(i));
					}
				}							
				
				clonedFormula.assignTruthValues();
				clonedFormula.reduceFast();
				
				// If formula collapsed and results in true we are done, else continue with search
				if (formula.rootIsTruthNode() && formula.rootGetTruthValue()) {
						r.type = Result.Type.TRUE;
						this.slave.master.returnResult(r);
						return;
				}
				
			}
		} catch (RemoteException e) {
			// Comm fail...what to do now
			logger.error(e);
			if(QPar.isMailInfoComplete() && QPar.exceptionNotifierAddress != null)
				Mailer.send_mail(QPar.exceptionNotifierAddress, QPar.mailServer, QPar.mailUser, QPar.mailPass, "Exception Notification (QProSolver.main())", e.toString());
			slave.reconnect();
		}
		
	}
	
	class TruthtableIterator implements Iterator {

		int variables = 0;
		int counter = 0;
		public BitSet bitset = null;
		boolean bitspaceExhausted = false;		
		
		/**
		 * 
		 * @param variables Number of variables
		 */
		public TruthtableIterator(int variables) {
			assert(variables > 0);
			this.variables = variables;
			this.bitset = new BitSet(variables);
			//this.bitset.set(variables-1, true);
		}
		
		@Override
		public boolean hasNext() {
			if(bitspaceExhausted)
				return false;
			for(int i=0; i < variables; i++) {
				if(bitset.get(i) == false) 
					return true;
			}
			bitspaceExhausted = true;
			return true;
		}

		@Override
		public boolean[] next() {
			boolean[] tmp = setToArray(bitset);
			addOneToBitset();
			return tmp;
		}

		private void addOneToBitset() {
			boolean overflow = false;
			for(int i=0; i < variables; i++) {
				if(bitset.get(i)) {
					bitset.set(i, false);
					overflow = true;
				} else {
					if(overflow) {
						bitset.set(i, true);
						overflow = false;
					}
					bitset.set(i, true);
					return;
				}
			}
		}

		@Override
		public void remove() {}
		
		boolean[] setToArray(BitSet bitset) {
			boolean[] arr = new boolean[variables];
			for(int i=0; i < bitset.length(); i++) {
				arr[i] = bitset.get(i);
			}			
			return arr;
		}
		
	}
}
