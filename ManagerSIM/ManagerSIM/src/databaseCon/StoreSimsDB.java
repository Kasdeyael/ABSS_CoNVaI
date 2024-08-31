package databaseCon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import dataset.DiffusionSpread;
import dataset.DiffusionString;
import dataset.MessagePerUser;
import exception.IncompatibleParameterException;

public class StoreSimsDB {

	private int confUID;
	private int modelType;
	private int spreadUID;
	private String nameFile;
	private ArrayList<Integer> news;
	private ArrayList<String> newsNames;
	private ConnectionMgr connection;

	/**
	 * Constructor for StoreDB.
	 * @param con connection for DB
	 */
	public StoreSimsDB(ConnectionMgr con) {
		this.connection = con;

	}

	/**
	 * Main method to insert the parameter file and the simulation file into the database.
	 * @param paramFile configuration
	 * @param simFile simulation results
	 * @param newsFileDir file of the news shared
	 * @return config number
	 * @throws IncompatibleParameterException
	 */
	public int loadFilesDB(File paramFile, File simFile, File newsFileDir) throws IncompatibleParameterException {
		Connection con = null;
		try {
			confUID = -1;
			modelType = -1;
			spreadUID = -1;
			
			con = connection.getConnection();
			//gather main info into the class
			setMainData(paramFile,con); 
			//news_being_sent
			setSpreadUID(con,newsFileDir);
			//load main_configuration and initial_data_configuration
			insertConfig(paramFile,con);
			//load initial_probabilities depending on modelType
			insertProbs(paramFile,con);
			//load state_per_run
			insertStepsCSV(simFile,con);
			connection.closeConnection();
			return confUID;
		} catch(IncompatibleParameterException e) {

			if(con!=null && confUID!=-1) removeLoadedInfo(con);
			connection.closeConnection();
			throw new IncompatibleParameterException("File could not be loaded, removed from DB.");
		}

	}



	/**
	 * Main method to insert the threads file and the simulation file into the database.
	 * @param paramFile configuration
	 * @param simFile simulation results
	 * @param thrFile where threads are
	 * @param newsFileDir file of the news shared
	 * @return config number
	 * @throws IncompatibleParameterException
	 */
	public int loadThreadsDB(File paramFile, File simFile, File[] thrFile, File newsFileDir) throws IncompatibleParameterException {
		Connection con = null;
		try {
			confUID = -1;
			modelType = -1;
			spreadUID = -1;
			
			con = connection.getConnection();

			//first loads the config. Others only load runs into steps run
			//gather main info into the class
			setMainData(paramFile,con); 
			//news_being_sent
			setSpreadUID(con,newsFileDir);
			//load main_configuration and initial_data_configuration
			insertConfig(paramFile,con);
			//load initial_probabilities depending on modelType
			insertProbs(paramFile,con);
			//load state_per_run
			insertStepsCSV(simFile,con);
			for (File fl : thrFile) {
				//System.out.println(fl.getName());
				//load state_per_run
				insertStepsJson(fl,con);
			}

			connection.closeConnection();
			return confUID;
		} catch(IncompatibleParameterException e) {
	
			if(con!=null && confUID!=-1) removeLoadedInfo(con);
			connection.closeConnection();
			throw new IncompatibleParameterException("File could not be loaded, removed from DB: "+e.getMessage());
		}
	}
	
	/**
	 * Returns confUID loaded.
	 * @return confUID
	 */
	public int getConfUID() {
		return confUID;
	}

	/**
	 * Loads the main parameters (model) about the batch run into the class for later use.
	 * @param paramFile the configuration file
	 * @param con db connection
	 * @throws IncompatibleParameterException 
	 */
	private void setMainData(File paramFile, Connection con) throws IncompatibleParameterException {
		//System.out.println("MAIN DATA");
		confUID = getNextConfiguration(con);
		//System.out.println(confUID);
		try {
			BufferedReader lineReader = new BufferedReader(new FileReader(paramFile));

			
			String[] data = lineReader.readLine().split(",");
			String[] firstLine = lineReader.readLine().split(",");

			for(int i = 0; i<data.length; i++) {
				String nameH = data[i].replaceAll("\"", "");
				if(nameH.equals("modelType")) { //we found model
					modelType = Integer.parseInt(firstLine[i]);
					break;
				}
			}
			for(int i = 0; i<data.length; i++) {
				String nameH = data[i].replaceAll("\"", "");
				if(nameH.equals("fileNews")) { //we found model
					if (firstLine[i].split("news_content_").length > 1) {
						nameFile = firstLine[i];

					}
					
					break;
				}
			}
			//System.out.println("Model: "+modelType);
			nameFile = nameFile.split("/")[(nameFile.split("/").length)-1];
			nameFile = nameFile.split("\"")[0];
			//System.out.println("NameFile: "+nameFile);
			lineReader.close();
			if(modelType == -1) throw new IncompatibleParameterException("Could not find the model. Simulation files are incomplete");
		} catch(NumberFormatException | IOException ex) {
			throw new IncompatibleParameterException("Could not parse parameter file");
		} 



	}

