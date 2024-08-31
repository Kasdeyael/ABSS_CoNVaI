package databaseCon;

import java.io.File;
import java.util.HashMap;

import dataset.DatasetData;
import dataset.DatasetInput;
import dataset.DatasetNamedData;
import exception.IncompatibleParameterException;
import java.util.ArrayList;
import metricCalc.CurrentExec;
import metricCalc.ErrorCalc;

public class DatabaseConnector {

	private static DatabaseConnector db_instance = null;
	private LoadDataset outData = null;
	private StoreSimsDB str = null;
	private StoreDatasetDB dataIn = null;
	private StoreErrorsDB res = null;
	private ConnectionMgr con = null;

	/**
	 * Constructor for DatabaseConnector.
	 */
	private DatabaseConnector() {
	}

	/**
	 * Returns the instance of DatabaseConnector. Creates one if it doesn't exist yet.
	 * @return this object
	 */
	public static DatabaseConnector getInstance() {
		if(db_instance ==null) {
			db_instance = new DatabaseConnector();
		}
		return db_instance;
	}

	/**
	 * Sets the DB connection parameters.
	 * @param url 
	 * @param usr
	 * @param pass
	 * @throws IncompatibleParameterException
	 */
	public void setParameters(String url, String usr, String pass) throws IncompatibleParameterException {
		con = new ConnectionMgr(url, usr, pass);

	}

	/**
	 * Stores a simulation in DB.
	 * @param paramFile parameters
	 * @param simFile simulation results
	 * @param newsFileDir 
	 * @return configuration number loaded
	 * @throws IncompatibleParameterException
	 */
	public int storeSimulations(File paramFile, File simFile, File newsFileDir) throws IncompatibleParameterException {
		if(str == null) str = new StoreSimsDB(con);
		return str.loadFilesDB(paramFile, simFile, newsFileDir);
	}
	
	/**
	 * Stores many threads in DB.
	 * @param paramFile parameters
	 * @param simFile simulation results
	 * @param thrFiles simulation threads
	 * @param newsFileDir 
	 * @return configuration number loaded
	 * @throws IncompatibleParameterException
	 */
	public int storeThrSimulations(File paramFile, File simFile, File[] thrFiles,  File newsFileDir) throws IncompatibleParameterException {
		if(str == null) str = new StoreSimsDB(con);
		return str.loadThreadsDB(paramFile, simFile, thrFiles, newsFileDir);
	}

	/**
	 * Stores a dataset in DB.
	 * @param data dataset info
	 * @throws IncompatibleParameterException
	 */
	public void storeDataset(DatasetInput data) throws IncompatibleParameterException {
		if(dataIn==null) dataIn = new StoreDatasetDB(con);
		dataIn.loadDataset(data);
	}

	/**
	 * Stores error in DB.
	 * @param error error info
	 * @param isSpread if spread framework or not
	 * @param spreader weight
	 * @param debunker weight
	 * @throws IncompatibleParameterException
	 */
	public void storeError(ErrorCalc error, boolean isSpread, int spreader, int debunker) throws IncompatibleParameterException {
		if(res == null) res = new StoreErrorsDB(con);
		res.loadRMSE(error, isSpread, spreader, debunker);
	}

	/**
	 * Loads a dataset in memory for use.
	 * @param newsUID dataset to load
	 * @param isSpread spread framework or not
	 * @return real dataset
	 * @throws IncompatibleParameterException
	 */
	public DatasetData loadDataset(int newsUID, boolean isSpread) throws IncompatibleParameterException {
		if(outData==null) outData = new LoadDataset(con);
		return outData.loadDataset(newsUID, isSpread);
	}
	
	/**
	 * Loads a dataset (with user names, instead of aggregates) in memory for use.
	 * @param newsUID
	 * @return real dataset
	 * @throws IncompatibleParameterException
	 */
	public DatasetNamedData loadDatasetName(int newsUID) throws IncompatibleParameterException {
		if(outData==null) outData = new LoadDataset(con);
		return outData.loadDatasetName(newsUID);
	}

	/**
	 * Loads a simulation in memory.
	 * @param real real dataset
	 * @param model 
	 * @param spreadUID dataset to use
	 * @param selError error to use (RMSE; MAE...)
	 * @param isSpread spread framework or not
	 * @param type 
	 * @param config number 
	 * @return simulation dataset
	 * @throws IncompatibleParameterException
	 */
	public DatasetData loadBestResult(DatasetData real, int model, int spreadUID, String selError, boolean isSpread, int type, int config) throws IncompatibleParameterException {
		if(outData==null) outData = new LoadDataset(con);
		return outData.loadBestResult(real, model, spreadUID, selError, isSpread, type, config);	
	}
	
	/**
	 * Loads the best simulation (spread and msg) in memory.
	 * @param real real dataset
	 * @param model 
	 * @param spreadUID dataset to use
	 * @param selError error to use (RMSE; MAE...)
	 * @param type 
	 * @param config number 
	 * @return simulation dataset
	 * @throws IncompatibleParameterException
	 */
	public DatasetData loadBestResultGlobal(DatasetData real, int model, int spreadUID, String selError, int type, int config) throws IncompatibleParameterException {
		if(outData==null) outData = new LoadDataset(con);
		return outData.loadBestResultGlobal(real, model, spreadUID, selError, type, config);	
	}

	/**
	 * Loads specific simulation in memory. 
	 * @param configurationUID 
	 * @param dataset real dataset
	 * @param exec execution
	 * @return newsUID news
	 * @throws IncompatibleParameterException
	 */
	public DatasetData loadSimulation(int configurationUID, DatasetData dataset, int exec, int newsUID) throws IncompatibleParameterException {
		if(outData==null) outData = new LoadDataset(con);
		return outData.loadSimulation(configurationUID, dataset, exec, newsUID);
	}

