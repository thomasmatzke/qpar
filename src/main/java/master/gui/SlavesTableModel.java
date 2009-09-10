package main.java.master.gui;

import javax.swing.table.AbstractTableModel;

import main.java.master.Job;
import main.java.master.Slave;

public class SlavesTableModel extends AbstractTableModel {

	private String[] columnNames = {" ", "Hostname", "Cores", "Current Job Id"};
	
	public String getColumnName(int col) {
        return columnNames[col];
    }
	
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return Slave.getSlaves().size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		Slave slave = Slave.getSlaves().get(row);
		switch(col) {
			case 1: 	return new Boolean(false);
			case 2: 	return slave.getHostAddress();
			case 3: 	return "not implemented";
			default:	return null; 
		}
	}
	
	public void setValueAt(Object value, int row, int col) {
        fireTableCellUpdated(row, col);
    }

}