	/**
	 * Loads the probabilities per model into the database.
	 * @param paramFile the configuration file
	 * @param con db connection
	 * @throws IncompatibleParameterException
	 */
	private void insertProbs(File paramFile, Connection con) throws IncompatibleParameterException {
		//System.out.println("PROBS!");
		int batchSize = 50;
		try {
			BufferedReader lineReader = new BufferedReader(new FileReader(paramFile));
			String[] first = lineReader.readLine().split(",");

			if(modelType >= 1) { //type is 1
				int probRead = -1, useProbReply = -1, k = -1, confidence= -1, 
						engagement=-1, probInf = -1, probDeb = -1, probInfl = -1, probReply = -1, 
						probChange = -1, probOpinion = -1, noveltyFactor = -1, seed = -1;
				for(int i = 0; i<first.length; i++) {
					String nameH = first[i].replaceAll("\"", "");
					if(nameH.equals("probRead")) {
						probRead = i; 
					} else if(nameH.equals("probInfect")) {
						probInf = i; 
					} else if(nameH.equals("probDebunk")) {
						probDeb = i; 
					} else if(nameH.equals("probInfl")) {
						probInfl = i; 
					} else if(nameH.equals("probReply")) {
						probReply = i; 
					} else if(nameH.equals("probChange")) {
						probChange = i; 
					} else if(nameH.equals("probOpinion")) {
						probOpinion = i; 
					} else if(nameH.equals("noveltyFactor")) {
						noveltyFactor = i; 
					}else if(nameH.equals("randomSeed")) {
						seed = i; 
					}else if(nameH.equals("confidence")) {
						confidence = i; 
					}else if(nameH.equals("engagement")) {
						engagement = i; 
					}else if(nameH.equals("useReply")) {
						useProbReply = i; 
					}else if(nameH.equals("k")) {
						k = i; 
					}
				}
				if (probChange == -1 || probDeb == -1 || probInf == -1) {
					lineReader.close();
					throw new IncompatibleParameterException("Could not parse parameter file, missing parameters (common).");
				}
				if (modelType == 2 && engagement == -1) {
					lineReader.close();
					throw new IncompatibleParameterException("Could not parse parameter file (M2), missing parameters (engagement).");
				}
				if(modelType >2 && (probRead == -1 || probInfl == -1 ||
						probReply == -1 || probOpinion == -1 ||
								noveltyFactor == -1 || seed == -1 || k == -1 || useProbReply == -1)) {
					lineReader.close();
					throw new IncompatibleParameterException("Could not parse parameter file (M3-M6), missing parameters (M3 rest).");
				}
				if(modelType == 6 && confidence != -1) {
					lineReader.close();
					throw new IncompatibleParameterException("Could not parse parameter file (M6), missing parameters (confidence).");
				}
				
				String line = null;
				String sql = "INSERT INTO data_configuration_by_model (configurationUID,execution,probRead,"
						+ "probInfect,probDebunk,probInfl,probReply,probChange,probOpinion,noveltyFactor,randomSeed,confidence,engagement,useProbReply,k) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				PreparedStatement statement = con.prepareStatement(sql);

				//System.out.println(statement.toString());
				int count = 0;
				while((line = lineReader.readLine()) != null) {
					count++;
					String[] data = line.split(",");
					if(first.length != data.length) {
						lineReader.close();
						throw new IncompatibleParameterException("Could not parse config file, missing parameters");
					}
					
					int run = Integer.parseInt(data[0]);
					statement.setInt(1, confUID);
					statement.setInt(2, run);
					statement.setDouble(4, Double.valueOf(data[probInf]));
					statement.setDouble(5, Double.valueOf(data[probDeb]));
					statement.setDouble(8, Double.valueOf(data[probChange]));
					statement.setDouble(11, Double.valueOf(data[seed]));
					if (modelType <= 2) {
						
						statement.setNull(3, java.sql.Types.DOUBLE);
						statement.setNull(6, java.sql.Types.DOUBLE);
						statement.setNull(7, java.sql.Types.DOUBLE);
						statement.setNull(9, java.sql.Types.DOUBLE);
						statement.setNull(10, java.sql.Types.DOUBLE);
						statement.setNull(14, java.sql.Types.DOUBLE);
						statement.setNull(15, java.sql.Types.DOUBLE);
						
					}
					if (modelType == 1) {
						statement.setNull(13, java.sql.Types.DOUBLE);
						
						
					}else if (modelType == 2) {
						statement.setDouble(13, Double.valueOf(data[engagement]));
						
					}
					else if (modelType>2) {
						statement.setDouble(3, Double.valueOf(data[probRead]));
						statement.setDouble(6, Double.valueOf(data[probInfl]));
						statement.setDouble(7, Double.valueOf(data[probReply]));
						statement.setDouble(9, Double.valueOf(data[probOpinion]));
						statement.setDouble(10, Double.valueOf(data[noveltyFactor]));
						statement.setBoolean(14, Boolean.valueOf(data[useProbReply].replace('"', ' ').strip()));
						statement.setDouble(15, Double.valueOf(data[k]));
						
						statement.setNull(13, java.sql.Types.DOUBLE);
					}
					if (modelType == 6) {
						statement.setDouble(12, Double.valueOf(data[confidence]));
					} else {
						statement.setNull(12, java.sql.Types.DOUBLE);
					}
					//System.out.println(statement.toString());

					statement.addBatch();

					if(count % batchSize == 0) statement.executeBatch();
					
				}
				statement.executeBatch();
					
				lineReader.close();
			}
			


		}catch(NumberFormatException |IOException | SQLException ex){

			throw new IncompatibleParameterException("Error while parsing and loading parameter file into DB: "+ex.getMessage());
		}

	}
	
