package databaseCon;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import dataset.DatasetData;
import dataset.DatasetNamedData;
import exception.IncompatibleParameterException;

public class LoadDataset {

	private ConnectionMgr connection;
	private int configUID;
	private int exec;

	/**
	 * Constructor for OutputDataset
	 * @param con connection manager for DB.
	 */
	public LoadDataset(ConnectionMgr con) {
		this.connection = con;
	}


	/**
	 * Loads a dataset into memory.
	 * @param newsUID
	 * @param whether to load state (true) or msgs
	 * @return the dataset
	 * @throws IncompatibleParameterException if load failed
	 */
	public DatasetData loadDataset(int newsUID, boolean isSpread) throws IncompatibleParameterException {

		Connection con = null;
		DatasetData data = new DatasetData(newsUID, isSpread);
		
		try {
			con = connection.getConnection();
			
			String sql;
			if (isSpread) sql= "SELECT * FROM real_dataset_spread WHERE newsUID = "+newsUID+" ORDER BY timestamp ASC";
			else sql= "SELECT * FROM real_dataset_msg WHERE newsUID = "+newsUID+" ORDER BY timestamp ASC";
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);

			if(rs.next()) {
				int spreader = rs.getInt("supporting");
				int denier = rs.getInt("denying");
				data.addValues(spreader,denier);
				while(rs.next()) {
	
					spreader = rs.getInt("supporting");
					denier = rs.getInt("denying");
					data.addValues(spreader,denier);
				}

			} else {
					throw new IncompatibleParameterException("Could not load dataset "+newsUID+" with isSpread "+isSpread);
			}

			sql= "SELECT name FROM news_information WHERE newsUID = "+newsUID;
			st = con.createStatement();
			rs = st.executeQuery(sql);
			if(rs.next()) {
				data.setNewsName(rs.getString("name"));
			}
		} catch ( SQLException ex) {
			connection.closeConnection();
			throw new IncompatibleParameterException("Could not load dataset");

		}
		connection.closeConnection();
		return data;
	}
	
	/**
	 * Loads a dataset into memory with user names
	 * @param newsUID
	 * @return the dataset
	 * @throws IncompatibleParameterException if load failed
	 */
	public DatasetNamedData loadDatasetName(int newsUID) throws IncompatibleParameterException {

		Connection con = null;
		DatasetNamedData data = new DatasetNamedData(newsUID);
		try {
			con = connection.getConnection();
			
			String sql= "SELECT * FROM user_diff_per_news WHERE newsUID = "+newsUID+" ORDER BY tick ASC";
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);
			
			if(rs.next()) {
				int tick = rs.getInt("tick");
				Long user = rs.getLong("userUID");
				int vacVal = rs.getInt("vaccinated");
				int infVal = rs.getInt("infected");
				data.addValues(tick, Long.toString(user), infVal, vacVal);
				while(rs.next()) {
	
					tick = rs.getInt("tick");
					user = rs.getLong("userUID");
					vacVal = rs.getInt("vaccinated");
					infVal = rs.getInt("infected");
					data.addValues(tick, Long.toString(user), infVal, vacVal);
				}

			} else {
					throw new IncompatibleParameterException("Could not load dataset "+newsUID);
			}
			
			sql= "SELECT name FROM news_information WHERE newsUID = "+newsUID;
			st = con.createStatement();
			rs = st.executeQuery(sql);
			if(rs.next()) {
				data.setNewsName(rs.getString("name"));
			}

			
		} catch ( SQLException ex) {
			connection.closeConnection();
			throw new IncompatibleParameterException("Could not load dataset");

		}
		connection.closeConnection();
		return data;
	}


	/**
	 * Loads the best result for a model and a newsUID. If no result was found, throws error.
	 * @param real Dataset
	 * @param model number 
	 * @param spreadUID 
	 * @param selError error selected (RMSE, MAE) must appear in db
	 * @param isSpread whether it is spread or msg
	 * @param type type 
	 * @param config configUID
	 * @return simulation with best model loaded
	 * @throws IncompatibleParameterException
	 */
	public DatasetData loadBestResult(DatasetData real, int model, int spreadUID, String selError, boolean isSpread, int type, int config) throws IncompatibleParameterException {
		configUID = -1;
		exec = -1;
		getResult(model,real.getNewsUID(), spreadUID, selError, isSpread, type, config);

		return loadSimulation(configUID, real, exec, real.getNewsUID());
	}

	
	/**
	 * Loads the global best result for a model and a newsUID. If no result was found, throws error.
	 * @param real Dataset
	 * @param model number 
	 * @param spreadUID 
	 * @param selError error selected (RMSE, MAE) must appear in db
	 * @param type type 
	 * @param config configUID
	 * @return simulation with best model loaded
	 * @throws IncompatibleParameterException
	 */
	public DatasetData loadBestResultGlobal(DatasetData real, int model, int spreadUID, String selError, int type, int config) throws IncompatibleParameterException {
		configUID = -1;
		exec = -1;
		getResultGlobal(model,real.getNewsUID(), spreadUID, selError, type, config); //NO SE

		return loadSimulation(configUID, real, exec, real.getNewsUID());
	}
	
	

	/**
	 * Checks if said model for a specific dataset has results loaded and gets its configuration info.
	 * @param model model to compare
	 * @param newsUID dataset used for the calculations
	 * @param spreadUID 
	 * @param selError error selected (RMSE, MAE) must appear in db
	 * @param isSpread whether it is spread or msg
	 * @param type type 
	 * @param config configUID
	 * @throws IncompatibleParameterException
	 */
	private void getResult(int model, int newsUID, int spreadUID, String selError, boolean isSpread, int type, int config)  throws IncompatibleParameterException{
		Connection con = null;
		System.out.println("news: "+newsUID+", spread: "+spreadUID);
		try {
			con = connection.getConnection();
			String added_clause = "";
			String added_join = "";
			String conf = "";
			if (config > 0) {
				conf = " AND initial_data_configuration.configurationUID = "+config;
			
			}
			if(model>=4) {
				added_join="INNER JOIN data_configuration_by_model ON initial_data_configuration.configurationUID = data_configuration_by_model.configurationUID ";
				switch (type) {
				case 1:
		
					added_clause = " AND useProbReply=0 AND k=1";
					break;
				case 2:
					added_clause = " AND useProbReply=1";
					break;
				case 3:
					added_clause = " AND useProbReply=0 AND k=0";
					break;
				}
			}
			String sql;
			if(isSpread) sql = "SELECT state_rmse_results.configurationUID, state_rmse_results.execution, "+selError+" FROM state_rmse_results INNER JOIN "
					+ "initial_data_configuration ON initial_data_configuration.configurationUID = state_rmse_results.configurationUID "
					+ added_join
					+ "WHERE modelType = "+model+ conf+" AND spreadUID = "+spreadUID+ added_clause+ " ORDER BY "+selError+" ASC, execution ASC LIMIT 1";
			
			else sql = "SELECT msg_rmse_results.configurationUID, msg_rmse_results.execution, "+selError+" FROM msg_rmse_results INNER JOIN "
					+ "initial_data_configuration ON initial_data_configuration.configurationUID = msg_rmse_results.configurationUID "
					+ added_join
					+ "WHERE modelType = "+model+ conf+" AND newsUID = "+newsUID+" AND spreadUID = "+spreadUID+ added_clause+" ORDER BY "+selError+", execution ASC LIMIT 1";
			
			Statement st = con.createStatement();
			//System.out.println(sql);
			ResultSet rs = st.executeQuery(sql);
			if(rs.next()) {
				

				configUID = rs.getInt("configurationUID");
				exec = rs.getInt("execution");
				System.out.println("Dataset: "+newsUID+", Best "+selError+": " + rs.getDouble(selError)+", config: "+configUID+", exec: "+exec);

				sql = "SELECT * FROM simulation.data_configuration_by_model WHERE configurationUID = "+configUID+" AND execution = "+exec;
				st = con.createStatement();
				//System.out.println(sql);
				rs = st.executeQuery(sql);
				if(rs.next()) {
					System.out.println("Model: " + model+", probRead: "+rs.getDouble("probRead")+", probInfect: "+rs.getDouble("probInfect")+
							", probDebunk: "+rs.getDouble("probDebunk")+", probInfl: "+rs.getDouble("probInfl")+", probReply: "+rs.getDouble("probReply")+", probChange: "+rs.getDouble("probChange")+
						", probOpinion: "+rs.getDouble("probOpinion")+", noveltyFactor: "+rs.getDouble("noveltyFactor")+", randomSeed: "+rs.getInt("randomSeed")+", engagement: "+rs.getDouble("engagement")+
						", confidence: "+rs.getDouble("confidence") +", k: "+rs.getDouble("k")+", useProbReply: "+rs.getBoolean("useProbReply"));
					
				}
			} else {
				connection.closeConnection();
				System.out.println();
				throw new IncompatibleParameterException("No results loaded for said configuration and dataset: ");
			}

		}catch (SQLException ex) {
			connection.closeConnection();
			System.out.println();
			System.out.println(ex);
			throw new IncompatibleParameterException("SQLException, No results loaded for said configuration and dataset");

		}
		connection.closeConnection();
	} 
	
	
	
	
	/**
	 * Checks if said model for a specific dataset has results loaded and gets its configuration info. This one is for best global (state and msg).
	 * @param model model to compare
	 * @param newsUID dataset used for the calculations
	 * @param spreadUID 
	 * @param selError error selected (RMSE, MAE) must appear in db
	 * @param type type 
	 * @param config configUID
	 * @throws IncompatibleParameterException
	 */
	private void getResultGlobal(int model, int newsUID, int spreadUID, String selError, int type, int config)  throws IncompatibleParameterException{
		Connection con = null;
		System.out.println("news: "+newsUID+", spread: "+spreadUID);
		try {
			con = connection.getConnection();
			String added_clause = "";
			String added_join = "";
			String conf = "";
			if (config > 0) {
				conf = " AND initial_data_configuration.configurationUID = "+config;
			}
			
			if(model>=4) {
				added_join="INNER JOIN data_configuration_by_model ON initial_data_configuration.configurationUID = data_configuration_by_model.configurationUID ";
				switch (type) {
				case 1:
		
					added_clause = " AND useProbReply=0 AND k=1";
					break;
				case 2:
					added_clause = " AND useProbReply=1";
					break;
				case 3:
					added_clause = " AND useProbReply=0 AND k=0";
					break;
				}
			}
			String sql;
			sql = "SELECT state_rmse_results.configurationUID, state_rmse_results.execution, state_rmse_results."+selError+" AS state_err, msg_rmse_results."+selError+" AS msg_err, state_rmse_results."+selError+"+msg_rmse_results."+selError+" AS Added"+selError
					+ " FROM state_rmse_results "
					+ "INNER JOIN initial_data_configuration ON initial_data_configuration.configurationUID = state_rmse_results.configurationUID "
					+ added_join
					+ "INNER JOIN msg_rmse_results ON (msg_rmse_results.configurationUID = state_rmse_results.configurationUID AND msg_rmse_results.execution = state_rmse_results.execution)"
					+ "WHERE modelType = "+model+ conf+" AND spreadUID = "+spreadUID+ added_clause+ " ORDER BY Added"+selError+", execution ASC LIMIT 1";
			
			
			
			Statement st = con.createStatement();
			//System.out.println(sql);
			ResultSet rs = st.executeQuery(sql);
			if(rs.next()) {
				

				configUID = rs.getInt("configurationUID");
				exec = rs.getInt("execution");
				System.out.println("Dataset: "+newsUID+", Best state: "+rs.getDouble("state_err")+", Best msg: "+rs.getDouble("msg_err")+", Best Added"+selError+": " + rs.getDouble("Added"+selError)+", config: "+configUID+", exec: "+exec);

				sql = "SELECT * FROM simulation.data_configuration_by_model WHERE configurationUID = "+configUID+" AND execution = "+exec;
				st = con.createStatement();
				//System.out.println(sql);
				rs = st.executeQuery(sql);
				if(rs.next()) {
					System.out.println("Model: " + model+", probRead: "+rs.getDouble("probRead")+", probInfect: "+rs.getDouble("probInfect")+
							", probDebunk: "+rs.getDouble("probDebunk")+", probInfl: "+rs.getDouble("probInfl")+", probReply: "+rs.getDouble("probReply")+", probChange: "+rs.getDouble("probChange")+
						", probOpinion: "+rs.getDouble("probOpinion")+", noveltyFactor: "+rs.getDouble("noveltyFactor")+", randomSeed: "+rs.getInt("randomSeed")+", engagement: "+rs.getDouble("engagement")+
						", confidence: "+rs.getDouble("confidence") +", k: "+rs.getDouble("k")+", useProbReply: "+rs.getBoolean("useProbReply"));
					
				}
			} else {
				connection.closeConnection();
				System.out.println();
				throw new IncompatibleParameterException("No results loaded for said configuration and dataset");
			}

		}catch (SQLException ex) {
			connection.closeConnection();
			System.out.println();
			System.out.println(ex);
			throw new IncompatibleParameterException("SQLException, No results loaded for said configuration and dataset");

		}
		connection.closeConnection();
	} 
	
	
	/**
	 * Checks if said model for a specific dataset has results loaded and gets its configuration info. (for named results
	 * @param model model to compare
	 * @param newsUID dataset used for the calculations
	 * @param spreadUID 
	 * @param selError error selected (RMSE, MAE) must appear in db
	 * @param config configUID
	 * @throws IncompatibleParameterException
	 */
	private void getResultNamed(int model, int newsUID, int spreadUID, String selError, int config)  throws IncompatibleParameterException{
		Connection con = null;
		System.out.println("news: "+newsUID+", spread: "+spreadUID);
		try {
			con = connection.getConnection();
			String conf = "";
			if (config > 0) {
				conf = " AND initial_data_configuration.configurationUID = "+config;
			
			}
			String sql;
			sql = "SELECT msg_rmse_results.configurationUID, msg_rmse_results.execution, "+selError+" FROM msg_rmse_results INNER JOIN "
					+ "initial_data_configuration ON initial_data_configuration.configurationUID = msg_rmse_results.configurationUID "
					+ "WHERE modelType = "+model+conf+" AND newsUID = "+newsUID+" AND spreadUID = "+spreadUID+" ORDER BY "+selError+", execution ASC LIMIT 1";
			
			Statement st = con.createStatement();
			System.out.println(sql); 
			ResultSet rs = st.executeQuery(sql);
			if(rs.next()) {
				
				System.out.print("Dataset: "+newsUID+", Best "+selError+": " + rs.getDouble(selError));
				
				configUID = rs.getInt("configurationUID");
				exec = rs.getInt("execution");

				sql = "SELECT * FROM simulation.data_configuration_by_model WHERE configurationUID = "+configUID+" AND execution = "+exec;
				st = con.createStatement();
				System.out.println(sql);
				rs = st.executeQuery(sql);
				if(rs.next()) {
					System.out.println(". Model: " + model+", probRead: "+rs.getDouble("probRead")+", probInfect: "+rs.getDouble("probInfect")+
							", probDebunk: "+rs.getDouble("probDebunk")+", probInfl: "+rs.getDouble("probInfl")+", probReply: "+rs.getDouble("probReply")+", probChange: "+rs.getDouble("probChange")+
						", probOpinion: "+rs.getDouble("probOpinion")+", noveltyFactor: "+rs.getDouble("noveltyFactor")+", randomSeed: "+rs.getInt("randomSeed")+", engagement:"+rs.getDouble("engagement")+", confidence:"+rs.getDouble("confidence"));
					
				}
			} else {
				connection.closeConnection();
				System.out.println();
				throw new IncompatibleParameterException("No results loaded for said configuration and dataset");
			}

		}catch (SQLException ex) {
			connection.closeConnection();
			System.out.println();
			System.out.println(ex);
			throw new IncompatibleParameterException("SQLException, No results loaded for said configuration and dataset");

		}
		connection.closeConnection();
	}

	/**
	 * Loads the simulation for a specific configuration and execution.
	 * @param configurationUID configuration it's loading
	 * @param dataset dataset it's going to compare with
	 * @param exec execution to load
	 * @param newsUID dataset news
	 * @return dataset loaded for such configuration
	 */
	public DatasetData loadSimulation(int configurationUID, DatasetData dataset, int exec, int newsUID) throws IncompatibleParameterException{

		DatasetData simulation = new DatasetData();
		simulation.setConfigurationUID(configurationUID);
		simulation.setExecution(exec);
		simulation.setIsSpread(dataset.isSpread());
		simulation.setNewsUID(newsUID);
		Connection con = null;
		try {
			con = connection.getConnection();

			String sql;
			if(dataset.isSpread()) sql = "SELECT * FROM state_per_run WHERE configurationUID="+configurationUID+" AND execution="+exec+" ORDER BY tick ASC";
			else sql = "SELECT * FROM msg_per_run WHERE configurationUID="+configurationUID+" AND execution="+exec+" AND newsUID=" +newsUID+" ORDER BY tick ASC";
			Statement st = con.createStatement();

			ResultSet rs = st.executeQuery(sql);
			int endorsers = 0;
			int deniers = 0;
			
			while(rs.next()) {
				
				endorsers = rs.getInt("infected");
				deniers = rs.getInt("vaccinated");
				simulation.addValues(endorsers, deniers);
			}
			simulation.fillWithLastOrZero(dataset.getLengthLoad());
			

		} catch ( SQLException ex) {
			connection.closeConnection();
			throw new IncompatibleParameterException("Simulation could not be found for those parameters");

		}
		connection.closeConnection();
		return simulation;
	}
	
	/**
	 * Loads the simulation (with usernames) for a specific configuration and execution.
	 * @param configurationUID configuration it's loading
	 * @param dataset dataset it's going to compare with
	 * @param exec execution to load
	 * @param newsUID dataset news
	 * @return dataset loaded for such configuration
	 */
	public DatasetNamedData loadSimulationName(int configurationUID, DatasetNamedData dataset, int exec, int newsUID) throws IncompatibleParameterException{

		DatasetNamedData simulation = new DatasetNamedData();
		simulation.setConfigurationUID(configurationUID);
		simulation.setExecution(exec);
		simulation.setNewsUID(dataset.getNewsUID());
		Connection con = null;
		try {
			con = connection.getConnection();

			String sql;
			sql = "SELECT * FROM user_diff_per_sims WHERE configurationUID="+configurationUID+" AND execution="+exec+" AND newsUID=" +newsUID+" ORDER BY tick ASC";
			Statement st = con.createStatement();
			
			ResultSet rs = st.executeQuery(sql);
			
			
			Long user;
			int infVal, vacVal, tick;
			while(rs.next()) { 
	
				tick = rs.getInt("tick");
				user = rs.getLong("userUID");
				vacVal = rs.getInt("vaccinated");
				infVal = rs.getInt("infected");
				simulation.addValues(tick, Long.toString(user), infVal, vacVal);
			}
				
				simulation.fillWithLastOrZero(dataset.getLength());


		} catch ( SQLException ex) {
			connection.closeConnection();
			throw new IncompatibleParameterException("Simulation could not be found for those parameters");

		}
		connection.closeConnection();
		return simulation;
	}

	/**
	 * Gets display info on all the datasets loaded in the DB.
	 * @return map with dataset name and index.
	 * @throws IncompatibleParameterException
	 */
	public HashMap<Integer,String> checkAvailableDatasets() throws IncompatibleParameterException{
		HashMap<Integer, String> arrayData = new HashMap<Integer,String>();
		Connection con = null;
		try {
			con = connection.getConnection();

			String sql = "SELECT news_being_sent.spreadUID AS spreadUID, news_information.name AS name FROM news_being_sent JOIN news_information ON"
					+ " news_being_sent.newsUID = news_information.newsUID";

			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);
			while(rs.next()) {

				arrayData.put(rs.getInt("spreadUID"), rs.getString("name"));

			}

		} catch ( SQLException ex) {
			connection.closeConnection();
			throw new IncompatibleParameterException("No datasets could be found");

		}
		connection.closeConnection();

		return arrayData;
	}


	/**
	 * Gets the available configs for a spreadUID
	 * @param spreadUID
	 * @return list of configs
	 * @throws IncompatibleParameterException
	 */
	public ArrayList<Integer> getAvailableConfigs(int spreadUID) throws IncompatibleParameterException {
		ArrayList<Integer> configs = new ArrayList<Integer>();
		Connection con = null;
		try {
			con = connection.getConnection();
			
			String sql = "SELECT configurationUID FROM initial_data_configuration WHERE spreadUID = "+spreadUID;

			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);
			while(rs.next()) {

				configs.add(rs.getInt("configurationUID"));

			}

		} catch ( SQLException ex) {
			connection.closeConnection();
			throw new IncompatibleParameterException("No datasets could be found");

		}
		connection.closeConnection();
		
		return configs;
	}

	/**
	 * Returns news from a spreadUID
	 * @param spreadUID
	 * @return list of news for that spread
	 * @throws IncompatibleParameterException
	 */
	public ArrayList<Integer> getNewsFromSpread(int spreadUID) throws IncompatibleParameterException {
		ArrayList<Integer> newsUIDs = new ArrayList<Integer>();
		Connection con = null;
		try {
			con = connection.getConnection();
			
			String sql = "SELECT newsUID FROM news_being_sent WHERE spreadUID = "+spreadUID;

			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);
			while(rs.next()) {

				newsUIDs.add(rs.getInt("newsUID"));

			}

		} catch ( SQLException ex) {
			connection.closeConnection();
			throw new IncompatibleParameterException("No datasets could be found");

		}
		connection.closeConnection();
		
		return newsUIDs;
	}

	/**
	 * Loads the best result (with names) for a model and a newsUID. If no result was found, throws error.
	 * @param real Dataset
	 * @param model number 
	 * @param spreadUID 
	 * @param selError error selected (RMSE, MAE) must appear in db
	 * @param config configUID
	 * @return simulation with best model loaded
	 * @throws IncompatibleParameterException
	 */
	public DatasetNamedData loadBestResultName(DatasetNamedData real, int model, int spreadUID, String selError, int config) throws IncompatibleParameterException {
		configUID = -1; 
		exec = -1;
		getResultNamed(model,real.getNewsUID(), spreadUID, selError, config);
		DatasetNamedData data_res = loadSimulationName(configUID, real, exec, real.getNewsUID());
		data_res.setErrorName(selError);
		return data_res;
	}

	
	/**
	 * Loads the ablation correspondent with a specific simulation and an ablation id.
	 * @param valueLoadAblation the id for the ablation
	 * @param real simulation
	 * @param config configuration from sim
	 * @param execution exec
	 * @param newsUID
	 * @param isSpread whether it is spread or not
	 * @return dataset loaded
	 * @throws IncompatibleParameterException
	 */
	public DatasetData loadAblation(int valueLoadAblation, DatasetData real, int config, int execution, int newsUID, boolean isSpread) throws IncompatibleParameterException {

		configUID = -1; 
		exec = -1;
		String nameAbl = "";
		int modelBasedVal;
		switch(valueLoadAblation) { //probInfl low:0, probInfl high:1, novelty low:2, novelty high:3, only user:4, only news:5, k low 6, k high 7, probInfl low:8, probInfl high:9, novelty low:10, novelty high:11
		case 4: //user
			nameAbl = "ablation w/o news";
			modelBasedVal = 3;
			break;
		case 5: //News
			nameAbl = "ablation w/o user";
			modelBasedVal = 4;
			break;
		default: //rest is the same
			if (valueLoadAblation % 2 != 0) { // 1, 3, 7, 9, 11
				nameAbl = "high";
			} else {
				nameAbl = "low";
			}
			modelBasedVal = 0;
			break;
			
		}
		//extract params from config
		Connection con = null;
		try {
			con = connection.getConnection();
			
			String sql = "SELECT * FROM data_configuration_by_model "
					+ "INNER JOIN initial_data_configuration ON initial_data_configuration.configurationUID = data_configuration_by_model.configurationUID "+
					"WHERE initial_data_configuration.configurationUID = "+config+" AND execution = "+execution;
			
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);
			if(rs.next()) {
				int model = rs.getInt("modelType");
				System.out.println("Model: "+model);
				double probInfect, probDebunk, probChange, probRead, probInfl, randomSeed, noveltyFactor, probOpinion, k;
				switch (model){
					case 1: //probInfect, probDebunk, probChange

						probInfect = rs.getDouble("probInfect");
						probDebunk = rs.getDouble("probDebunk");
						probChange = rs.getDouble("probChange");
						sql = "SELECT * FROM data_configuration_by_model "
								+ "INNER JOIN initial_data_configuration ON initial_data_configuration.configurationUID = data_configuration_by_model.configurationUID "+
								"WHERE probInfect="+probInfect+" AND probDebunk="+probDebunk+" AND modelType="+(modelBasedVal!=0? modelBasedVal: model);
						break;
					case 5://+ probRead, probInfl, probReply, randomSeed, noveltyFactor, probOpinion, k
						probInfect = rs.getDouble("probInfect");
						probDebunk = rs.getDouble("probDebunk");
						probChange = rs.getDouble("probChange");
						probRead = rs.getDouble("probRead");
						probInfl = rs.getDouble("probInfl");
						randomSeed = rs.getInt("randomSeed");
						noveltyFactor = rs.getDouble("noveltyFactor");
						probOpinion = rs.getDouble("probOpinion");
						k = rs.getDouble("k");
						String v = valueLoadAblation == 6? "<" : valueLoadAblation == 7? ">": "=";
						sql = "SELECT * FROM data_configuration_by_model "
								+ "INNER JOIN initial_data_configuration ON initial_data_configuration.configurationUID = data_configuration_by_model.configurationUID "
								+ "INNER JOIN news_being_sent ON news_being_sent.spreadUID = initial_data_configuration.spreadUID "+
								"WHERE probInfect="+probInfect+" AND probDebunk="+probDebunk+" AND probChange="+probChange+
								" AND probRead="+probRead+" AND probInfl="+probInfl+ //" AND probReply="+probReply+
								" AND randomSeed="+randomSeed+
								" AND noveltyFactor="+noveltyFactor+" AND probOpinion="+probOpinion+" AND k"+v+k+" AND modelType="+(modelBasedVal!=0? modelBasedVal: model);
						break;
					default:
						connection.closeConnection();
						throw new IncompatibleParameterException("Error parsing the parameters!");
						
				}
				
			//check config for those params
			//newsUID is same if changing modelType. Otherwise, extract newsUID
			
			int news=0;
			
			if (modelBasedVal!=0 || valueLoadAblation == 6 || valueLoadAblation == 7) { // model 3, 4 and k
				if(modelBasedVal==0) { //not modelbased

					nameAbl = "ablation k "+ nameAbl;
				}
				sql = sql+" AND newsUID="+newsUID;
				news = newsUID;
			} else { //novelty, probinfl
				if (valueLoadAblation < 2) {
					nameAbl = "ablation 0.1 infl "+ nameAbl;
				} else if (valueLoadAblation < 4) {
					nameAbl = "ablation 0.1 nov "+ nameAbl;
				} else if (valueLoadAblation < 10) {
					nameAbl = "ablation 0.2 infl "+ nameAbl;
				} else if (valueLoadAblation < 12) {
					nameAbl = "ablation 0.2 nov "+ nameAbl;
				}
				
				if (valueLoadAblation > 3) {
					valueLoadAblation = valueLoadAblation - 4;
				}
				String sq2 = "SELECT name,author FROM news_information WHERE newsUID="+newsUID;
				st = con.createStatement();
				rs = st.executeQuery(sq2);
				String name, author;
				if (rs.next()) {
					name = rs.getString("name");
					author = rs.getString("author");
					String start = name.substring(name.length()-8, name.length()); //last 8
					start += valueLoadAblation; //already prepared
					sq2 = "SELECT newsUID FROM news_information WHERE name LIKE '"+start+"%' AND author="+author;
					System.out.println(sq2);
					st = con.createStatement();
					rs = st.executeQuery(sq2);
					if (rs.next()) {
						news = rs.getInt("newsUID");
					} else {
						connection.closeConnection();
						System.out.println("Ablation data not loaded! Might not exist...");
						return null; //not found!
						
					}
				} else {
					connection.closeConnection();
					throw new IncompatibleParameterException("The news could not be extracted.");
				}
				
				
				sql = sql+" AND newsUID="+String.valueOf(news);
			}
				
			System.out.println(sql);
			st = con.createStatement();
			rs = st.executeQuery(sql);
			
			if(rs.next()) {
				configUID = rs.getInt("configurationUID");
				exec = rs.getInt("execution");
				System.out.println("FOUND: config: "+configUID+"; exec "+exec+" for model: "+modelBasedVal);
			} else {
				System.out.println("Not found!");
				connection.closeConnection();
				return null; //not found!
			}
			connection.closeConnection();

			DatasetData dataAbl = loadSimulation(configUID, real, exec, news);

			dataAbl.setName(nameAbl);
			return dataAbl;
			//loadSimulation(configUID, real, exec, real.getNewsUID());
			} else {
				connection.closeConnection();
				throw new IncompatibleParameterException("No original run found!");
			}
			
			
		} catch ( SQLException ex) {
			connection.closeConnection();
			throw new IncompatibleParameterException("No datasets could be found");

		}
		
		
	}
	
	
	/**
	 * Checks if a newsUID corresponds with an ablation UID.
	 * @param newsUID
	 * @return true (corresponds) or false
	 * @throws IncompatibleParameterException
	 */
	public boolean isAblationNews(int newsUID) throws IncompatibleParameterException {
		
		//
		Connection con = null;
		try {
			con = connection.getConnection();
			String sql = "SELECT * FROM news_information WHERE newsUID = "+newsUID;
			
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);
			if(rs.next()) {
				String name = rs.getString("name");
				String author = rs.getString("author");
				sql = "SELECT * FROM news_information WHERE author = "+author;
				
				st = con.createStatement();
				rs = st.executeQuery(sql);
				while(rs.next()) { //check length
					if (name.length() < rs.getString("name").length()) {
						connection.closeConnection();
						return true;
					}
				}
			} else {

				connection.closeConnection();
				throw new IncompatibleParameterException("The newsUID could not be found");
			}
			
		} catch ( SQLException ex) {
			connection.closeConnection();
			throw new IncompatibleParameterException("No datasets could be found");

		}
		
		return false;
	}


}
