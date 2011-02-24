package main.java.master.gui;

import javax.swing.table.AbstractTableModel;

import main.java.master.Job;

public class JobsTableModel extends AbstractTableModel {

	private String[] columnNames = { "Job Id", "Started at", "Finished At",
			"Status" };

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	
	public int getColumnCount() {
		return columnNames.length;
	}

	
	public int getRowCount() {
		return Job.getJobs().size();
	}

	
	public Object getValueAt(int row, int col) {
		Job job = Job.getJobs().get(row);
		switch (col) {
		case 0:
			return job.getId();
		case 1:
			return job.getStartedAt();
		case 2:
			return job.getSolvedAt();
		case 3:
			switch(job.getStatus()) {
				case READY:
					return "Ready";
				case RUNNING:
					return "Running";
				case COMPLETE:
					return "Complete";
				case ERROR:
					return "Error";
			}
			
			return "Undefined";
		default:
			return null;
		}
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		fireTableCellUpdated(row, col);
	}

}