	/**
	 * Reads main database, finds highest configurationUID and returns the next.
	 * @param con db connection
	 * @return newsFileDir file of the news shared
	 * @throws IncompatibleParameterException
	 */
	private void setSpreadUID(Connection con, File newsFileDir) throws IncompatibleParameterException{
		
		//System.out.println("file dir: " + newsFileDir);
		if (newsFileDir.exists() && newsFileDir.isDirectory()) {

			newsNames = new ArrayList<String>();
			news = new ArrayList<Integer>();
			ArrayList<Integer> ticks = new ArrayList<Integer>();
			//System.out.println("SEARCHING: "+nameFile);
			File[] files = newsFileDir.listFiles(c -> c.getName().equals(nameFile));
			//System.out.println(files.toString());
			if (files.length != 1) throw new IncompatibleParameterException("News file used was not found!");
			
			//System.out.println("News file: "+files[0].getAbsolutePath());
			try (BufferedReader lineReader = new BufferedReader(new FileReader(files[0]))) {
				String line = null;
				lineReader.readLine(); //header
				
				while((line = lineReader.readLine()) != null) {
					//newsUID, author, info
					String[] data = line.split(",");
					if (data.length < 3) throw new IncompatibleParameterException("News file does not have the right format.");
					String name = data[0]; //name
					System.out.println("Name: "+name);
					newsNames.add(name);
					ticks.add(Integer.valueOf(data[2]));
					//System.out.println(Integer.valueOf(data[2]));
				
				}
			} catch (NumberFormatException | IOException e) {
				throw new IncompatibleParameterException("Error while setting spreadUID: "+e.getMessage());
			}
			for (String name : newsNames) {
				try {
					
					String sql = "SELECT newsUID FROM news_information WHERE name LIKE '"+name+"'";
					Statement st = con.createStatement();
					ResultSet rs = st.executeQuery(sql);
					int id = 0;
					if(rs.next()) {
						id = rs.getInt("newsUID");
						news.add(id);
					}
					//System.out.println("NewsID: "+id+", for conf: "+confUID);
				} catch ( SQLException ex) {
					throw new IncompatibleParameterException("Error while connecting to the DB");
	
				}
				
			}
			
			if (newsNames.size() != news.size() || news.size() != ticks.size()) 
				throw new IncompatibleParameterException("NewsUID found: "+news+", while searching for: "+newsNames.size()+". DB does not contain such news.");
			
			try {
				String v = "(";
				for (int i = 0; i< news.size(); i++) {
					if (i != 0) v += ",";
					v += ""+news.get(i);
				}
				v += ")";
				String sql = "SELECT spreadUID FROM news_being_sent WHERE newsUID IN "+v+" GROUP BY spreadUID HAVING COUNT(spreadUID) = "+news.size();
				//System.out.println(sql);
				Statement st = con.createStatement();
				ResultSet rs = st.executeQuery(sql);
	
				if(rs.next()) { //preexisting config
					spreadUID = rs.getInt("spreadUID");
					System.out.println("Spread: "+spreadUID+", NewsUID: "+news.get(0)+", confUID: "+confUID);
	
				} else {
					
					sql = "SELECT MAX(spreadUID) as spreadUID FROM news_being_sent";
					//System.out.println(sql);
					st = con.createStatement();
					rs = st.executeQuery(sql);
	
					if(rs.next()) {
						spreadUID = rs.getInt("spreadUID") + 1;
						//System.out.println(spreadUID);
					}
					if (spreadUID == -1) {
						throw new IncompatibleParameterException("Problem connecting to the DB to recover spreadUID.");
	
					}
					//was not inserted, get next and do so
					insertNewsSent(con, ticks);
					
				}
			} catch ( SQLException ex) {
				throw new IncompatibleParameterException("Error while connecting to the DB");
	
			}
			
		} else {
			throw new IncompatibleParameterException("News file dir does not exist");
			
		}
			
	
	
	}
	