	/**
	 * Loads countermeasures in the database.
	 * @param dataset to compare
	 * @param model model for countermeasures
	 * @param configurationLoad configuration used
	 * @return simulation dataset
	 * @throws IncompatibleParameterException
	 */
	/*
	public DatasetData loadCounter(DatasetData dataset, int model, int configurationLoad) throws IncompatibleParameterException {
		if(outData==null) outData = new LoadDataset(con);
		return outData.loadSimulationModel(dataset, model, configurationLoad);
	}
	*/
	/**
	 * Checks if a configuration has results loaded for the metrics. If they are incomplete, they get deleted. If 
	 * it's complete, it doesn't reload the metrics.
	 * @param configurationUID
	 * @param newsUID dataset selected
	 * @param current current execution with its data
	 * @param isSpread spread framework or not
	 * @return 0 if the results are loaded, 1 if missing values or none, -1 if the configuration is missing
	 * @throws IncompatibleParameterException
	 */
	public int checkConfiguration(int configurationUID, int newsUID, CurrentExec current, boolean isSpread) throws IncompatibleParameterException {
		// TODO Auto-generated method stub
		if(res==null) res = new StoreErrorsDB(con);
		return res.checkConfiguration(configurationUID, newsUID, current, isSpread);
	}

	/**
	 * Checks the real datasets loaded into the DB to select in the view.
	 * @return map with dataset name and its index.
	 * @throws IncompatibleParameterException
	 */
	public HashMap<Integer,String> checkAvailableDatasets() throws IncompatibleParameterException{
		if(outData==null) outData = new LoadDataset(con);
		return outData.checkAvailableDatasets();
	}
	
	/**
	 * Gets available configs for a specific spreadUID
	 * @param spreadUID dataset configs
	 * @return configUID for that spread
	 * @throws IncompatibleParameterException
	 */
	public ArrayList<Integer> getAvailableConfigs(int spreadUID) throws IncompatibleParameterException{
		if(outData==null) outData = new LoadDataset(con);
		return outData.getAvailableConfigs(spreadUID);
	}
	
	
	/**
	 * Loads newsUID for a specific spreadUID
	 * @param spreadUID
	 * @return newsUID list
	 * @throws IncompatibleParameterException
	 */
	public ArrayList<Integer> getNewsID(int spreadUID) throws IncompatibleParameterException {
		if(outData==null) outData = new LoadDataset(con);
		return outData.getNewsFromSpread(spreadUID);
	}
	
	/**
	 * Loads best simulation in memory, with names instead of agg spreads.
	 * @param real real dataset
	 * @param model 
	 * @param spreadUID dataset to use
	 * @param selError error to use (RMSE; MAE...)
	 * @param config number 
	 * @return simulation dataset
	 * @throws IncompatibleParameterException
	 */
	public DatasetNamedData loadBestResultName(DatasetNamedData real, int model, int spreadUID, String selError, int config) throws IncompatibleParameterException {
		// TODO Auto-generated method stub
		if(outData==null) outData = new LoadDataset(con);
		return outData.loadBestResultName(real, model, spreadUID, selError, config);	
	}


	/**
	 * Loads ablation for a specific config
	 * @param valueLoadAblation type of ablation (//probInfl low:0, probInfl high:1, novelty low:2, novelty high:3, user:4, news:5, k low 6, k high 7, probInfl low:8, probInfl high:9, novelty low:10, novelty high:11)
	 * @param real simulation dataset before ablation
	 * @param config configUID
	 * @param execution number exec
	 * @param newsUID news id
	 * @param isSpread whether it is spread or msg framework
	 * @return dataset
	 * @throws IncompatibleParameterException
	 */
	public DatasetData loadAblation(int valueLoadAblation, DatasetData real, int config, int execution, int newsUID, boolean isSpread) throws IncompatibleParameterException {
		if(outData==null) outData = new LoadDataset(con);
		return outData.loadAblation(valueLoadAblation, real, config, execution, newsUID, isSpread);	
		
	}
	
	/**
	 * Check if a newsUID is ablation or not
	 * @param newsUID to check
	 * @return true (ablation) or false
	 * @throws IncompatibleParameterException
	 */
	public boolean isAblation(int newsUID) throws IncompatibleParameterException {
		if(outData==null) outData = new LoadDataset(con);
		return outData.isAblationNews(newsUID);
	}

	/**
	 * Stores error for the ablation table.
	 * @param error errors
	 * @param isSpread whether is spread or not
	 * @param data the simulation before ablation
	 * @param ablation the ablation
	 * @throws IncompatibleParameterException
	 */
	public void storeErrorAbl(ErrorCalc error, boolean isSpread, DatasetData data, DatasetData ablation) throws IncompatibleParameterException {
		// TODO Auto-generated method stub
		if(res == null) res = new StoreErrorsDB(con);
		res.loadRMSEAbl(error, isSpread, data, ablation);
	}

	/**
	 * Checks if an ablation is present in db to load
	 * @param configurationUID config simulation
	 * @param configurationUIDAbl config ablation
	 * @param isSpread whether it is state-based or msg-based
	 * @return true (present) or false
	 * @throws IncompatibleParameterException
	 */
	public boolean checkAblationPresent(int configurationUID, int configurationUIDAbl, boolean isSpread) throws IncompatibleParameterException {
		// TODO Auto-generated method stub
		if(res==null) res = new StoreErrorsDB(con);
		return res.checkConfigurationAblation(configurationUID, configurationUIDAbl, isSpread);
	}

}
