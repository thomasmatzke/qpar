package qpar.master.gui;

import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;


import org.apache.log4j.Logger;

import qpar.common.rmi.SlaveRemote;
import qpar.master.SlaveRegistry;

public class SlavesTableModel extends AbstractTableModel {

	private static Logger 	logger = Logger.getLogger(SlavesTableModel.class);
	private String[] columnNames = { "Hostname", "Cores"};

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	
	public int getColumnCount() {
		return columnNames.length;
	}

	
	public int getRowCount() {
		return SlaveRegistry.instance().getSlaves().size();
	}

	
	public Object getValueAt(int row, int col) {
		SlaveRemote slave = new ArrayList<SlaveRemote>(SlaveRegistry.instance().getSlaves().values()).get(row);
		try {
			switch (col) {
				case 0:				
					return slave.getHostName();
				case 1:
					return slave.getCores();
				default:
					return null;
			}
		} catch (RemoteException e) {
			logger.error("RMI fail", e);
			return null;
		} catch (UnknownHostException e) {
			logger.error("Host not found", e);
			return null;
		}
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		fireTableCellUpdated(row, col);
	}

	@Override
	public Class<String> getColumnClass(int col) {
		switch (col) {
		case 0:
			return String.class;
		case 1:
			return String.class;
		default:
			return null;
		}
	}

}