	/**
	 * Inserts the new spreadUID assigned to a set of news (simultaneous diff) 
	 * @param con db connection
	 * @param ticks list of when the news are sent
	 * @throws IncompatibleParameterException
	 */
	private void insertNewsSent(Connection con, ArrayList<Integer> ticks) throws IncompatibleParameterException{
		try {

			String sql = "INSERT INTO news_being_sent (spreadUID,newsUID,tick) VALUES (?,?,?)";
			//System.out.println(sql);
			PreparedStatement statement = con.prepareStatement(sql);
			for (int i=0; i<ticks.size();i++) {
				
				statement.setInt(1, spreadUID);
				statement.setInt(2, news.get(i));
				statement.setInt(3, ticks.get(i));
				//System.out.println(statement.toString());
				statement.executeUpdate();
				
			}
			
		} catch(SQLException ex){
			throw new IncompatibleParameterException("Error while loading news file into DB");
		}
	}
	
	/**
	 * Loads the main databases with the simulation run information and other static parameters.
	 * @param paramFile the configuration file
	 * @param con db connection
	 * @throws IncompatibleParameterException
	 */
	private void insertConfig(File paramFile, Connection con) throws IncompatibleParameterException{

		try {
			
			String sql = "INSERT INTO initial_data_configuration (configurationUID,spreadUID,modelType,netType,linksPerNode,networkSeed,initialNodesNetwork,probConnect) VALUES (?,?,?,?,?,?,?,?)";
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setInt(1, confUID);

			double pCon = -1;
			int links = -1, netSeed = -1, iniNodes = -1, nwType = -1;

			
			BufferedReader lineReader = new BufferedReader(new FileReader(paramFile));
			String[] header = lineReader.readLine().split(",");
			String[] data = lineReader.readLine().split(",");
			for(int i = 0; i<header.length; i++) {
				String nameH = header[i].replaceAll("\"", "");
				if(nameH.equals("initialNodes")) {
					iniNodes = Integer.parseInt(data[i]);
				} else if(nameH.equals("linksPerNode")) {
					links = Integer.parseInt(data[i]);
				} else if(nameH.equals("networkSeed")) {
					netSeed = Integer.parseInt(data[i]);
				}else if(nameH.equals("probNetwork")) {
					pCon = Double.parseDouble(data[i]);
				} else if(nameH.equals("networkType")) {
					if (data[i].equals("File-Load")) {
						nwType = 1;
					} else {
						nwType = (data[i].equals("Barabasi-Albert")) ? 2 : 3; //File-load, 2 BA, 3 ER
					}
					

				}

				if(links!= -1 && netSeed != -1 && iniNodes != -1 && pCon != -1 && nwType != -1) break; //they're found
			}
			lineReader.close();
												
			if(links== -1 || netSeed == -1 || iniNodes == -1 || pCon == -1) {
				throw new IncompatibleParameterException("Could not parse simulation file, missing parameters");
			}
			statement.setInt(2, spreadUID);
			statement.setInt(3, modelType);
			statement.setInt(4, nwType);
			if(nwType == 2) statement.setInt(5, links);
			else statement.setNull(5, java.sql.Types.SMALLINT);
			statement.setInt(6, netSeed);
			if(nwType == 2) statement.setInt(7, iniNodes);
			else statement.setNull(7, java.sql.Types.SMALLINT);
			if(nwType == 3) statement.setDouble(8, pCon);
			else statement.setNull(8, java.sql.Types.DOUBLE);

			//System.out.println(statement.toString());

			statement.executeUpdate();

		}catch(NumberFormatException |IOException ex) {

			throw new IncompatibleParameterException("Error while parsing parameter file");
		} catch ( SQLException ex) {

			try {
				con.rollback();

			} catch (SQLException exc) {

				throw new IncompatibleParameterException("Error while loading parameter file into DB, could not delete partial results");
			}
		}

	}

