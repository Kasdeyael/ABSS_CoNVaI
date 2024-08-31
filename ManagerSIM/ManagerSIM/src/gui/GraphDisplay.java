package gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import control.Controller;

public class GraphDisplay{

	private Controller control;

	/**
	 * Constructor for GraphFrame.
	 * @param control
	 */
	public GraphDisplay(Controller control) {
		this.control = control;
	}

	/**
	 * Creates the graph panel and relies on the controller for the visualization.
	 * @param mainPanel main panel
	 * @param optionPan option panel where it will place the new components
	 * @param messageLog message panel to show progress
	 */
	public void showResults(JComponent mainPanel, JPanel optionPan, JFrame messageLog) {

		optionPan.removeAll();

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.insets = new Insets(8, 2, 8, 2);
		
		optionPan.add(new JLabel("Model"), c);
		

		DefaultComboBoxModel<String> panelName = new DefaultComboBoxModel<String>();

		panelName.addElement("Model 1 (SIR)");
		panelName.addElement("Model 2 (SIR modified)");
		panelName.addElement("Model 3 (user)");
		panelName.addElement("Model 4 (news)");
		panelName.addElement("Model 5 (user + news)");
		panelName.addElement("Model 6 (user + news, confidence)");

		JComboBox<String> listCombo = new JComboBox<String>(panelName); 
		listCombo.setToolTipText("Select model for the simulation");
		listCombo.setSelectedIndex(4);
		JScrollPane listComboScrollPane = new JScrollPane(listCombo);
		c.gridwidth = 2;
		c.gridx = 1;
		optionPan.add(listComboScrollPane,c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		optionPan.add(new JLabel("Error Type"), c);
		

		c.gridx = 1;
		c.gridwidth = 2;
		DefaultComboBoxModel<String> panelName2 = new DefaultComboBoxModel<String>();
		
		panelName2.addElement("RMSE");
		panelName2.addElement("RMSESpr");
		panelName2.addElement("RMSEDeb");
		panelName2.addElement("MAE");
		panelName2.addElement("MAESpr");
		panelName2.addElement("MAEDeb");

		JComboBox<String> listCombo2 = new JComboBox<String>(panelName2); 
		listCombo2.setToolTipText("Select type of error to minimize");
		listCombo2.setSelectedIndex(0);
		JScrollPane listComboScrollPane2 = new JScrollPane(listCombo2);
		c.gridwidth = 2;
		optionPan.add(listComboScrollPane2,c);
		
		c.gridx = 0;
		c.gridy = 2;
		
		
		HashMap<Integer, String> map = control.availableDataset(); //datasets to show

		DefaultComboBoxModel<String> panel = new DefaultComboBoxModel<String>();

		if(map ==null || map.isEmpty()) {
			if(!messageLog.isVisible()) messageLog.setVisible(true); //set visible if not before
			System.err.println("Datasets could not be retrieved. Make sure the database is running and there are datasets loaded");
			return;
		}

		for(Integer val : map.keySet()) {
			panel.addElement(val+" "+map.get(val));
		}

		c.gridx = 0;
		c.gridwidth = 1;
		optionPan.add(new JLabel("Dataset"), c) ;
		c.gridwidth = 2;
		c.gridx = 1;
		
		JComboBox<String> list2 = new JComboBox<String>(panel);  
		list2.setToolTipText("Select dataset to compare the results");
		list2.setSelectedIndex(0);
		JScrollPane listScroll = new JScrollPane(list2);
		c.fill = GridBagConstraints.HORIZONTAL;
		optionPan.add(listScroll,c);
		
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 3;
		JCheckBox checkbox_state = new JCheckBox("State");
		checkbox_state.setSelected(false);
		checkbox_state.setToolTipText("Check this option if the dataset is characterized by state. Otherwise, it is by message.");
		optionPan.add(checkbox_state,c);

		
		c.gridx = 1;
		JCheckBox checkbox1_agg = new JCheckBox("Aggregate");
		checkbox1_agg.setSelected(true);
		checkbox1_agg.setToolTipText("Check this option if you want to visualize by aggregates.");
		optionPan.add(checkbox1_agg,c);

		c.gridx = 2;
		JCheckBox checkbox4_both = new JCheckBox("Both");
		checkbox4_both.setSelected(false);
		checkbox4_both.setToolTipText("Check this option if you want to minimize and visualize msgs and states.");
		optionPan.add(checkbox4_both,c);

		c.gridy = 4;
		c.gridx = 0;
		JCheckBox checkbox2_matching = new JCheckBox("User-matching");
		checkbox2_matching.setSelected(false);
		checkbox2_matching.setToolTipText("Check this option if you want to visualize the network, with the agents that get infected per time.");
		optionPan.add(checkbox2_matching,c);
		
		c.gridx = 1;
		JCheckBox checkbox3_config = new JCheckBox("Specific config");
		checkbox3_config.setSelected(false);
		checkbox3_config.setToolTipText("Check this option if you want to check a specific configuration.");
		optionPan.add(checkbox3_config,c);
		
		c.gridx = 2;
		JCheckBox checkbox4_ablation = new JCheckBox("Ablation");
		checkbox4_ablation.setSelected(false);
		checkbox4_ablation.setToolTipText("Check this option if you want to show ablation results too.");
		optionPan.add(checkbox4_ablation,c);
		
		JFormattedTextField range2 = new JFormattedTextField(Integer.valueOf(1));
		range2.setToolTipText("Config selected.");
		JLabel lab2 = new JLabel("Configuration ID");
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 5;
		
		optionPan.add(lab2,c);
		
		c.gridx = 1;
		c.gridwidth = 2;
		range2.setColumns(5);
		range2.setVisible(false);
		lab2.setVisible(false);
		optionPan.add(range2,c);
	
		checkbox4_both.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (checkbox_state.isSelected() || checkbox2_matching.isSelected()) {
					
					System.out.println("Selecting both aggregate and states.");
					checkbox_state.setSelected(false);
					checkbox2_matching.setSelected(false);
				}
			}
		});
		
		checkbox_state.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (checkbox1_agg.isSelected() || checkbox2_matching.isSelected() || checkbox4_both.isSelected()) {
					
					System.out.println("State is aggregated. 'Aggregate' checkbox has been modified.");
					checkbox1_agg.setSelected(false);
					checkbox2_matching.setSelected(false);
					checkbox4_both.setSelected(false);
				}
			}
		});
		
		checkbox1_agg.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (checkbox_state.isSelected() || checkbox2_matching.isSelected() || checkbox4_both.isSelected()) {
					
					System.out.println("Aggregate is by message, excluding state. 'State' checkbox has been modified.");
					checkbox_state.setSelected(false);
					checkbox2_matching.setSelected(false);
					checkbox4_both.setSelected(false);
				}
			}
		});
		

		
		checkbox2_matching.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (checkbox_state.isSelected() || checkbox1_agg.isSelected() || checkbox4_both.isSelected() || checkbox4_ablation.isSelected()) {
					
					System.out.println("User-matching is neither aggregated nor state. 'State' and 'Aggregate' checkboxes have been modified.");
					checkbox_state.setSelected(false);
					checkbox1_agg.setSelected(false);
					checkbox4_both.setSelected(false);
					checkbox4_ablation.setSelected(false);
				}
			}
		});
		
		checkbox3_config.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (checkbox3_config.isSelected()) {

					range2.setVisible(true);
					lab2.setVisible(true);
				}
				else {
					range2.setVisible(false);

					lab2.setVisible(false);
				}
					
			}
		});
		
		c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = 1;
		JLabel lab = new JLabel("Type");
		optionPan.add(lab, c);
		

		DefaultComboBoxModel<String> panelName3 = new DefaultComboBoxModel<String>();

		panelName3.addElement("Timeline"); // useProbReply=0  & k > 0
		panelName3.addElement("UseProbReply"); //useProbReply=1
		panelName3.addElement("ProbNw"); // useProbReply=0  & k = 0

		JComboBox<String> listCombo3 = new JComboBox<String>(panelName3); 
		listCombo3.setToolTipText("Select model for the simulation");
		listCombo3.setSelectedIndex(0);
		JScrollPane listComboScrollPane3 = new JScrollPane(listCombo3);
		c.gridwidth = 2;
		c.gridx = 1; 
		optionPan.add(listComboScrollPane3,c);
		
		
		listCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (1+ listCombo.getSelectedIndex() == 1) {
					checkbox2_matching.setVisible(false);
				}else {
					checkbox2_matching.setVisible(true);
				}
				if ( 1+ listCombo.getSelectedIndex() > 3) {
					listComboScrollPane3.setVisible(true);
					lab.setVisible(true);
				} else {
					listComboScrollPane3.setVisible(false);
					lab.setVisible(false);
				}
			}
		});
		
		
		JButton load_met = new JButton("Show graph");
		load_met.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				int config = 0;
				if (checkbox3_config.isSelected()) config = (int)range2.getValue();
				
				if(!messageLog.isVisible())messageLog.setVisible(true);
				String dataset = panel.getElementAt(list2.getSelectedIndex());
				String init = dataset.split(" ")[0];
				int datSel = Integer.parseInt(init);
				if(datSel < 1) {
					System.out.println("Dataset selected not valid. Aborting action");
					return;
				}
				

				int model = 1+ listCombo.getSelectedIndex(); //based on index, model 1 to 4
				System.out.println("Operation started. Showing model "+model+".");

				load_met.setEnabled(false); //disable buttons
				for(Component jc : mainPanel.getComponents()) {
					if(jc instanceof JButton) jc.setEnabled(false);
				}
				
				if (!checkbox2_matching.isSelected()) {
					int sel_type = 1 + listCombo3.getSelectedIndex();
					boolean state= !checkbox4_both.isSelected()? checkbox_state.isSelected():checkbox4_both.isSelected();
					boolean agg= !checkbox4_both.isSelected()? checkbox1_agg.isSelected():checkbox4_both.isSelected();

					control.runGraph(model, datSel, (String)panelName2.getSelectedItem(), state, agg, sel_type, config, checkbox4_ablation.isSelected());
					
				} else {
					control.runDisplayGraphTime(model, datSel, (String)panelName2.getSelectedItem(), config);
				}

			}
		});
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 0;
		c.gridx = 0;
		c.gridy = 7;
		optionPan.add(load_met,c);

		optionPan.updateUI();
	}
}
