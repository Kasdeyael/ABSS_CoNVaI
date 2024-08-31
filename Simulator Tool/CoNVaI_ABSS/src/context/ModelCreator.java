package context;


import networkBuilding.*;
import agent.*;
import message.NewsPiece;
import message.State;
import message.Thread;
import model.Model;
import model.ModelSIR;
import model.ModelSIRExtended;
import model.ModelCoNVaI;
import model.ModelCoNVaIConfidence;

import java.io.IOException;
import exception.IncompatibleParameterException;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;

import au.com.bytecode.opencsv.CSVReader;
import cern.jet.random.Exponential;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import repast.simphony.context.Context;
import repast.simphony.context.space.graph.AbstractGenerator;
import repast.simphony.context.space.graph.ContextJungNetwork;
import repast.simphony.context.space.graph.NetworkBuilder;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ScheduleParameters;

import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;

import repast.simphony.space.graph.DefaultEdgeCreator;
import repast.simphony.space.graph.DirectedJungNetwork;
import repast.simphony.space.graph.Network;
import repast.simphony.util.collections.IndexedIterable;

public class ModelCreator {
	
	static String netFile;
	static String fileAgents;
	static String netType;
	double last_tick;
	String last_news;
	static Model model;
	static int modelType;
	static Context<Object> context;
	static ModelCreator instance;
	
	/**
	 * Gets instance of ModelCreator (if exists) or creates a new one. If preexisting, it restarts users and other values
	 * @return
	 */
	public static ModelCreator getInstance() {
		if (instance == null) { //if null, that is it
			instance = new ModelCreator();
			
		} else { //make sure the params are correct if it is old
			Parameters params = RunEnvironment.getInstance().getParameters();
			
			if (!fileAgents.equals(params.getString("fileAgents"))) {
				ModelCreator.context = null; //remove the context if the users file has changed
			}
			else if (fileAgents.equals("") && model.getnAgents() != params.getInteger("nAgents")) {
				ModelCreator.context = null; //remove the context if the user number has changed (ER does not accept adding)
			}
			else if (!netType.equals(params.getString("networkType"))) {
				ModelCreator.context = null; //remove the context if the netType has changed
			}
			else if (netType.equals(params.getString("networkType")) && !netFile.equals(params.getString("fileNet"))) {
				ModelCreator.context = null; //remove the context if the netType is the same but the netFile has changed
			}
			else if (modelType != params.getInteger("modelType") ) {
				ModelCreator.context = null; //remove the context if the model has changed (only 1-2 and 3-5 are compatible due to the agent type)
			}
		}
		
	    return instance;
	}
	
	/**
	 * Populates the context
	 * @param context
	 * @throws IncompatibleParameterException
	 */
	public void populateContext(Context<Object> context) throws IncompatibleParameterException {
		if (ModelCreator.context == null) //if null, set that one
			setNewContext(context);
		else { //if not null, previous execution
			//reset with each run!
			ThreadManager.resetThreadManager(); 
			//we reset the params
			Parameters params = RunEnvironment.getInstance().getParameters();
			
			//news 
			String fileNews = params.getString("fileNews");
			File fNews = new File(fileNews);
			HashMap<String, String[]> linesNews;
			if (fNews.exists() && !fNews.isDirectory()) {
				linesNews = readAllLines(fileNews); //load news
				
			} else throw new IncompatibleParameterException("The file of News does not exist or is a directory.");
			
			//reset model and send to users
			
			model = getModel(params, linesNews); //some params might change
			IndexedIterable<Object> stream_ag = ModelCreator.context.getObjects(Agent.class);
			for (Object el : stream_ag) {
				Agent ag = (Agent)el;
				ag.setModel(model);
				//reset stacks...
				ag.resetState();
			}
			
				
			//set new seed
			RandomHelper.setSeed(params.getInteger("randomSeed"));
			
			//we create each news (if more than one) and schedule them for release 
			releaseNews(linesNews);
		}
	}
	
