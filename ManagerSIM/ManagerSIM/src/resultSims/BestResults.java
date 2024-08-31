package resultSims;


import databaseCon.DatabaseConnector;
import dataset.DatasetData;
import dataset.DatasetNamedData;
import exception.IncompatibleParameterException;

public class BestResults {

	private DatasetData first;
	private DatasetData second;
	private DatasetNamedData firstNamed;
	private DatasetNamedData secondNamed;
	/**
	 * Constructor for BestResults.
	 */
	public BestResults() {} 

	/**
	 * Loads the datasets (real and best simulated for a specific model) and displays them.
	 * @param model model selected
	 * @param spreadUID spreadUID
	 * @param newsUID the real dataset to use
	 * @param selError error to use
	 * @param isSpread whether it is state-based or msg-based
	 * @param type type
	 * @param config to load
	 * @throws IncompatibleParameterException
	 */
	public void loadBestResult(int model, int spreadUID, int newsUID, String selError, boolean isSpread, int type, int config) throws IncompatibleParameterException{

		DatabaseConnector db_ins = DatabaseConnector.getInstance();

		first = db_ins.loadDataset(newsUID, isSpread); //dataset
		first.trimForMetrics(); //only work with trimmed
		second = db_ins.loadBestResult(first, model, spreadUID, selError, isSpread, type, config);
		second.setErrorName(selError);
	}
	
	
	/**
	 * Loads the datasets (real and best simulated for a specific model and for state and msg) and displays them.
	 * @param model model selected
	 * @param spreadUID spreadUID
	 * @param newsUID the real dataset to use
	 * @param selError error to use
	 * @param isSpread whether it is state-based or msg-based
	 * @param type type
	 * @param config to load
	 * @throws IncompatibleParameterException
	 */
	public void loadBestResultSprAndAgg(int model, int spreadUID, int newsUID, String selError, boolean isSpread, int type, int config) throws IncompatibleParameterException{

		DatabaseConnector db_ins = DatabaseConnector.getInstance();

		first = db_ins.loadDataset(newsUID, isSpread); //dataset
		first.trimForMetrics(); //only work with trimmed
		second = db_ins.loadBestResultGlobal(first, model, spreadUID, selError, type, config);
		second.setErrorName(selError);
	}
	
	/**
	 * Loads the datasets (real and best simulated for a specific model, with user names) and displays them.
	 * @param model model selected
	 * @param spreadUID spreadUID
	 * @param newsUID the real dataset to use
	 * @param selError error to use
	 * @param config to load
	 * @throws IncompatibleParameterException
	 */
	public void loadBestResultName(int model, int spreadUID, int newsUID, String selError, int config) throws IncompatibleParameterException{

		DatabaseConnector db_ins = DatabaseConnector.getInstance();

		firstNamed = db_ins.loadDatasetName(newsUID); //dataset
		firstNamed.trimForMetrics(); //only work with trimmed
		secondNamed = db_ins.loadBestResultName(firstNamed, model, spreadUID, selError, config);

	} 

	/**
	 * Loads ablation
	 * @param valueLoadAblation ablation id to load
	 * @return dataset with ablation
	 * @throws IncompatibleParameterException
	 */
	public DatasetData loadAblation(int valueLoadAblation) throws IncompatibleParameterException {
		DatabaseConnector db_ins = DatabaseConnector.getInstance();
		
		return db_ins.loadAblation(valueLoadAblation, first, second.getConfigurationUID(), second.getExecution(), first.getNewsUID(), first.isSpread());
		
		
	}

	/**
	 * Returns first dataset loaded
	 * @return dataset
	 */
	public DatasetData getFirst() {
		 return first;
	}

	/**
	 * Returns second dataset loaded
	 * @return dataset
	 */
	public DatasetData getSecond() {
		return second;
	}
	
	/**
	 * Returns first named dataset loaded
	 * @return dataset
	 */
	public DatasetNamedData getFirstNamed() {
		 return firstNamed;
	}

	/**
	 * Returns second named dataset loaded
	 * @return dataset
	 */
	public DatasetNamedData getSecondNamed() {
		return secondNamed;
	}


}
