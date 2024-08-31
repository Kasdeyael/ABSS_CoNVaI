package metricCalc;


import java.util.ArrayList;

import databaseCon.DatabaseConnector;
import dataset.DatasetData;
import exception.IncompatibleParameterException;


public class MetricCalculator {


	/**
	 * Constructor for MetricCalculator
	 */
	public MetricCalculator() {

	}
	
	/**
	 * Obtains the RMSE linked to all the configurations contained between configTo and configFrom, using the newsUID.
	 * @param spreadUID dataset to use
	 * @param spreader weight of each spreader msg
	 * @param debunker weight of each debunker msg
	 * @throws IncompatibleParameterException
	 */
	public void startCalc(int spreadUID, int spreader, int debunker) throws IncompatibleParameterException {

		DatabaseConnector db_ins = DatabaseConnector.getInstance();
		ArrayList<Integer> newsUIDs = db_ins.getNewsID(spreadUID);
		ArrayList<Integer> configs = db_ins.getAvailableConfigs(spreadUID);
		for (int newsUID : newsUIDs) {
			DatasetData data = db_ins.loadDataset(newsUID, true); //dataset for spread(state)
			DatasetData data_msg = db_ins.loadDataset(newsUID, false); //for msg
			data_msg.trimForMetrics(); //trim the data
			data.trimForMetrics(); //trim the data
			
			errorActConfig(data, db_ins, configs, newsUID, true, spreader, debunker);
			errorActConfig(data_msg, db_ins, configs, newsUID, false, spreader, debunker);
		}
		
	}
	
	/**
	 * Check if errors are loaded for the configs and loads them if they're not
	 * @param data real dataset
	 * @param db_ins db con
	 * @param configs configurations
	 * @param newsUID news id
	 * @param isSpread whether state or msg-based
	 * @param spreader spreader weight
	 * @param debunker debunker weight
	 * @throws IncompatibleParameterException
	 */
	public void errorActConfig(DatasetData data, DatabaseConnector db_ins, ArrayList<Integer> configs, int newsUID, boolean isSpread, int spreader, int debunker) throws IncompatibleParameterException {
		
		//System.out.println("deniers " + data.getDeniers());
		//System.out.println("spreaders " + data.getSpreaders());
		//for each config
		CurrentExec current = new CurrentExec();
		ErrorCalc error = new ErrorCalc();
		for(Integer conf : configs) {
			System.out.println("CHECKING CONF "+conf+", news "+data.getNewsUID()+", for the spread? : "+isSpread);
			int response = db_ins.checkConfiguration(conf,newsUID,current, isSpread);

			switch(response) {

			case 0:
				System.out.println("Results already loaded for "+conf);
				break;
			case 1:

				System.out.println("Processing configuration = "+conf+", with "+current.getMaxExec()+" executions...");

				for(int exec=1; exec <= current.getMaxExec(); exec++) { //for all executions
					
					
					
					DatasetData sim = db_ins.loadSimulation(conf, data, exec, newsUID);
					
					error.setNewsUID(newsUID);
					error.setConfigurationUID(conf);
					error.setExec(exec);

					double rmseAggSpr = obtainGlobalMsgRMSE(sim.getSpreaders(), data.getSpreaders(), isSpread);
					double rmseAggDeb = obtainGlobalMsgRMSE(sim.getDeniers(), data.getDeniers(), isSpread);
					
					double maeAggSpr = obtainGlobalMsgMae(sim.getSpreaders(), data.getSpreaders(), isSpread);
					double maeAggDeb = obtainGlobalMsgMae(sim.getDeniers(), data.getDeniers(), isSpread);
					

					double rmseNonWei = ((spreader * rmseAggSpr) + (debunker * rmseAggDeb)) / ((spreader * data.getTotalNumberSprs()) + (debunker * data.getTotalNumberDebs()) - (1 * spreader));
					double maeNonWei =((spreader * maeAggSpr) + (debunker * maeAggDeb)) / ((spreader * data.getTotalNumberSprs()) + (debunker * data.getTotalNumberDebs()) - (1*spreader) );
					
					error.setRmseAgg(rmseAggSpr, rmseAggDeb, rmseNonWei); //, rmseNonWei);
					error.setMaeAgg(maeAggSpr, maeAggDeb, maeNonWei); //, maeNonWei);

					
					db_ins.storeError(error, isSpread, spreader, debunker);
				}
				System.out.println();
				break;

			case -1:
				System.out.println("Configuration "+conf+" could not be found");
				break;
			}

		}
	}
	