	/**
	 * Loads the ticks per run with its state into the database.
	 * @param simFile simulation results
	 * @param con db connection
	 * @throws IncompatibleParameterException
	 */
	private void insertStepsCSV(File simFile, Connection con) throws IncompatibleParameterException{

		int batchSize = 20;
		//System.out.println("STEPS!");
		try {

			con.setAutoCommit(false);
			String sql = "INSERT INTO state_per_run (configurationUID,execution,tick,vaccinated,infected,neutral) VALUES (?,?,?,?,?,?)";
			PreparedStatement statement = con.prepareStatement(sql);
			//System.out.println(statement.toString());
			BufferedReader lineReader = new BufferedReader(new FileReader(simFile));

			String line = null;
			lineReader.readLine();

			int count = 0;
			while((line = lineReader.readLine()) != null) {
				count++;
				String[] data = line.split(",");
				if(data.length<5) {
					lineReader.close();
					throw new IncompatibleParameterException("Could not parse simulation file, wrong format.");
				}
				int run = Integer.parseInt(data[0]);
				int tick = (int)Double.parseDouble(data[1]);
				int infected = Integer.parseInt(data[2]);
				int neutral = Integer.parseInt(data[3]);
				int vaccinated = Integer.parseInt(data[4]);
				
				statement.setInt(1, confUID);
				statement.setInt(2, run);
				statement.setInt(3, tick);
				statement.setInt(4, vaccinated);
				statement.setInt(5, infected);
				statement.setInt(6, neutral);
				statement.addBatch();

				if(count % batchSize == 0) statement.executeBatch();
			}

			lineReader.close();

			statement.executeBatch();

			con.commit();
			con.setAutoCommit(true);

		} catch (NumberFormatException ex) {

			throw new IncompatibleParameterException("Could not parse simulation file, wrong format.");
		} catch(IOException ex) {

			throw new IncompatibleParameterException("Error while parsing parameter file");
		} catch ( SQLException ex) {

			try {
				con.rollback();

			} catch (SQLException exc) {
				throw new IncompatibleParameterException("Error while loading parameter file into DB, could not delete partial results");
			}
		}


	}


