package gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

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
public class LoaderDisplay{

	private JTextField  nameField;
	private JTextField  nameField2;
	private File fileSel;
	private File fileSel2;
	private Controller control;

	/**
	 * Constructor for LoaderFrame.
	 * @param control
	 */
	public LoaderDisplay(Controller control) {
		this.control = control;
	}

	/**
	 * Creates the output panel and relies on the controller for the loading of the simulation.
	 * @param mainPanel main panel
	 * @param optionPan option panel where it will place the new components
	 * @param messageLog message panel to show progress
	 */
	public void addOutput(JComponent mainPanel, JPanel optionPan, JFrame messageLog) {

		optionPan.removeAll();
		nameField = null;
		nameField2 = null;
		fileSel = null;
		fileSel2 = null;
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.insets = new Insets(8, 2, 8, 2);
		

		JButton browseInput =new JButton(new AbstractAction("Browse Input") {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent event) { 
				selectFolder(optionPan, false) ;}
		});
		browseInput.setToolTipText("Selects the folder where the files are");

		optionPan.add(browseInput,c);

		c.gridx = 1;
		JButton browseInput2 =new JButton(new AbstractAction("Browse Thread Input") {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent event) { 
				selectFolder(optionPan, true) ;}
		});
		browseInput2.setToolTipText("Selects the folder where the threads are");

		optionPan.add(browseInput2,c);
		
		c.gridx = 0;
		//show file
		c.gridy = 1;
		c.gridwidth = 1;
		optionPan.add(new JLabel("Browsed directory"), c);
		
		c.gridx = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		nameField = new JTextField(12) ;
		nameField.setEditable(false);
		
		nameField.setToolTipText("Browse file with the output");
		nameField.setName("outputFolder");

		optionPan.add(nameField,c);
		
		

		c.gridx = 0;
		//show file
		c.gridy = 2;
		c.gridwidth = 1;
		optionPan.add(new JLabel("Browsed thread directory"), c);
		
		c.gridx = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		nameField2 = new JTextField(12) ;
		nameField2.setEditable(false);
		
		
		nameField2.setToolTipText("Browse file with the thread output");
		
		nameField2.setName("outputThrFolder");

		optionPan.add(nameField2,c);
		
		JCheckBox checkb = new JCheckBox("Add all output");
		checkb.setSelected(true);
		checkb.setToolTipText("Adds all the output files currently on the folder selected or only one.");
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 0;
		c.fill = GridBagConstraints.CENTER;
		optionPan.add(checkb,c);
		
		JCheckBox checkb2 = new JCheckBox("Thread");
		checkb2.setSelected(true);
		checkb2.setToolTipText("For threads, they are contained in folders and each folder is a news with their msg spread");
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 0;
		c.fill = GridBagConstraints.CENTER;
		optionPan.add(checkb2,c);

		c.gridwidth = 2;
		c.gridy = 5;
		
		
		JButton load_met = new JButton("Execute");
		load_met.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				boolean isThr = checkb2.isSelected();
				
				if(!messageLog.isVisible())messageLog.setVisible(true);
				if(fileSel == null) {

					System.out.println("No file was selected, aborted action.");
					return;
				}
				if(isThr && fileSel2 == null) {

					System.out.println("No thread folder was selected but isThread is true, aborted action.");
					return;
				}
				boolean isSel = checkb.isSelected();
				
				System.out.println("Operation started. Loading simulations into the database, please wait.");

				load_met.setEnabled(false); //disable buttons
				browseInput.setEnabled(false);
				browseInput2.setEnabled(false);
				for(Component jc : mainPanel.getComponents()) {
					if(jc instanceof JButton) jc.setEnabled(false);
				}
				
				if (isThr) control.runThreadOutput(fileSel2, isSel);
				else control.runOutput(fileSel, isSel);
			}
		});
		//c.fill = GridBagConstraints.CENTER;
		c.gridx=0;
		//c.gridy = 6;
		optionPan.add(load_met,c);
		optionPan.updateUI();
	}

	/**
	 * Selects the output folder
	 * @param optionPan option panel used for showing new dialog.
	 * @param isThread whether we want to load thread
	 */
	private void selectFolder(JPanel optionPan, boolean isThread) {
		
		JFileChooser chooser = null;
		
		if (!isThread && fileSel != null)
			chooser = new JFileChooser(fileSel.getParent());
		else if (isThread && fileSel2 != null)
			chooser = new JFileChooser(fileSel2.getParent());
		else 
			chooser = new JFileChooser(System.getProperty("user.dir"));
		
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setVisible(true) ;
		chooser.setAcceptAllFileFilterUsed(false);

		if (chooser.showOpenDialog(optionPan) == JFileChooser.APPROVE_OPTION) {
			File    file    = chooser.getSelectedFile() ;
			if(!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					System.err.println("Output folder could not be created.");
				}
			}

			if(file.exists() && file.isDirectory()){
				if (!isThread){
					fileSel = file;
					nameField.setText(file.getName()) ;
				} else {
					fileSel2 = file;
					nameField2.setText(file.getName()) ;
				}
				
			}

		}

	}
}
