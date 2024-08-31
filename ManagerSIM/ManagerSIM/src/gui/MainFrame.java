package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import control.Controller;
import control.MainView;

public class MainFrame  extends JFrame implements MainView {

	private static final long serialVersionUID = 1L;
	private Controller control;
	private JTextArea messageLog;
	private JComponent mainPanel;
	private JPanel toolButtons, optionPan;
	private JFrame frame;
	private GraphDisplay graph;
	private LoaderDisplay loader;
	private MetricDisplay metric;
	private DatasetDisplay dataset;

	/**
	 * Constructor for MainFrame
	 * @param control controller
	 */
	public MainFrame(Controller control) {
		this.control = control;
		frame = new JFrame("Console");

		messageLog = new JTextArea(40, 10);
		messageLog.setEditable(false);
		PrintStream printStream = new PrintStream(new CustomOStr(messageLog));
		createNewConsole();
		System.setOut(printStream);
		System.setErr(printStream);

		initialize();
	}

	/**
	 * Method to initialize the main frame.
	 */
	private void initialize() {

		mainPanel = new JPanel(new BorderLayout(4,4));

		optionPan = new JPanel();
		optionPan.setLayout(new GridBagLayout ());
		optionPan.setBorder(new TitledBorder("Configuration"));
		mainPanel.add(optionPan);

		JPanel mainBorder = new JPanel(new GridBagLayout());
		mainBorder.setBorder(new TitledBorder("Options"));
		mainPanel.add(mainBorder, BorderLayout.LINE_START);

		toolButtons = new JPanel(new GridLayout(4, 1, 15, 15));
		mainBorder.add(toolButtons); 

		

		JButton add_output = new JButton("Load simulations");

		add_output.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(loader == null)loader = new LoaderDisplay(control);
				loader.addOutput(toolButtons,optionPan, frame);
			}
		});
		toolButtons.add(add_output);
		JButton load_data = new JButton("Load dataset");
		load_data.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(dataset == null)dataset = new DatasetDisplay(control);
				dataset.loadDataset(toolButtons,optionPan, frame);
			}
		});
		toolButtons.add(load_data);

		JButton load_met = new JButton("Load metrics");
		load_met.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(metric == null)metric = new MetricDisplay(control);
				metric.loadMetricsSel(toolButtons,optionPan, frame);
			}
		});
		toolButtons.add(load_met);

		JButton results = new JButton("Display results");
		results.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(graph == null)graph = new GraphDisplay(control);
				graph.showResults(toolButtons,optionPan, frame);
			}
		});
		toolButtons.add(results);
	}

	/**
	 * Returns the main panel.
	 * @return panel
	 */
	public JComponent getPanel() {
		return mainPanel;
	}

	/**
	 * Enables all buttons in its components
	 */
	public void finishOperation(long started) {
		
		long current = System.currentTimeMillis() - started;
		String val = "milliseconds";
		if (current > 1000) {
			current = current / 1000;
			val = "seconds";
			if (current > 60) {
				current = current / 60;
				val = "minutes";
			}
		}
		
		System.out.println("Operation finished in " + current +" "+ val);
		for(Component jc : toolButtons.getComponents()) { //all buttons in left panel
			if(jc instanceof JButton) jc.setEnabled(true);
		}
		//all right panel buttons
		for(Component jc : optionPan.getComponents()) {
			if(jc instanceof JButton) jc.setEnabled(true);
		}
	}

	/**
	 * Creates the console to display the message information on progress.
	 */
	private void createNewConsole() {

		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setLayout(new GridBagLayout());

		frame.setSize(new Dimension(800,350));
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets(10, 10, 10, 10);
		constraints.anchor = GridBagConstraints.WEST;
		JButton buttonClear = new JButton("Clear Log");
		buttonClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {

				messageLog.selectAll();
				messageLog.replaceSelection("");
			}
		});
		frame.add(buttonClear, constraints);
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		frame.add(new JScrollPane(messageLog),constraints);
		frame.setLocationRelativeTo(frame);
		frame.setVisible(false);

	}
}
