package control;


import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

import databaseCon.DatabaseConnector;
import dataset.DatasetData;
import dataset.DatasetInput;
import exception.IncompatibleParameterException;
import inputFiles.Parameters;
import inputFiles.SelectInput;
import metricCalc.MetricCalculator;
import resultSims.BestResults;
import resultSims.GraphChart;
import resultSims.GraphChartTimeline;

public class Controller {

	private Parameters params;
	private MainView view;

	/**
	 * Constructor for Controller.
	 * @throws IncompatibleParameterException
	 */
	public Controller() throws IncompatibleParameterException {
		params = new Parameters();
		DatabaseConnector.getInstance().setParameters(params.getParameter("URL_conn"),params.getParameter("user"),params.getParameter("pass"));
	}

	/**
	 * Changes the parameter file
	 * @param params
	 * @throws IncompatibleParameterException
	 */
	public void changeParams(Parameters params) throws IncompatibleParameterException {
		this.params = params;
		view = null;
		DatabaseConnector.getInstance().setParameters(params.getParameter("URL_conn"),params.getParameter("user"),params.getParameter("pass"));

	}

	/**
	 * Sets the View
	 * @param view
	 */
	public void setView(MainView view) {
		this.view = view;
	}



	/**
	 * Stores a dataset in the DB. 
	 * @param data dataset info to store
	 */
	public void runDataset(File fileSel, boolean isAllSelected) {
		long current = System.currentTimeMillis();
		Thread th = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					DatabaseConnector db_ins = DatabaseConnector.getInstance();
					if (fileSel.isDirectory()) {
						for (File fil : fileSel.listFiles(new FilenameFilter() {
							
							@Override
							public boolean accept(File dir, String name) {

								if (name.endsWith(".json")) return true;
								return false;
							}
						})) {
							DatasetInput data = new DatasetInput(fil.getAbsolutePath());
							db_ins.storeDataset(data);
						}
					}else {
						DatasetInput data = new DatasetInput(fileSel.getAbsolutePath());
						db_ins.storeDataset(data);
					}
					
					

				} catch (IncompatibleParameterException e1) {

					System.err.println("Datasets could not be loaded: "+e1.getMessage());
				} finally {

					if(view!=null)view.finishOperation(current);
				}
			}

		});
		th.start();

	}


	/**
	 * Stores the output in the folder selected into the DB.
	 * @param fileSel the folder where the output files are
	 * @param runAll if it's true, it loads all the output files.
	 */
	public void runOutput(File fileSel, boolean runAll) {

		long current = System.currentTimeMillis();
		Thread th = new Thread(new Runnable() {

			@Override
			public void run() {

				int init = 0, finish = 0;
				boolean first= true;
				try {

					SelectInput input = new SelectInput(params.getParameter("directory"), params.getParameter("prefix"), params.getParameter("suffix"));
					DatabaseConnector db_ins = DatabaseConnector.getInstance();
					
					do {

						File paramFile = input.selectParameterFile(fileSel.getAbsolutePath());
						
						File sims = input.matchingSimulationFile(paramFile);

						if(!sims.exists()) throw new IncompatibleParameterException("Simulation file could not be found.");
						finish = db_ins.storeSimulations(paramFile, sims, new File(params.getParameter("newsFilesDir")));

						input.storeFiles(paramFile, sims);
						if(first) {
							init=finish;
							first = false;
						}
						
					} while(input.hasNext(fileSel.getAbsolutePath()) && runAll); //if we have more and we want to add them all

					System.out.println("Configurations loaded from "+init+" to "+finish);
				} catch (IncompatibleParameterException e1) {

					System.err.println("ERROR: "+e1.getMessage());
				} finally {
					if(view!=null) {

						view.finishOperation(current);
					}

				}
			}

		});
		th.start();
	}

	
	
	/**
	 * Stores the output in the folder selected into the DB.
	 * @param fileSel the folder where the output thread files are
	 * @param runAll if it's true, it loads all the output files.
	 */
	public void runThreadOutput(File fileSel, boolean runAll) {

		long current = System.currentTimeMillis();
		Thread th = new Thread(new Runnable() {
		
			@Override
			public void run() {

				try {
					ArrayList<Integer> configsLoaded=new ArrayList<Integer>();
					SelectInput input = new SelectInput(params.getParameter("directory"), params.getParameter("prefix"), params.getParameter("suffix"));
					DatabaseConnector db_ins = DatabaseConnector.getInstance();
					
					if (runAll) {
						File[] eachthrFile = fileSel.listFiles(c -> c.isDirectory()); //directorios, cargamos orden?
						for (File threadF: eachthrFile) {
							
							File paramFile = input.selectParameterFile(threadF.getAbsolutePath()); //only one

							File sims = input.matchingSimulationFile(paramFile);
							System.out.println("Params: "+paramFile.getAbsolutePath()+ ", sims: "+ sims.getAbsolutePath());
							
							File[] thrFiles = threadF.listFiles(c -> c.isDirectory()); //directorios, cargamos orden?

							int val = db_ins.storeThrSimulations(paramFile, sims, thrFiles, new File(params.getParameter("newsFilesDir")));
							configsLoaded.add(val);
							
						}
						System.out.println("Finished! For: "+configsLoaded.toString());
						
					} else {
						File paramFile = input.selectParameterFile(fileSel.getAbsolutePath()); //only one

						File sims = input.matchingSimulationFile(paramFile);
						System.out.println("Params: "+paramFile.getAbsolutePath()+ ", sims: "+ sims.getAbsolutePath());
						
						File[] thrFiles = fileSel.listFiles(c -> c.isDirectory()); //directorios, cargamos orden?

						System.out.println("Loading! ");
						db_ins.storeThrSimulations(paramFile, sims, thrFiles, new File(params.getParameter("newsFilesDir")));

					}
					
					
					//System.out.println("Configurations loaded from "+init+" to "+finish);
				} catch (IncompatibleParameterException e1) {

					System.err.println("ERROR: "+e1.getMessage());
				} finally {
					if(view!=null) {

						view.finishOperation(current);
					}

				}
			}

		});
		th.start();
	}
	
	
	/**
	 * Obtains the metrics for the configurations and loads them into the DB.
	 * @param newsUID news selected for the runs
	 * @param spreader weight of each spreader msg
	 * @param configTo weight of each debunker msg
	 */
	public void runMetrics(int spreadUID, int spreader, int debunker, boolean runAll) {
		long current = System.currentTimeMillis();
		Thread th = new Thread(new Runnable() {

			@Override
			public void run() {
				try {

					MetricCalculator metric = new MetricCalculator();
					
					if (!runAll) {

						metric.startCalc(spreadUID, spreader, debunker);
					}else {
						HashMap<Integer, String> map = availableDataset();
						
						for(Integer val : map.keySet()) {
							metric.startCalc(val, spreader, debunker);
						}
						
						DatabaseConnector db_ins = DatabaseConnector.getInstance();
						BestResults bRes = new BestResults();
						for(Integer spreadUID : map.keySet()) {
							
							for(int newsUID : db_ins.getNewsID(spreadUID)) {
								
								if (db_ins.isAblation(newsUID)) continue;
								//if (newsUID>58) continue; //ablation only. 

								
								boolean isSpread = false;
								bRes.loadBestResult(5, spreadUID, newsUID, "RMSE", isSpread, 1, 0); //first data, second simulation.
								
								for (int i=0; i<12; i++) {
									DatasetData abl = bRes.loadAblation(i);

									if (abl != null) {
										metric.errorAbl(bRes.getSecond(), db_ins, abl, newsUID, isSpread);
									}
										
								}
								
								isSpread = true;
								bRes.loadBestResult(5, spreadUID, newsUID, "RMSE", isSpread, 1, 0); //first data, second simulation.
								
								for (int i=0; i<12; i++) {
									DatasetData abl = bRes.loadAblation(i);
									
									if (abl != null) {
										metric.errorAbl(bRes.getSecond(), db_ins, abl, newsUID, isSpread);
									}
										

								}
							}
						}
							
					}
					

				} catch (IncompatibleParameterException e1) {

					System.err.println("ERROR: "+e1.getMessage());
				} finally {
					if(view!=null)view.finishOperation(current);
				}
			}

		});
		th.start();
	}
	
	
	
	/**
	 * Displays the graph with the best results for a model and a dataset.
	 * @param model model to display
	 * @param newsUID dataset used
	 */
	public void runGraph(int model, int spreadUID, String selectedError, boolean isSpread, boolean isAgg, int type, int config, boolean showAblation) {
		long current = System.currentTimeMillis();

		Thread th = new Thread(new Runnable() {
			DatabaseConnector db_ins = DatabaseConnector.getInstance();
			@Override
			public void run() {
				try {
					
					for (int newsUID : db_ins.getNewsID(spreadUID)) {
						//para cada news
						BestResults metric = new BestResults();
						if (isSpread && isAgg) {
							//msg
							boolean val = false;
							metric.loadBestResultSprAndAgg(model, spreadUID, newsUID, selectedError, val, type, config);
							displayGraph(metric, val, !val, showAblation);
							
							//spread
							val = true;
							metric.loadBestResultSprAndAgg(model, spreadUID, newsUID, selectedError, val, type, config);
							displayGraph(metric, val, !val, showAblation);
							
							
						} else{
							metric.loadBestResult(model, spreadUID, newsUID, selectedError, isSpread, type, config);
							displayGraph(metric, isSpread, isAgg, showAblation);
						}
					}
				} catch (IncompatibleParameterException e1) {

					System.err.println("ERROR: "+e1.getMessage());
				} finally {
					if(view!=null)view.finishOperation(current);
				}
			}

		});
		th.start();

	}

	/**
	 * Runs the user timeline graph
	 * @param model
	 * @param spreadUID
	 * @param selectedError
	 * @param config
	 */
	public void runDisplayGraphTime(int model, int spreadUID, String selectedError, int config) {
		long current = System.currentTimeMillis();
		Thread th = new Thread(new Runnable() {
			DatabaseConnector db_ins = DatabaseConnector.getInstance();
			@Override
			public void run() { 
				try {
					
					for (int newsUID : db_ins.getNewsID(spreadUID)) {
						//para cada news
						BestResults metric = new BestResults();
						metric.loadBestResultName(model, spreadUID, newsUID, selectedError, config);
						GraphChartTimeline gp = new GraphChartTimeline(metric.getFirstNamed(), metric.getSecondNamed());
						gp.generateGraph(); 
					}
					//else metric.compareCountermeasures(model,newsUID);

					

					//gp.generateGraph(model==4 ? true:false); 
				} catch (IncompatibleParameterException e1) {

					System.err.println("ERROR: "+e1.getMessage());
				} finally {
					if(view!=null)view.finishOperation(current);
				}
			}

		});
		th.start();

	}
	/**
	 * Checks the datasets loaded in the DB to allow its selection.
	 * @return map with the dataset name and its index.
	 */
	public HashMap<Integer,String> availableDataset() {

		DatabaseConnector db_ins = DatabaseConnector.getInstance();
		try{

			return db_ins.checkAvailableDatasets();

		} catch (IncompatibleParameterException e1) {

			System.err.println("ERROR: "+e1.getMessage());
		}
		return null;
	}
	
	/**
	 * Loads the possible ablation configurations and returns them
	 * @param metric
	 * @return
	 * @throws IncompatibleParameterException
	 */
	private ArrayList<DatasetData> ablationRun(BestResults metric) throws IncompatibleParameterException {
		
		ArrayList<DatasetData> dataAb = new ArrayList<DatasetData>();
		for (int i=0; i<12; i++) { //hasta la 11
			DatasetData abl = metric.loadAblation(i);
			dataAb.add(abl);

		}
		return dataAb;
	}

	/**
	 * Displays the specific graph based on the params
	 * @param metric
	 * @param isSpread
	 * @param isAgg
	 * @param showAblation
	 * @throws IncompatibleParameterException
	 */
	private void displayGraph(BestResults metric, boolean isSpread, boolean isAgg, boolean showAblation) throws IncompatibleParameterException {
		
		//metric.loadBestResult(model, spreadUID, newsUID, selectedError, isSpread, type, config);
		System.out.println(metric.getFirst().toString());
		System.out.println(metric.getSecond().toString());
		
		GraphChart gp = new GraphChart(metric.getFirst(), metric.getSecond());
		
		if (!showAblation) {
			gp.generateGraph(isAgg, isSpread); 
		} else {
			

			ArrayList<DatasetData> dataAb = ablationRun(metric);
			
			if (dataAb.size( )== 12) gp.setAblation(dataAb);
			//check nulls!
			gp.generateGraph(isAgg, isSpread); 
		}
		
	}
	

}
