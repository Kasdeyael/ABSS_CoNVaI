package resultSims;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Paint;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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

import dataset.DatasetData;

public class GraphChart extends JFrame {

	private int counter; 
	private int counterAbl;
	private DatasetData real;
	private DatasetData sim; 
	ArrayList<DatasetData> dataAb;
	
	private static final long serialVersionUID = 1L;

	/**
	 * Initializes the frame
	 * @param counterMeasure
	 */
	private void initialization(boolean isAgg, boolean isSpread) {
		XYDataset data = createDataset(isAgg, isSpread);
		JFreeChart chart;

		chart = createChart(data, isSpread);
		ChartPanel chartP = new ChartPanel(chart);
		chartP.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		chartP.setBackground(Color.WHITE);
		JButton toggleButton = new JButton("Toggle Type Visibility");
		JButton toggleAblButton = new JButton("Toggle Ablation Visibility");
		
		if (data.getSeriesCount() > 4) {
			toggleAblButton.setVisible(true);
			counter = 0;
			counterAbl = 4;
		} else {
			toggleAblButton.setVisible(false);
			counter = 2;
		}
			
		//button to alternate with ablation
		toggleAblButton.addActionListener(e -> { 
			for (int i=0; i<data.getSeriesCount(); i++) {
				if (counterAbl <= (data.getSeriesCount()/2) && (i == 0 || i == 1)) {
					chart.getXYPlot().getRenderer().setSeriesVisible(i, true);
				} else if (counterAbl > (data.getSeriesCount()/2) && (i == 2 || i == 3)) {
					chart.getXYPlot().getRenderer().setSeriesVisible(i, true);
					
				} else if (i == counterAbl) { //low
					chart.getXYPlot().getRenderer().setSeriesVisible(i, true);
					if (counterAbl <= (data.getSeriesCount()/2))chart.getXYPlot().getRenderer().setSeriesPaint(i, Color.RED);
					else chart.getXYPlot().getRenderer().setSeriesPaint(i, Color.BLACK);
				} else if (i > counterAbl && i < (counterAbl+2)) { //high
					chart.getXYPlot().getRenderer().setSeriesVisible(i, true);
					if (counterAbl <= (data.getSeriesCount()/2))chart.getXYPlot().getRenderer().setSeriesPaint(i, Color.CYAN);
					else chart.getXYPlot().getRenderer().setSeriesPaint(i, Color.ORANGE);
				}else {
					chart.getXYPlot().getRenderer().setSeriesVisible(i, false);
				}
				
			}
			if (counterAbl + 2 > (data.getSeriesCount()-1)) {
				counterAbl=4;
			}else counterAbl+= 2;
		});
		
		toggleButton.addActionListener(e -> {
		    // Logic to toggle visibility of specific lines based on user input
			if (data.getSeriesCount() == 6) {
				for (int i=0; i<data.getSeriesCount(); i++) {
					
					if (i == counter || i == (counter+1)) {
						chart.getXYPlot().getRenderer().setSeriesVisible(i, true);
					}else {
						chart.getXYPlot().getRenderer().setSeriesVisible(i, false);
					}
				}
				if (counter == 4) {
					counter=0;
				}else counter+= 2;
			} else { //ablation
				int div = data.getSeriesCount() / 3;
				for (int i=0; i<data.getSeriesCount(); i++) {
					
					if ( i < (counter+div) && i >= counter) {
						chart.getXYPlot().getRenderer().setSeriesVisible(i, true);
					}else {
						chart.getXYPlot().getRenderer().setSeriesVisible(i, false);
					}
				}
				if (counter+ div == data.getSeriesCount()) {
					counter=0;
				}else counter+= div;
			}
				
		    
		});
		this.setLayout(new FlowLayout());
		this.setPreferredSize(new Dimension(800, 600));
		chartP.setPreferredSize(new Dimension(800, 500));
    XYPlot plot = (XYPlot) chart.getPlot();
    plot.setDomainGridlinesVisible(false);
    plot.setRangeGridlinesVisible(false);
    
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.add(chartP);		
		this.add(toggleButton);
		this.add(toggleAblButton);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}


	/**
	 * Loads the datasets it's going to display.
	 * @param real real dataset
	 * @param simulation simulation dataset
	 */
	public GraphChart(DatasetData real, DatasetData simulation) {
		this.real = real;
		this.sim = simulation;

		dataAb = new ArrayList<DatasetData>(); 
	}
	


	/**
	 * Loads the dataset it's going to display (only one dataset)
	 * @param real real dataset
	 */
	public GraphChart(DatasetData real) {
		this.real = real;
		dataAb = new ArrayList<DatasetData>(); 
	}
	
