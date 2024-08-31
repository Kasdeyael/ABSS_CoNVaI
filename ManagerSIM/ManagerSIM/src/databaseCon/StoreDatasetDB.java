package databaseCon;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import dataset.DatasetInput;
import dataset.Diffusion;
import dataset.DiffusionString;
import dataset.MessagePerUser;
import exception.IncompatibleParameterException;

public class StoreDatasetDB {

	private ConnectionMgr connection;

	/**
	 * Constructor for InputDataset
	 * @param con connection manager for DB.
	 */
	public StoreDatasetDB(ConnectionMgr con) {
		this.connection = con;
	}

	/**
	 * Checks in the DB the last newsUID loaded and gets the next int for dataset.
	 * @return next DatasetUID.
	 * @throws IncompatibleParameterException if dataset doesn't exist
	 */
	private int getNextNewsUID() throws IncompatibleParameterException{
		Connection con = null;
		int id = 0;
		try {
			con = connection.getConnection();
			String sql = "SELECT MAX(newsUID) as newsUID FROM news_information";
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);

			if (rs.next()) {
				//System.out.println(rs.getInt("newsUID"));
				id = rs.getInt("newsUID") + 1;
			}else {
				id = 1;
			}
			
			connection.closeConnection();

		}catch (SQLException | IncompatibleParameterException ex) {

			connection.closeConnection();

			throw new IncompatibleParameterException("Error connecting to the DB");
		}

		return id;

	}

	/**
	 * Loads the dataset, parsed into percentages for user spread or endorsers/deniers into the database.
	 * @param dataIn the info on the dataset
	 * @throws IncompatibleParameterException if dataset doesn't exist or load failed
	 */
	public void loadDataset(DatasetInput dataIn) throws IncompatibleParameterException {
		Connection con = null;
		File file = new File(dataIn.getFilename());
		if(!file.exists()) throw new IncompatibleParameterException("the dataset doesn't exist or couldn't be found.");
		int nextNewsUID = getNextNewsUID();

		int batchSize = 20;
		try {

			con = connection.getConnection();
			String sql = "INSERT INTO news_information (newsUID,name,author,probInfl,novelty) VALUES (?,?,?,?,?)";
			PreparedStatement statement = con.prepareStatement(sql);

			statement = con.prepareStatement(sql);
			statement.setInt(1, nextNewsUID);
			statement.setString(2, dataIn.getName());
			statement.setString(3, dataIn.getAuthor());
			statement.setDouble(4, dataIn.getProbInfl());
			statement.setDouble(5, dataIn.getNovelty());
			statement.executeUpdate();

			con.setAutoCommit(false);
			if (dataIn.getStates() != null) { //diff
				//System.out.println("STATES");
				sql = "INSERT INTO real_dataset_spread (newsUID,timestamp,supporting,denying,neutral) VALUES (?,?,?,?,?)";
				statement = con.prepareStatement(sql);
				for (Diffusion dif : dataIn.getStates()) {
					statement.setInt(1, nextNewsUID);
					statement.setInt(2, dif.getTick());
					statement.setInt(3, dif.getInfected());
					statement.setInt(4, dif.getDebunker());
					statement.setInt(5, dif.getNeutral());
					statement.addBatch();
					if((dif.getTick()) % batchSize == 0) statement.executeBatch();
				}
				statement.executeBatch();
			}
			
			if (dataIn.getSpread() != null) { //diff
				//System.out.println("MSG");
				sql = "INSERT INTO user_diff_per_news (newsUID,tick,userUID,vaccinated,infected) VALUES (?,?,?,?,?)";
				statement = con.prepareStatement(sql);
				for (DiffusionString dif : dataIn.getSpread()) {
					Map<String, MessagePerUser> infects = dif.getTotalNames();
					for (String user: infects.keySet()){
						
						statement.setInt(1, nextNewsUID);
						statement.setInt(2, dif.getTick());
						statement.setLong(3, Long.valueOf(user));
						statement.setInt(4, infects.get(user).getDebunker());
						statement.setInt(5, infects.get(user).getInfected());
						statement.addBatch();
						if((dif.getTick()) % batchSize == 0) statement.executeBatch();
					}
					
				}
				statement.executeBatch();
			}
			if (dataIn.getSpread() != null) {

				//System.out.println("MSG");
				sql = "INSERT INTO real_dataset_msg (newsUID,timestamp,supporting,denying) VALUES (?,?,?,?)";
				statement = con.prepareStatement(sql);
				for (DiffusionString dif : dataIn.getSpread()) {
					statement.setInt(1, nextNewsUID);
					statement.setInt(2, dif.getTick());
					statement.setInt(3, dif.getInfected());
					statement.setInt(4, dif.getDebunker());
					statement.addBatch();
					if((dif.getTick()) % batchSize == 0) statement.executeBatch();
				}
				statement.executeBatch();
			}
			
			con.commit();
			con.setAutoCommit(true);
			connection.closeConnection();
		} catch (NumberFormatException | SQLException e) {

			try {
				con.setAutoCommit(true);
				
				String sql;
				sql = "DELETE FROM real_dataset_spread WHERE newsUID ="+nextNewsUID;
				PreparedStatement statement = con.prepareStatement(sql);
				statement.executeUpdate();
				
				sql = "DELETE FROM real_dataset_msg WHERE newsUID ="+nextNewsUID;
				statement = con.prepareStatement(sql);
				statement.executeUpdate();
				
				sql = "DELETE FROM user_diff_per_news WHERE newsUID ="+nextNewsUID;
				statement = con.prepareStatement(sql);
				statement.executeUpdate();
				
				sql = "DELETE FROM news_information WHERE newsUID ="+nextNewsUID;
				statement = con.prepareStatement(sql);
				statement.executeUpdate();

				connection.closeConnection();

				throw new IncompatibleParameterException("Error parsing dataset. It was deleted from DB: "+e.getMessage());
			} catch (SQLException exc) {
				connection.closeConnection();
				throw new IncompatibleParameterException("Error parsing dataset. Could not delete partial results from DB");
			}
		}


	}




}
