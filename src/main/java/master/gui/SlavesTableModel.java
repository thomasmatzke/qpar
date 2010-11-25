package main.java.master.gui;

import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import main.java.master.Master;
import main.java.rmi.SlaveRemote;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class SlavesTableModel extends AbstractTableModel {

	private static Logger 	logger = Logger.getLogger(SlavesTableModel.class);
	private String[] columnNames = { "Hostname", "Cores", "Current Jobs" };

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	
	public int getColumnCount() {
		return columnNames.length;
	}

	
	public int getRowCount() {
		return Master.getSlaves().size();
	}

	
	public Object getValueAt(int row, int col) {
		SlaveRemote slave = new ArrayList<SlaveRemote>(Master.getSlaves().values()).get(row);
		try {
			switch (col) {
				case 0:
				
					return slave.getHostName();
				
				case 1:
					return slave.getCores();
				case 2:
					return StringUtils.join(slave.getCurrentJobs(), ",");
				default:
					return null;
			}
		} catch (RemoteException e) {
			logger.error(e);
			return null;
		} catch (UnknownHostException e) {
			logger.error(e);
			return null;
		}
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		fireTableCellUpdated(row, col);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<String> getColumnClass(int col) {
		switch (col) {
		case 0:
			return String.class;
		case 1:
			return String.class;
		case 2:
			return String.class;
		default:
			return null;
		}
	}

}