	/**
	 * Computes ablation errors
	 * @param data sim data
	 * @param db_ins db con
	 * @param ablation ablation data
	 * @param newsUID news id
	 * @param isSpread whether state or msg-based
	 * @throws IncompatibleParameterException
	 */
	public void errorAbl(DatasetData data, DatabaseConnector db_ins, DatasetData ablation, int newsUID, boolean isSpread) throws IncompatibleParameterException {
		
		
		if(db_ins.checkAblationPresent(data.getConfigurationUID(), ablation.getConfigurationUID(), isSpread))
			return;
		
		ErrorCalc error = new ErrorCalc();
		
		//System.out.println("sim load "+sim.getLengthLoad());
		//System.out.println("data load "+data.getLengthLoad());
					
		//System.out.println("sims newsUID " +sim.getNewsUID());
		error.setNewsUID(newsUID);
		
					
		double rmseAggSpr = obtainGlobalMsgRMSE(ablation.getSpreaders(), data.getSpreaders(), isSpread);
		double rmseAggDeb = obtainGlobalMsgRMSE(ablation.getDeniers(), data.getDeniers(), isSpread);

					
		double msdAggSpr = obtainGlobalMsgMSD(ablation.getSpreaders(), data.getSpreaders(), isSpread);
		double msdAggDeb = obtainGlobalMsgMSD(ablation.getDeniers(), data.getDeniers(), isSpread);
					
					
		double rmseNonWei = (rmseAggSpr + rmseAggDeb) / (data.getTotalNumberSprs() + data.getTotalNumberDebs() - 1);
		double msdNonWei =(msdAggSpr + msdAggDeb) / (data.getTotalNumberSprs() + data.getTotalNumberDebs() - 1);
		
		error.setRmseAgg(rmseAggSpr, rmseAggDeb, rmseNonWei); //, rmseNonWei);
		error.setMSD(msdAggSpr, msdAggDeb, msdNonWei); //, maeNonWei);

					
		db_ins.storeErrorAbl(error, isSpread, data, ablation);
				

		
	}
	
	
	/**
	 * Calculates the RMSE for endorsers and vaccinated users
	 * @param simulation the simulated dataset
	 * @param real the real dataset
	 * @param isSpread whether state or msg-based
	 * @return error
	 */
	public double obtainGlobalMsgMae(ArrayList<Integer> simulation, ArrayList<Integer> real, boolean isSpread) {

		double sum1 = 0.0;
		int aggregate_end_real = 0;
		int aggregate_end_sim = 0;
		
		for(int i=0; i<real.size(); i++) {
			if(isSpread) {//already aggregated
				aggregate_end_real = real.get(i); 
				aggregate_end_sim = simulation.get(i);
			}else { //we aggregate it
				aggregate_end_real += real.get(i); 
				aggregate_end_sim += simulation.get(i);
			}
			
			 //two datasets
			double val1 = Double.valueOf(Math.abs(aggregate_end_real - aggregate_end_sim)) / Double.valueOf(aggregate_end_real + 1); //1 to avoid indeterminations
			
			sum1 = sum1 + val1;
			//System.out.println("aggregate_end_real "+aggregate_end_real+", aggregate_end_sim " + aggregate_end_sim + ", sum1 "+sum1);
			

		}
		
		sum1 = sum1 / (double)real.size(); //divide by length (ticks or minutes)
		//System.out.println("sum1 "+sum1);
		
		return sum1;
		
	}
	


	/**
	 * Calculates the RMSE for endorsers and vaccinated users
	 * @param simulation the simulated dataset
	 * @param real the real dataset
	 * @param isSpread whether state or msg-based
	 * @return error
	 */
	public double obtainGlobalMsgRMSE(ArrayList<Integer> simulation, ArrayList<Integer> real, boolean isSpread) {



		double rmse= 0.0;

		double sum1 = 0.0;
		int aggregate_end_real = 0;
		int aggregate_end_sim = 0;
		
		for(int i=0; i<real.size(); i++) {
			if(isSpread) {//already aggregated
				aggregate_end_real = real.get(i);
				aggregate_end_sim = simulation.get(i); 
			}else {//we aggregate it
				aggregate_end_real += real.get(i);
				aggregate_end_sim += simulation.get(i);
			}
			 //two datasets
			double val1 = aggregate_end_real - aggregate_end_sim;

			val1 = Math.pow(val1, 2);

			sum1 = sum1 + val1;

		}
		sum1 = sum1 / (double)real.size(); //divide by length (ticks or minutes)
		rmse = Math.sqrt(sum1);

		return rmse;
		
	}
	
	
	
	/**
	 * Calculates the MSD for endorsers and vaccinated users
	 * @param simulation the simulated dataset
	 * @param real the real dataset
	 * @param isSpread whether state or msg-based
	 * @return error
	 */
	public double obtainGlobalMsgMSD(ArrayList<Integer> simulation, ArrayList<Integer> real, boolean isSpread) {

		double sum1 = 0.0;
		int aggregate_end_real = 0;
		int aggregate_end_sim = 0;
		
		for(int i=0; i<real.size(); i++) {
			if(isSpread) { //already aggregated
				aggregate_end_real = real.get(i); 
				aggregate_end_sim = simulation.get(i);
			}else {//we aggregate it
				aggregate_end_real += real.get(i);
				aggregate_end_sim += simulation.get(i);
			}
			
			 //two datasets
			double val1 = Double.valueOf(aggregate_end_real - aggregate_end_sim) ; //1 to avoid indeterminations
			
			sum1 = sum1 + val1;
			//System.out.println("aggregate_end_real "+aggregate_end_real+", aggregate_end_sim " + aggregate_end_sim + ", sum1 "+sum1);
			

		}
		
		sum1 = sum1 / (double)real.size(); //divide by length (ticks or minutes)
		//System.out.println("sum1 "+sum1);
		
		return sum1;
		
	}

}