	/**
	 * Main method to populate the context with users. Depending on the model selected, different users will be created.
	 * @param context
	 * @throws Exception 
	 */
	public void setNewContext(Context<Object> context) throws IncompatibleParameterException {
		
		ModelCreator.context = context;
		Parameters params = RunEnvironment.getInstance().getParameters();
		int numberAgents = 0;
		fileAgents = params.getString("fileAgents");
		String fileNews = params.getString("fileNews");
		modelType = params.getInteger("modelType");
		
		//we create each news (if more than one) and schedule them for release 
		File fNews = new File(fileNews);
		HashMap<String, String[]> linesNews;
		if (fNews.exists() && !fNews.isDirectory()) {
			linesNews = readAllLines(fileNews); //cargar news
			
		} else throw new IncompatibleParameterException("The file of News does not exist or is a directory.");

		//reset with each run!
		ThreadManager.resetThreadManager(); 
		
		model = getModel(params, linesNews);
		File fAgent = new File(fileAgents);
		//if fileAgents.equals("") -> create generic. Just need to give them labels and influence from a distribution 
		//else use the file
		boolean debug = params.getBoolean("debug");
		if (fileAgents.equals("")) { // create generic. Just need to give them labels and influence from a distribution
			Exponential dist = RandomHelper.createExponential(7); //Similar to the data

			numberAgents = params.getInteger("nAgents");
			for (int i = 1; i <= numberAgents; i++) { // [1,nAgents]
				Agent user;
				
				if (modelType == 1 || modelType == 2 ) {
					user = new UserSIR(String.valueOf(i)); //change based on model
				}else if (modelType >=3 && modelType <= 6) {
					user = new UserCoNVaI(String.valueOf(i)); //change based on model
				} else
					throw new IncompatibleParameterException("The model introduced could not be found.");
				
				double val = dist.nextDouble() / 7.0; //value divided by 7 to keep roughly [0,1]
				while (val <= 0 || val > 1) {
					val = dist.nextDouble();
				}
				user.setInfluence(val);
				user.setModel(model);
				user.setDebug(debug);
				context.add(user);
			}
			
		} else {
			if(fAgent.exists() && !fAgent.isDirectory()) {
				HashMap<String, String[]> lines = readAllLines(fileAgents); //cargar infls based on labels
				numberAgents = lines.size();
				model.setnAgents(numberAgents);
				for (String label : lines.keySet()) {
					if (lines.get(label).length != 2) throw new IncompatibleParameterException("Agents file does not comply with format: label, influence");
					Agent user;
					
					if (modelType == 1 || modelType == 2 ) {
						user = new UserSIR(label); //change based on model
					}else if (modelType >=3 && modelType <= 6) {
						user = new UserCoNVaI(label); //change based on model
					} else
						throw new IncompatibleParameterException("The model introduced could not be found.");
						
					user.setInfluence(Double.valueOf(lines.get(label)[1]));
					user.setModel(model);
					user.setDebug(debug);
					context.add(user);
				}
				if (context.size() != numberAgents) throw new IncompatibleParameterException("Agent IDs must be unique in the Agents file. Agents in context: "+context.size()+"; in file: " + numberAgents);
				
			} else throw new IncompatibleParameterException("The file of Agents does not exist or is a directory.");
		}
		
		
		//generate network
		generateNetwork(params, context, numberAgents);
		
		
		RandomHelper.setSeed(params.getInteger("randomSeed"));
		
		//release the news
		releaseNews(linesNews);
	}
	
	/**
	 * Creates the Spreading Model (1-6), by giving it the initial probabilities.
	 * @param params parameters of the configuration
	 * @return model created
	 * @throws IncompatibleParameterException
	 */
	public Model getModel(Parameters params, HashMap<String, String[]> linesNews) throws IncompatibleParameterException {
		
		//general values
		double probChange = params.getDouble("probChange"); //probChange
		double probInfect = params.getDouble("probInfect"); //probInfect
		double probDebunk = params.getDouble("probDebunk"); //probDebunk
		int randomSeed = params.getInteger("randomSeed");
		boolean useReply = params.getBoolean("useReply");
		double probRead = params.getDouble("probRead"); //probRead
		double probReply = params.getDouble("probReply"); //probReply
		double probOpinion = params.getDouble("probOpinion"); //probOpinion
		double noveltyFactor = params.getDouble("noveltyFactor"); //noveltyFactor
		double engagement = params.getDouble("engagement"); //noveltyFactor
		//specific for model 2 onwards
		double probInfl = params.getDouble("probInfl"); //probInfluence
		double k = params.getDouble("k"); //k
		
		HashMap<String,List<Double>> probsInteract = null;
		HashMap<String,Double> probsReply = new HashMap<String,Double>();
		for (String newsID : linesNews.keySet()) {
			probsReply.put(newsID,Double.valueOf(linesNews.get(newsID)[4]));
			if (linesNews.get(newsID).length < 6) continue; //no diffusion (index 5)
			List<String> diffusionList = Arrays.asList(linesNews.get(newsID)[5].split(";"));
			List<Double> diff = diffusionList.stream().map(c -> Double.valueOf(c)).collect(Collectors.toList());
			diff.add(0, 1.0);
			System.out.println(diff.toString());
			if (probsInteract == null) probsInteract = new HashMap<String,List<Double>>();
			probsInteract.put(newsID, diff);
		}
		if (useReply) {
			probsReply = null; //if use reply
			probsInteract = null;
			k = 0;
		}
		switch(params.getInteger("modelType")) {
		
		case 1: //SIR

			//NEW ONES - FOR THE MODEL
			return new ModelSIR(probChange, probInfect, probDebunk, randomSeed);
			
		case 2: //SIR with influencers and engagement

			//NEW ONES - FOR THE MODEL
			return new ModelSIRExtended(probChange, probInfect, probDebunk, randomSeed, engagement, probInfl);
		case 3: //Adding user only

			noveltyFactor = 0;
			k = 0;
			probsInteract = null;
			probsReply = null;
			return new ModelCoNVaI(probRead, probReply, probsReply, probOpinion, probChange, probInfect, probDebunk, probInfl, noveltyFactor, randomSeed, k, probsInteract);
			
		case 4: //Adding news only
			probInfl = 0;
			//probReply dependent on newsdif
			return new ModelCoNVaI(probRead, probReply, probsReply, probOpinion, probChange, probInfect, probDebunk, probInfl, noveltyFactor, randomSeed, k, probsInteract);
				
		case 5: // Adding news and users
			return new ModelCoNVaI(probRead, probReply, probsReply, probOpinion, probChange, probInfect, probDebunk, probInfl, noveltyFactor, randomSeed, k, probsInteract);
			
		case 6: // Adding news and users with confidence within nets

			//NEW ONES - FOR THE MODEL
			double confidence = params.getDouble("confidence");
			return new ModelCoNVaIConfidence(probRead, probReply, probsReply, probOpinion, probChange, probInfect, probDebunk, probInfl, noveltyFactor, randomSeed, k, probsInteract, confidence);
			
		default:
			throw new IncompatibleParameterException("The model introduced could not be found.");
		}
		
	}
	
