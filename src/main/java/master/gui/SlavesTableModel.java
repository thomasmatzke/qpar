package main.java.master.gui;

import javax.swing.table.AbstractTableModel;

import main.java.QPar;
import main.java.master.Slave;

public class SlavesTableModel extends AbstractTableModel {

	private String[] columnNames = { "Hostname", "Cores", "Current Jobs" };

	@Override
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
		switch (col) {
		case 0:
			return slave.getHostName();
		case 1:
			return slave.getCores();
		case 2:
			return join(slave.getAssignedJobIds(), ",");
		default:
			return null;
		}
	}
	public static String join(String[] strings, String separator) {
	    StringBuffer sb = new StringBuffer();
	    for (int i=0; i < strings.length; i++) {
	        if (i != 0) sb.append(separator);
	  	    sb.append(strings[i]);
	  	}
	  	return sb.toString();
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		fireTableCellUpdated(row, col);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class getColumnClass(int col) {
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
