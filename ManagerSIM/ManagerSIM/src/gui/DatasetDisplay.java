package gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import control.Controller;


public class DatasetDisplay{

	private JTextField  fileNameField = null;
	private File fileSel = null;
	private Controller control;

	/**
	 * Constructor for DatasetFrame.
	 * @param control
	 */
	public DatasetDisplay(Controller control) {
		this.control = control;
	}

	/**
	 * Creates the dataset panel and relies on the controller for the dataset loading.
	 * @param mainPanel main panel
	 * @param optionPan option panel where it will place the new components
	 * @param messageLog message panel to show progress
	 */
	public void loadDataset(JComponent mainPanel, JPanel optionPan, JFrame messageLog) {

		optionPan.removeAll(); //remove all
		fileNameField = null;
		fileSel = null;
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.insets = new Insets(8, 2, 8, 2);
		

		JButton browseOutput =new JButton(new AbstractAction("Browse Dataset") {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent event) { 
				selectDataset(optionPan) ;}
		});
		browseOutput.setToolTipText("Browse file with the dataset");

		optionPan.add(browseOutput,c);

		c.gridy = 1;
		c.gridwidth = 1;
		//show file
		optionPan.add(new JLabel("Browsed file"), c) ;
		fileNameField   = new JTextField(12) ;
		fileNameField.setEditable(false);
		
		fileNameField.setToolTipText("Shows the folder selected");
			
		
		fileNameField.setName("outputFolder");
		c.gridx = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		optionPan.add(fileNameField,c);

		c.gridy = 2;
		c.gridx = 0;
		c.fill = GridBagConstraints.CENTER;
		JCheckBox checkb_ablat = new JCheckBox("All");
		checkb_ablat.setSelected(false);
		checkb_ablat.setToolTipText("Load All files within folder");
		optionPan.add(checkb_ablat,c);
		

		JButton run = new JButton("Execute");
		run.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if(!messageLog.isVisible())messageLog.setVisible(true);

				if(fileSel == null) {
					System.out.println("No file was selected or name was not set, aborted action.");
					return;
				}
				if(!checkb_ablat.isSelected() && (fileSel.isDirectory() || !fileSel.getName().endsWith(".json"))) {
					System.out.println("The file needs to be a json file");
					return;
				}else if(checkb_ablat.isSelected() && fileSel.isFile()) {
					System.out.println("The file needs to be a directory to load All");
					return;
				}
				
				System.out.println("Operation started. Loading dataset into the database.");
				run.setEnabled(false); //disable buttons
				browseOutput.setEnabled(false);
				for(Component jc : mainPanel.getComponents()) {
					if(jc instanceof JButton) jc.setEnabled(false);
				}
				
				
				control.runDataset(fileSel, checkb_ablat.isSelected());
					
				

			}
		});
		c.gridx = 0; 
		c.gridy = 4;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.CENTER;
		optionPan.add(run,c);
		
		optionPan.updateUI();


	}

	/**
	 * Selects the dataset file
	 * @param optionPan option panel used for showing new dialog.
	 */
	private void selectDataset(JPanel optionPan) {
		JFileChooser chooser = null;
		if (fileSel == null)
			chooser = new JFileChooser(System.getProperty("user.dir"));
		else
			chooser = new JFileChooser(fileSel.getAbsolutePath());
		
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setVisible(true) ;
		chooser.setAcceptAllFileFilterUsed(true);

		if (chooser.showOpenDialog(optionPan) == JFileChooser.APPROVE_OPTION) {
			File    file    = chooser.getSelectedFile();
			if(file.exists()) {
				fileSel = file;
				fileNameField.setText(file.getName()) ;
			}

		}

	}
}