	/**
	 * Creates the network based on the parameters. Currently implemented Barabasi-Albert and Erdos-Renyi.
	 * @param params  initial parameters
	 * @param context
	 * @param numberAgents total number of agents
	 * @throws Exception 
	 */
	public Network<Object> generateNetwork(Parameters params, Context<Object> context, int numberAgents) throws IncompatibleParameterException {
		
		netType = params.getString("networkType");
		Network<Object> net = null;
		netFile = params.getString("fileNet");
	
		if (netType.equals("File-Load") && !fileAgents.equals("")) { //file loaded and not random Agents
			System.out.println("Model: "+model.toString());
			
			File f = new File(netFile);
			
			
			if(f.exists() && !f.isDirectory()) { 

				System.out.println("File net: "+f.toString());
				net = new ContextJungNetwork<Object>(new DirectedJungNetwork<Object>("network", new DefaultEdgeCreator<Object>()), context);
				context.addProjection(net);
				
				NetworkLoader netLoad = new NetworkLoader(context, netFile);
				
				//netBuilder.load(fileLocation, NetworkFileFormat.SPARSE, nodeCreator); // generic node creator

				netLoad.createNetwork(net);

			} else throw new IncompatibleParameterException("Network file does not exist or is a folder.");
			

		} else if (!netType.equals("File-Load")){ //if we don't have file load, real net
			boolean directed = true; //hard-coded for now, it's realistic
			NetworkBuilder<Object> netBuilder = null;
			netBuilder = new NetworkBuilder<Object>("network", context, directed);
			
			AbstractGenerator<Object> selGen = null;
			
			if(netType.equals("Barabasi-Albert")) {
				selGen = new BarabasiAlbertGenerator(context,params.getInteger("networkSeed"), params.getInteger("linksPerNode"), params.getInteger("initialNodes"), numberAgents, Agent.class, directed);

			} else if(netType.equals("Erdos-Renyi")){
				selGen = new ErdosRenyiGenerator(context, params.getInteger("networkSeed"), numberAgents, params.getDouble("probNetwork"), Agent.class, directed);

			} else throw new IncompatibleParameterException("The network selected doesn't exist");
			
			netBuilder.setGenerator(selGen); //set generator based on user choice
			net = netBuilder.buildNetwork();
		}
		else throw new IncompatibleParameterException("The fileAgents has to provide the users to load an existing network, otherwise a generated network must be selected");

			
		return net;
	}
	
	/**
	 * Reads info from input params
	 * @param filePath
	 * @return
	 * @throws IncompatibleParameterException
	 */
	public HashMap<String, String[]> readAllLines(String filePath) throws IncompatibleParameterException {
		Path path = Path.of(filePath);
		System.out.println("Loading:" + path);
		HashMap<String, String[]> lines= new HashMap<String, String[]> ();
	    try (Reader reader = Files.newBufferedReader(path)) {
	    	
	        try (CSVReader csvReader = new CSVReader(reader)) {
	        	String[] line;
	        	
	        	line = csvReader.readNext(); //get rid of the first one
	        	
	            while ((line = csvReader.readNext()) != null) {
	            	if (line.length ==0) throw new IncompatibleParameterException("Error while reading the file "+filePath+", it does not comply with CSV format.");
	            	String label = line[0];
	            	
	            	lines.put(label, line);
	            }
	        }
	    } catch (IOException e) {
			throw new IncompatibleParameterException("Error while reading the file "+filePath+", with exception: "+e.getMessage());
		}
	    return lines;
	}
	