	/**
	 * Reads main database, finds highest configurationUID and returns the next.
	 * @param con db connection
	 * @return next configurationUID value
	 * @throws IncompatibleParameterException
	 */
	private int getNextConfiguration(Connection con) throws IncompatibleParameterException{
		
		try {

			String sql = "SELECT MAX(configurationUID) as configurationUID FROM initial_data_configuration";
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);

			int id = 0;
			if(rs.next()) {
				id = rs.getInt("configurationUID") + 1;
			}
			
			return id;
		} catch ( SQLException ex) {

			throw new IncompatibleParameterException("Error while connecting to the DB");

		}


	}
	

	/**
	 * Inserts diffusion steps from the json file
	 * @param thrFile thread file
	 * @param con db connection
	 * @throws IncompatibleParameterException
	 */
	private void insertStepsJson(File thrFile, Connection con) throws IncompatibleParameterException {
		int batchSize = 20;
		//System.out.println(thrFile.getAbsolutePath());
		try {
			con.setAutoCommit(false);
			String sql = "INSERT INTO msg_per_run (configurationUID,execution,newsUID,tick,vaccinated,infected) VALUES (?,?,?,?,?,?)";
			PreparedStatement statement = con.prepareStatement(sql);
			
			String sql2 = "INSERT INTO user_diff_per_sims (configurationUID,execution,newsUID,tick,userUID,vaccinated,infected) VALUES (?,?,?,?,?,?,?)";
			PreparedStatement statement2 = con.prepareStatement(sql2);

			File[] filesS = thrFile.listFiles(c -> c.isFile());
			//System.out.println("Loading: "+filesS.length);
			for (File file : filesS) { // each news in folder (multinews)
				//System.out.println("File: "+file.getAbsolutePath());

				ObjectMapper mapper = new ObjectMapper();
				// Read a single attribute
				try {
						DiffusionSpread msgSpread = mapper.readValue(file, DiffusionSpread.class);

						int run = msgSpread.getRun_number();
						//System.out.println("run " + run);
						sql = "SELECT newsUID from news_information WHERE NAME LIKE '"+msgSpread.getNewsID()+"'";
						//System.out.println(sql);
						Statement st = con.createStatement();
						ResultSet rs = st.executeQuery(sql);
						int newsUID;
						if (rs.next()) {
							newsUID = rs.getInt("newsUID");
						}else throw new IncompatibleParameterException("Error retrieving the newsUID from the DB for the steps.");
						//System.out.println("news " + newsUID+", for the spread: "+spreadUID+", for conf: "+confUID);
						//System.out.println("news " + msgSpread.getDiffusion());
						List<DiffusionString> diff = msgSpread.getDiffusion();
						//System.out.println(diff.size());
						for (int i = 0; i< diff.size(); i ++) { //i is tick
							
							
							if (modelType !=1) { //not doing for model 1 due to MILLIONS of messages
								Map<String, MessagePerUser> infsAndDebs = diff.get(i).getTotalNames();
								for (String user: infsAndDebs.keySet()){ //all users
									
									statement2.setInt(1, confUID);
									statement2.setInt(2, run);
									statement2.setInt(3, newsUID);
									statement2.setInt(4, diff.get(i).getTick());
 
									statement2.setLong(5, Long.valueOf(user));
									statement2.setInt(6, infsAndDebs.get(user).getDebunker());  //number of msgs user sent in that period
									statement2.setInt(7, infsAndDebs.get(user).getInfected());
									
									statement2.addBatch();
									if((diff.get(i).getTick()) % batchSize == 0) {
										statement2.executeBatch();
									}
								}
							}
							
							statement.setInt(1, confUID);
							statement.setInt(2, run);
							statement.setInt(3, newsUID);
							statement.setInt(4, diff.get(i).getTick());
							statement.setInt(5, diff.get(i).getDebunker());
							statement.setInt(6, diff.get(i).getInfected());

							//System.out.println(statement.toString());
							
							statement.addBatch();


							//System.out.println(statement.toString());
							if((diff.get(i).getTick()) % batchSize == 0) {
								statement.executeBatch();
							}
							
						}

						statement.executeBatch();
						statement2.executeBatch();
						
				} catch (IOException e) {
						System.out.println("ERROR FORMAT!");
						// TODO Auto-generated catch block
						throw new IncompatibleParameterException("Error reading and creating the diffusion from the file: "+e.getMessage());
				}
					
			}
			
			con.commit();

			con.setAutoCommit(true);

			
		} catch (NumberFormatException ex) {

			throw new IncompatibleParameterException("Could not parse simulation file, wrong format.");
		} catch(IOException ex) {

			throw new IncompatibleParameterException("Error while parsing parameter file");
		} catch ( SQLException ex) {

			try {
				con.rollback();

			} catch (SQLException exc) {
				throw new IncompatibleParameterException("Error while loading parameter file into DB, could not delete partial results");
			}
		}


	}
	
	
	/**
	 * Removes any loaded info in the DB if there was a partial load.
	 * @param con db connection
	 * @throws IncompatibleParameterException
	 */
	private void removeLoadedInfo(Connection con) throws IncompatibleParameterException{

		
		try {
			con.setAutoCommit(true);
			String sql = "DELETE FROM state_per_run WHERE configurationUID="+confUID;
			con.prepareStatement(sql).executeUpdate();
			sql = "DELETE FROM msg_per_run WHERE configurationUID="+confUID;
			con.prepareStatement(sql).executeUpdate();
			sql = "DELETE FROM user_diff_per_sims WHERE configurationUID="+confUID;
			con.prepareStatement(sql).executeUpdate();
			sql = "DELETE FROM data_configuration_by_model WHERE configurationUID="+confUID;
			con.prepareStatement(sql).executeUpdate();
			sql = "DELETE FROM initial_data_configuration WHERE configurationUID="+confUID;
			con.prepareStatement(sql).executeUpdate();
			
			
		} catch ( SQLException ex) {
			throw new IncompatibleParameterException("Could not restore DB.");

		}
	}
}
