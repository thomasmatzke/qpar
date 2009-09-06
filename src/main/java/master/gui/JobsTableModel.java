package main.java.master.gui;

import javax.swing.table.AbstractTableModel;

import main.java.master.Job;
import main.java.master.MasterDaemon;

public class JobsTableModel extends AbstractTableModel {

	private String[] columnNames = {"Job Id", "Started at", "Finished At", "Status"};
	
	public String getColumnName(int col) {
        return columnNames[col];
    }
	
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return Job.getJobs().size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		Job job = Job.getJobs().get(row);
		switch(col) {
			case 1: 	return job.getId();
			case 2: 	return job.getStartedAt();
			case 3: 	return job.getStoppedAt();
			default:	return null; 
		}
	}
	
	public void setValueAt(Object value, int row, int col) {
        fireTableCellUpdated(row, col);
    }

}