	/**
	 * Checks for the execution to be finished
	 * @throws IOException 
	 * 
	 */
	public void runExecution() throws IOException {
		
		Stream<Object> s = context.getObjectsAsStream(Agent.class);
		
		//extract those with messages
		List<Object> list = s.filter(c -> !((Agent)c).hasNoMessages()).collect(Collectors.toList());
		
		//over 1000 ticks, no msgs, no prob to reply (M1 or others)
		if ((RunEnvironment.getInstance().getCurrentSchedule().getTickCount() - last_tick) > 1000 || list.isEmpty() || //no msgs
				(model instanceof ModelCoNVaI && ((ModelCoNVaI)model).getProbReply(last_tick, RunEnvironment.getInstance().getCurrentSchedule().getTickCount(), last_news) < 0.000001) || //model 3+, and impossibility
				(model instanceof ModelSIRExtended && ((ModelSIRExtended)model).getEngagement()< 0.000001) ||  //model 2 and impossibility
				(modelType == 1 &&  context.getObjectsAsStream(Agent.class).filter(c -> ((Agent)c).hasChanged()).collect(Collectors.toList()).isEmpty())) { //model 1 and no changes in X min
			
			Parameters params = RunEnvironment.getInstance().getParameters();
			String folder = params.getString("outputFolder");
			RunEnvironment.getInstance().endRun(); // if users have no more threads, stop exec
			
			int batch = RunState.getInstance().getRunInfo().getBatchNumber();
			int runNumber = RunState.getInstance().getRunInfo().getRunNumber();
			boolean debug = params.getBoolean("debug");
			
			ThreadManager.getInstance().exportThreads(folder, batch, runNumber, debug);
		}

		
	}
	
	/**
	 * Releases the news and starts the spread
	 * @param newsParams
	 */
	public void releaseNews(HashMap<String, String[]> newsParams) throws IncompatibleParameterException{
		//Specify that the action should start at tick 1 and execute every other tick
		last_tick = 0;
		for (String newsID : newsParams.keySet()) {
			if (newsParams.get(newsID).length < 5) throw new IncompatibleParameterException("News file does not comply with format: newsID, labelUser, tick, novelty, probInflUser");
			
			String label = newsParams.get(newsID)[1]; //0 newsID, 1 label of poster
			List<Object> agent;
			
			if (fileAgents.equals("")) { //users were created with different labels, so we don't use them
				agent = context.getRandomObjectsAsStream(Agent.class, 1).collect(Collectors.toList());
			}else {//find the correct one
				agent = context.getObjectsAsStream(Agent.class).filter(c -> ((Agent)c).getLabel().equals(label)).collect(Collectors.toList()); //only 1
				if (agent.size() != 1) throw new IncompatibleParameterException("Errow while scheduling the news. Found repeated or nonexistent users. Agents that spread the news: "+agent.size());
				
			}
			
			double tick = Integer.valueOf(newsParams.get(newsID)[2]); //0 newsID, 1 label, 2 tick
			if (tick > last_tick) {

				last_tick = tick;
				last_news = newsID;
			}
			
			
			String[] newsChars = Arrays.copyOfRange(newsParams.get(newsID), 3, newsParams.get(newsID).length);
			NewsPiece news = new NewsPiece(newsID, newsChars); //create news from news info we have

			ScheduleParameters sched = ScheduleParameters.createOneTime(tick);
			//Schedule my agent to execute the move method given the specified schedule parameters.
			RunEnvironment.getInstance().getCurrentSchedule().schedule(sched, agent.get(0), "postMessage", new Thread(news, tick), State.INFECTED);
			
			
		}
		//order of addition
		for (Object obj : context.getObjectsAsStream(Agent.class).sorted().collect(Collectors.toList())) {
			RunEnvironment.getInstance().getCurrentSchedule().schedule(ScheduleParameters.createRepeating(1, 1), obj, "step");
		}
		
		
		ScheduleParameters schedule_end = ScheduleParameters.createRepeating(last_tick + 5, 1, ScheduleParameters.LAST_PRIORITY);
		RunEnvironment.getInstance().getCurrentSchedule().schedule(schedule_end, this, "runExecution");
		
	}
	
	/**
	 * Returns context
	 * @return
	 */
	public Context<Object> getContext() {
		// TODO Auto-generated method stub
		return context;
	}

}