	/**
	 * Creates the series based on the values
	 * @param selected selected dataset
	 * @param name name of data
	 * @param isAgg whether to aggregate
	 * @param isSpread state based
	 * @param isDenier debunkers
	 * @param isSpreader spreaders
	 * @return
	 */
	private XYSeries getSeries(DatasetData selected, String name, boolean isAgg, boolean isSpread, boolean isDenier, boolean isSpreader) {
		XYSeries selT = new XYSeries(name);
		int aggregate = 0;
		for(int i = 0; i < real.getSpreaders().size(); i ++) {
			if (isAgg && !isSpread) { //if aggregate, not spread (msgs, need to add them)
				aggregate += (isSpreader ? selected.getSpreaders().get(i):0) + (isDenier?selected.getDeniers().get(i):0);
				
			} else // states already aggregated, or we don't want to aggregate
			aggregate = (isSpreader ? selected.getSpreaders().get(i):0) + (isDenier?selected.getDeniers().get(i):0);
			selT.add(i, aggregate);
		}
		return selT;
	}
	/**
	 * Creates the dataset it's going to be used for the display.
	 * @param isAgg aggregated
	 * @param isSpread state-based
	 * @return dataset with the points to be represented
	 */
	private XYDataset createDataset(boolean isAgg, boolean isSpread) {
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		
		XYSeries realE = getSeries(real, "real spreaders", isAgg, isSpread, false, true);
		XYSeries simE = getSeries(sim, "simulation spreaders", isAgg, isSpread, false, true);

		dataset.addSeries(realE);
		dataset.addSeries(simE);
		
		XYSeries realDe = getSeries(real, "real debunkers", isAgg, isSpread, true, false);
		XYSeries simDe = getSeries(sim, "simulation debunkers", isAgg, isSpread, true, false);

		dataset.addSeries(realDe);
		dataset.addSeries(simDe);
		
		
		if (dataAb.size() > 0) {
			for (DatasetData data : dataAb) { //spreaders!
				if (data!= null) dataset.addSeries(getSeries(data, data.getName() + " spreaders", isAgg, isSpread, false, true));
				
			}
			
			for (DatasetData data : dataAb) { //debunkers!
				if (data!= null) dataset.addSeries(getSeries(data, data.getName() + " debunkers", isAgg, isSpread, true, false));
				
			}
			
		}
		
		return dataset;
	}
	


	/**
	 * Creates a chart with the data to display.
	 * @param data dataset created
	 * @param isSpread state-based
	 * @return chart
	 */
	private JFreeChart createChart(XYDataset data, boolean isSpread) {
		String time = "Minutes";
		String type;
		if (isSpread) type = "States";
		else type = "Msgs";
		JFreeChart graph = ChartFactory.createXYLineChart("Spread of news", time, type+" of users", data, 
				PlotOrientation.VERTICAL,true,true, false);

		XYPlot plot = graph.getXYPlot();

		XYLineAndShapeRenderer rend = new XYLineAndShapeRenderer();
		
		Paint[] paints = {Color.MAGENTA, Color.BLUE, Color.GREEN, Color.GRAY, Color.CYAN, Color.ORANGE, Color.BLACK, Color.RED, Color.PINK};

		for (int i =0; i < data.getSeriesCount(); i++) {
				rend.setSeriesPaint(i, paints[i%paints.length]);
				rend.setSeriesStroke(i, new BasicStroke(2f));
				rend.setSeriesShape(i, ShapeUtilities.createDiagonalCross(1,1));
				rend.setSeriesShapesVisible(i, false);
				if (data.getSeriesCount() > 4 && (i==3 || i == 2 || i > 7)) rend.setSeriesVisible(i, false); //for visualization
				else rend.setSeriesVisible(i, true);
		}
			

		plot.setRenderer(rend);
		plot.setBackgroundPaint(Color.WHITE);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
		graph.getLegend().setFrame(BlockBorder.NONE);
		graph.setTitle(new TextTitle("Number of users' "+type+" per time unit ("+sim.getErrorName()+") for NEWS "+real.getNewsName().substring(real.getNewsName().length()-6), JFreeChart.DEFAULT_TITLE_FONT));

		return graph;

	}

	/**
	 * Initializes the graph, for a complex graph with endorsers and deniers.
	 * @param isAgg aggregate
	 * @param isSpread state-based
	 */
	public void generateGraph(boolean isAgg, boolean isSpread) {

		initialization(isAgg, isSpread);
		

	}

	/**
	 * Sets ablation runs
	 * @param dataAb
	 */
	public void setAblation(ArrayList<DatasetData> dataAb) {
		
		this.dataAb = dataAb; 
		
	
	}


}
