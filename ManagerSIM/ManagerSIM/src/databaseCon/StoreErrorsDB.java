package databaseCon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import dataset.DatasetData;
import exception.IncompatibleParameterException;
import metricCalc.CurrentExec;
import metricCalc.ErrorCalc;

public class StoreErrorsDB {


	private ConnectionMgr connection;

	/**
	 * Constructor for InputDataset
	 * @param con connection manager for DB.
	 */
	public StoreErrorsDB(ConnectionMgr con) {
		this.connection = con;
	}

	/**
	 * Loads into the DB the error obtained for a specific configuration and execution.
	 * @param error calculated
	 * @param isSpread whether it is state or msg-based
	 * @param spreader weight
	 * @param debunker weight
	 * @throws IncompatibleParameterException
	 */
	public void loadRMSE(ErrorCalc error, boolean isSpread, int spreader, int debunker) throws IncompatibleParameterException{

		Connection con = null;
		
		try {
			con = connection.getConnection();

			String sql;
			if (isSpread) sql= "INSERT INTO state_rmse_results (configurationUID,execution,RMSE,RMSESpr,RMSEDeb,MAE,MAESpr,MAEDeb,weightSpr,weightDeb) VALUES (?,?,?,?,?,?,?,?,?,?)";
			else sql= "INSERT INTO msg_rmse_results (configurationUID,execution,newsUID,RMSE,RMSESpr,RMSEDeb,MAE,MAESpr,MAEDeb,weightSpr,weightDeb) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setInt(1, error.getConfigurationUID());
			statement.setInt(2, error.getExec());
			if (isSpread) {
				statement.setDouble(3, error.getAggRmse());
				statement.setDouble(4, error.getAggRmseSpr());
				statement.setDouble(5, error.getAggRmseDeb());
				statement.setDouble(6, error.getAggMae());
				statement.setDouble(7, error.getAggMaeSpr());
				statement.setDouble(8, error.getAggMaeDeb());
				statement.setDouble(9,spreader);
				statement.setDouble(10, debunker);
				
			}
			else {
				statement.setInt(3, error.getNewsUID());
				statement.setDouble(4, error.getAggRmse());
				statement.setDouble(5, error.getAggRmseSpr());
				statement.setDouble(6, error.getAggRmseDeb());
				statement.setDouble(7, error.getAggMae());
				statement.setDouble(8, error.getAggMaeSpr());
				statement.setDouble(9, error.getAggMaeDeb());
				statement.setDouble(10, spreader);
				statement.setDouble(11, debunker);
			}

			//System.out.println(statement.toString());
			statement.executeUpdate();
			connection.closeConnection();

		} catch ( SQLException | IncompatibleParameterException ex) {
			connection.closeConnection();
			throw new IncompatibleParameterException("Could not load error into the DB for (configuration,execution)"
					+ " ("+error.getConfigurationUID()+","+error.getExec()+"): "+ex.getMessage());

		}
	}


