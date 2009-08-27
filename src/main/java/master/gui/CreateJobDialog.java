package main.java.master.gui;

import javax.swing.JPanel;
import java.awt.Frame;
import java.awt.BorderLayout;
import javax.swing.JDialog;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import javax.swing.JFileChooser;
import javax.swing.JButton;

public class CreateJobDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JLabel inputFileLabel = null;
	private JPanel dialogContentPane = null;
	private JLabel outputFileLabel = null;
	private JLabel solverLabel = null;
	private JLabel heuristicLabel = null;
	private JTextField formulaTextField = null;
	private JFileChooser jFileChooser = null;
	private JButton inputFileButton = null;
	/**
	 * @param owner
	 */
	public CreateJobDialog(Frame owner) {
		super(owner);
		initialize();
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
		this.setSize(357, 316);
		this.setContentPane(getDialogContentPane());
	}

	/**
	 * This method initializes dialogContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getDialogContentPane() {
		if (dialogContentPane == null) {
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 2;
			gridBagConstraints21.gridy = 0;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.gridy = 1;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints4.gridy = 0;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.gridx = 1;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.gridy = 3;
			heuristicLabel = new JLabel();
			heuristicLabel.setText("Heuristic");
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 2;
			solverLabel = new JLabel();
			solverLabel.setText("Solver");
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 1;
			outputFileLabel = new JLabel();
			outputFileLabel.setText("Outputfile");
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			dialogContentPane = new JPanel();
			dialogContentPane.setLayout(new GridBagLayout());
			dialogContentPane.add(inputFileLabel, gridBagConstraints);
			dialogContentPane.add(outputFileLabel, gridBagConstraints1);
			dialogContentPane.add(solverLabel, gridBagConstraints2);
			dialogContentPane.add(heuristicLabel, gridBagConstraints3);
			dialogContentPane.add(getFormulaTextField(), gridBagConstraints4);
			dialogContentPane.add(getInputFileButton(), gridBagConstraints21);
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
	 * This method initializes jFileChooser	
	 * 	
	 * @return javax.swing.JFileChooser	
	 */
	private JFileChooser getJFileChooser() {
		if (jFileChooser == null) {
			jFileChooser = new JFileChooser();
		}
		return jFileChooser;
	}

	/**
	 * This method initializes inputFileButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getInputFileButton() {
		if (inputFileButton == null) {
			inputFileButton = new JButton();
			inputFileButton.setText("Open");
		}
		return inputFileButton;
	}

}  //  @jve:decl-index=0:visual-constraint="143,11"
