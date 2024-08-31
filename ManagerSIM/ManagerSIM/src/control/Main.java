package control;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.UIManager;
import gui.MainFrame;



public class Main {


	/**
	 * GUI main
	 */
	public static void mainGUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			Controller control = new Controller();
			MainFrame view = new MainFrame(control);
			view.setTitle("ManagerSim");
			control.setView(view);
			view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			view.getContentPane().add(view.getPanel());
			view.setPreferredSize(new Dimension(700,500));
			view.pack();       
			view.setLocationRelativeTo(null);
			view.setVisible(true);
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}


	}



	/**
	 * Main
	 * @param args
	 */
	public static void main(String[] args) {

		mainGUI();
		
	}




}