	/**
	 * Check if a configuration has results loaded. If any is missing, we delete them all.
	 * @param configurationUID config
	 * @param newsUID news
	 * @param current current exec to load
	 * @param isSpread whether it is state-based or msg-based
	 * @return 0 if the results were loaded, 1 if there were missing values or none at all, -1 if no sim found.
	 * @throws IncompatibleParameterException when there were no executions
	 */
	public int checkConfiguration(int configurationUID, int newsUID, CurrentExec current, boolean isSpread) throws IncompatibleParameterException {
		//we only have 2 models. check how many runs we have per model. Check rmse_results and count rows.
		current.clear();
		
		Connection con = null;

		try {
			con = connection.getConnection();
			System.out.println("SELECTING EXEC with conf "+configurationUID+", news "+newsUID);
			String sql = "SELECT MAX(execution) as execution FROM data_configuration_by_model WHERE configurationUID = "+configurationUID;
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);

			if(rs.next()) {
				//System.out.println("NEWSUID: "+newsUID);
				current.setMaxExec(rs.getInt("execution")); //total number of execs
				//System.out.println(rs.getInt("execution"));
				if(isSpread) sql = "SELECT COUNT(execution) as execution FROM state_rmse_results WHERE configurationUID = "+configurationUID;
				else sql = "SELECT COUNT(execution) as execution FROM msg_rmse_results WHERE configurationUID = "+configurationUID+" AND newsUID = "+newsUID;
				st = con.createStatement();
				rs = st.executeQuery(sql);
				
				if(rs.next() && current.getMaxExec()!=0) {

					int execsResult = rs.getInt("execution");

					if(current.getMaxExec() != execsResult) {

						if(isSpread)sql = "DELETE FROM state_rmse_results WHERE configurationUID = "+configurationUID;
						else sql = "DELETE FROM msg_rmse_results WHERE configurationUID = "+configurationUID +" AND newsUID = "+newsUID;
						PreparedStatement stat = con.prepareStatement(sql);
						stat.executeUpdate();

						connection.closeConnection();
						return 1;
					}

					else {
						connection.closeConnection();
						return 0;
					}
				}



			} else {
				connection.closeConnection();
				throw new IncompatibleParameterException("the database has no configurationUID = "+configurationUID); //no execs found
			}


		} catch ( SQLException ex) {
			connection.closeConnection();
			throw new IncompatibleParameterException("Could not check if "+configurationUID+" has results loaded");

		}
		connection.closeConnection();
		return -1;

	}
	
	/**
	 * Loads RMSE for ablation table.
	 * @param error error set
	 * @param isSpread whether it is state-based or msg-based
	 * @param data simulation data
	 * @param ablation ablation data
	 * @throws IncompatibleParameterException
	 */
	public void loadRMSEAbl(ErrorCalc error, boolean isSpread, DatasetData data, DatasetData ablation) throws IncompatibleParameterException {
		Connection con = null;

		try {
			con = connection.getConnection();

			String sql= "INSERT INTO errors_ablation (configuration_best,configuration_ablation,isSpread,RMSE,RMSESpr,RMSEDeb,MSD,MSDSpr,MSDDeb) VALUES (?,?,?,?,?,?,?,?,?)";
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setInt(1, data.getConfigurationUID());
			statement.setInt(2, ablation.getConfigurationUID());
			statement.setBoolean(3, isSpread);
			
			statement.setDouble(4, error.getAggRmse());
			statement.setDouble(5, error.getAggRmseSpr());
			statement.setDouble(6, error.getAggRmseDeb());
			statement.setDouble(7, error.getMsdAgg());
			statement.setDouble(8, error.getMsdAggSpr());
			statement.setDouble(9, error.getMsdAggDeb());
				
			
			//System.out.println(statement.toString());
			statement.executeUpdate();
			connection.closeConnection();

		} catch ( SQLException | IncompatibleParameterException ex) {
			connection.closeConnection();
			throw new IncompatibleParameterException("Could not load error into the DB for ablation (configuration_data,config_abl)"
					+ " ("+data.getConfigurationUID()+","+ablation.getConfigurationUID()+"): "+ex.getMessage());

		}
		
	}

	/**
	 * Check if ablation config is loaded 
	 * @param configurationUID config from simulation
	 * @param configurationUIDAbl config from ablation
	 * @param isSpread whether it is state or msg-based
	 * @return true (present) or false
	 * @throws IncompatibleParameterException
	 */
	public boolean checkConfigurationAblation(int configurationUID, int configurationUIDAbl, boolean isSpread) throws IncompatibleParameterException {

		Connection con = null;

		try {
			con = connection.getConnection();
			String sql = "SELECT configuration_best, configuration_ablation, isSpread FROM errors_ablation WHERE configuration_best = "+configurationUID +
					" AND configuration_ablation="+configurationUIDAbl+
					" AND isSpread= "+ (isSpread ? 1 : 0);
			//System.out.println(sql);
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);

			if(rs.next()) {
				connection.closeConnection();
				return true;
			}


		} catch ( SQLException ex) {
			connection.closeConnection();
			throw new IncompatibleParameterException("Could not check if "+configurationUID+" has results loaded");

		}
		connection.closeConnection();
		return false;
	}


}
