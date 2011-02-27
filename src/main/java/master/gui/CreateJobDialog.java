package main.java.master.gui;

import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import main.java.master.Job;

public class CreateJobDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon getIcon(String path) {
		String img_path = Thread.currentThread().getContextClassLoader()
				.getResource(path).getFile();
		if (img_path != null) {
			return new ImageIcon(img_path);
		}
		System.err.println("Couldn't find file: " + path);
		return null;
	}

	private JPanel actionPanel = null;
	private JButton cancelButton = null;
	private JPanel dialogContentPane = null;
	private JTextField formulaTextField = null;
	private JComboBox heuristicComboBox = null;
	private JLabel heuristicLabel = null;
	private String[] heuristics;
	private JButton inputFileButton = null;
	private JLabel inputFileLabel = null;
	private JButton outputFileButton = null;
	private JLabel outputFileLabel = null;
	private JTextField outputFileTextField = null;
	private JButton saveButton = null;
	private JComboBox solverComboBox = null;
	private JLabel solverLabel = null;

	private String[] solvers;

	/**
	 * @param owner
	 */
	public CreateJobDialog(Frame owner, String[] solvers, String[] heuristics) {
		super(owner);
		this.heuristics = heuristics;
		this.solvers = solvers;
		initialize();
	}

	/**
	 * This method initializes actionPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getActionPanel() {
		if (actionPanel == null) {
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = -1;
			gridBagConstraints7.gridy = -1;
			actionPanel = new JPanel();
			actionPanel.setLayout(new GridBagLayout());
			actionPanel.add(getSaveButton(), gridBagConstraints7);
			actionPanel.add(getCancelButton(), new GridBagConstraints());
		}
		return actionPanel;
	}

	/**
	 * This method initializes cancelButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText("Cancel");
			cancelButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dispose();
				}
			});
		}
		return cancelButton;
	}

	/**
	 * This method initializes dialogContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getDialogContentPane() {
		if (dialogContentPane == null) {
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 1;
			gridBagConstraints6.gridy = 6;
			GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
			gridBagConstraints41.fill = GridBagConstraints.BOTH;
			gridBagConstraints41.gridy = 3;
			gridBagConstraints41.weightx = 1.0;
			gridBagConstraints41.gridwidth = 2;
			gridBagConstraints41.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints41.gridx = 1;
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			gridBagConstraints31.fill = GridBagConstraints.BOTH;
			gridBagConstraints31.gridy = 2;
			gridBagConstraints31.weightx = 1.0;
			gridBagConstraints31.gridwidth = 2;
			gridBagConstraints31.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints31.gridx = 1;
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			gridBagConstraints22.gridx = 2;
			gridBagConstraints22.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints22.gridy = 1;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.fill = GridBagConstraints.BOTH;
			gridBagConstraints11.gridy = 1;
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints11.gridx = 1;
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 2;
			gridBagConstraints21.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints21.gridy = 0;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.gridy = 1;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.fill = GridBagConstraints.BOTH;
			gridBagConstraints4.gridy = 0;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.ipadx = 0;
			gridBagConstraints4.ipady = 0;
			gridBagConstraints4.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints4.gridx = 1;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints3.gridy = 3;
			heuristicLabel = new JLabel();
			heuristicLabel.setText("Heuristic");
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints2.gridy = 2;
			solverLabel = new JLabel();
			solverLabel.setText("Solver");
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints1.gridy = 1;
			outputFileLabel = new JLabel();
			outputFileLabel.setText("Outputfile");
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints.gridy = 0;
			dialogContentPane = new JPanel();
			dialogContentPane.setLayout(new GridBagLayout());
			dialogContentPane.add(inputFileLabel, gridBagConstraints);
			dialogContentPane.add(outputFileLabel, gridBagConstraints1);
			dialogContentPane.add(solverLabel, gridBagConstraints2);
			dialogContentPane.add(heuristicLabel, gridBagConstraints3);
			dialogContentPane.add(getFormulaTextField(), gridBagConstraints4);
			dialogContentPane.add(getInputFileButton(), gridBagConstraints21);
			dialogContentPane.add(getOutputFileTextField(),
					gridBagConstraints11);
			dialogContentPane.add(getOutputFileButton(), gridBagConstraints22);
			dialogContentPane.add(getSolverComboBox(), gridBagConstraints31);
			dialogContentPane.add(getHeuristicComboBox(), gridBagConstraints41);
			dialogContentPane.add(getActionPanel(), gridBagConstraints6);
		}
		return dialogContentPane;
	}

	/**
	 * This method initializes formulaTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getFormulaTextField() {
		if (formulaTextField == null) {
			formulaTextField = new JTextField();
			formulaTextField.setColumns(20);
		}
		return formulaTextField;
	}

	/**
	 * This method initializes heuristicComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getHeuristicComboBox() {
		if (heuristicComboBox == null) {
			heuristicComboBox = new JComboBox(heuristics);
		}
		return heuristicComboBox;
	}

	/**
	 * This method initializes inputFileButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getInputFileButton() {
		if (inputFileButton == null) {
			inputFileButton = new JButton(getIcon("open.gif"));
			// inputFileButton.setText("Open");
			inputFileButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							JFileChooser fc = new JFileChooser();
							int returnVal = fc
									.showOpenDialog(getDialogContentPane());
							if (returnVal == JFileChooser.APPROVE_OPTION) {
								File file = fc.getSelectedFile();
								getFormulaTextField().setText(
										file.getAbsolutePath());
							}
						}
					});
		}
		return inputFileButton;
	}

	/**
	 * This method initializes outputFileButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getOutputFileButton() {
		if (outputFileButton == null) {
			outputFileButton = new JButton(getIcon("open.gif"));
			// outputFileButton.setText("Open");
			outputFileButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							JFileChooser fc = new JFileChooser();
							int returnVal = fc
									.showSaveDialog(getDialogContentPane());
							if (returnVal == JFileChooser.APPROVE_OPTION) {
								File file = fc.getSelectedFile();
								getOutputFileTextField().setText(
										file.getAbsolutePath());
							}
						}
					});
		}
		return outputFileButton;
	}

	/**
	 * This method initializes jTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getOutputFileTextField() {
		if (outputFileTextField == null) {
			outputFileTextField = new JTextField();
		}
		return outputFileTextField;
	}

	/**
	 * This method initializes saveButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getSaveButton() {
		if (saveButton == null) {
			saveButton = new JButton();
			saveButton.setText("Save");
			saveButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (formulaTextField.getText().equals("")
							|| outputFileTextField.getText().equals("")) {
						return;
					}
					// TODO: Let set timeout & maxcores via GUI
					if(new File(formulaTextField.getText()).exists()) {
						new Job(getFormulaTextField().getText(),
								getOutputFileTextField().getText(),
								(String) getSolverComboBox().getSelectedItem(),
								(String) getHeuristicComboBox().getSelectedItem(),
								0,
								0);
						dispose();
					} else {
						formulaTextField.setBackground(Color.RED);
					}
				}
			});
		}
		return saveButton;
	}

	/**
	 * This method initializes solverComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getSolverComboBox() {
		if (solverComboBox == null) {
			solverComboBox = new JComboBox(solvers);
		}
		return solverComboBox;
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		inputFileLabel = new JLabel();
		inputFileLabel.setText("Formula File");
		inputFileLabel.setName("inputFileLabel");
		this.setSize(380, 219);
		this.setContentPane(getDialogContentPane());
	}

} // @jve:decl-index=0:visual-constraint="157,2"
