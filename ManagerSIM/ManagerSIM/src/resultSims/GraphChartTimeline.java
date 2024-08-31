package resultSims;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

import dataset.DatasetNamedData;
import java.util.ArrayList;

        


public class GraphChartTimeline extends JFrame {

	private DatasetNamedData real;
	private DatasetNamedData sim;
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * Initializes the frame
	 */
	private void initialization() { 
		XYDataset data = createDataset();
		JFreeChart chart = createChart(data);
		ChartPanel chartP = new ChartPanel(chart);
		chartP.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		chartP.setBackground(Color.WHITE);
    
		this.setLayout(new FlowLayout());
		this.setPreferredSize(new Dimension(1600, 600));
		chartP.setPreferredSize(new Dimension(1400, 500));
    XYPlot plot = (XYPlot) chart.getPlot();
    plot.setDomainGridlinesVisible(false);
    plot.setRangeGridlinesVisible(false);
    
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.add(chartP);		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	/**
	 * Creates the dataset
	 * @return dataset
	 */
	private XYDataset createDataset() {
	
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries realT = new XYSeries("same users spreading");
		double aggregate = 0;
		
		for(int i = 0; i < real.getLength(); i ++) {
			
			
			ArrayList<String> sprs = real.getSpreadersUntil(i);
			int before = sprs.size();
			ArrayList<String> sprs_sims = sim.getSpreadersUntil(i);
			//System.out.println(sprs_sims);
			//System.out.println(sprs);
			for (String sp_s : sprs_sims) {
				if (sprs.contains(sp_s)) sprs.remove(sp_s);
			}
			if (sprs_sims.size() != 0)
				aggregate = Double.valueOf(before - sprs.size()) / Double.valueOf(sprs_sims.size());
			else aggregate = 1;
			//System.out.println("tick "+i+", agg"+aggregate);
			realT.add(i, aggregate);
		}
			
			


		XYSeries simT = new XYSeries("same users debunking");
		aggregate = 0;
		
		for(int i = 0; i < real.getLength(); i ++) {
			
			
			ArrayList<String> debs = real.getDebunkersUntil(i);
			int before = debs.size();
			ArrayList<String> debs_sims = sim.getDebunkersUntil(i);
			for (String sp_s : debs_sims) {
				if (debs.contains(sp_s)) debs.remove(sp_s);
			}
			if (debs_sims.size() != 0)
				aggregate = Double.valueOf(before - debs.size()) / Double.valueOf(debs_sims.size());
			else aggregate = 1;
			//System.out.println("tick "+i+", agg"+aggregate);
			simT.add(i, aggregate);
		}

		dataset.addSeries(realT);
		dataset.addSeries(simT);
		

		return dataset;
	}

/**
 * Creates a chart with the data to display
 * @param data dataset created
 * @return chart
 */
private JFreeChart createChart(XYDataset data) {
	String time = "Minutes";
	JFreeChart graph = ChartFactory.createXYLineChart("Shared usrs", time, "Usrs", data, 
			PlotOrientation.VERTICAL,true,true, false);

	XYPlot plot = graph.getXYPlot();

	XYLineAndShapeRenderer rend = new XYLineAndShapeRenderer();
	rend.setSeriesPaint(0, Color.BLACK);
	rend.setSeriesStroke(0, new BasicStroke(2f));
	rend.setSeriesShape(0, ShapeUtilities.createDiagonalCross(1,1));
	rend.setSeriesShapesVisible(0, false);
	rend.setSeriesVisible(0, true);
	rend.setSeriesPaint(1, Color.RED);
	rend.setSeriesStroke(1, new BasicStroke(2f));
	rend.setSeriesShape(1, ShapeUtilities.createDiagonalCross(1,1));
	rend.setSeriesShapesVisible(1, false);
	rend.setSeriesVisible(1, true);
	

	plot.setRenderer(rend);
	plot.setBackgroundPaint(Color.WHITE);
	plot.setRangeGridlinesVisible(true);
	plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
	plot.setDomainGridlinesVisible(true);
	plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
	graph.getLegend().setFrame(BlockBorder.NONE);
	graph.setTitle(new TextTitle("Number of same users' msgs per time unit ("+sim.getErrorName()+") for NEWS "+real.getNewsName().substring(real.getNewsName().length()-6), JFreeChart.DEFAULT_TITLE_FONT));

	return graph;

}
	
	
	/**
	 * Loads the datasets it's going to display.
	 * @param real real dataset
	 * @param simulation simulation dataset
	 */
	public GraphChartTimeline(DatasetNamedData real, DatasetNamedData simulation) {
		this.real = real;
		this.sim = simulation;

	}
	
	
	/**
	 * Initializes the graph, for a complex graph with endorsers and deniers
	 */
	public void generateGraph() {

		initialization();
		

	}
	
	
	
	
}
