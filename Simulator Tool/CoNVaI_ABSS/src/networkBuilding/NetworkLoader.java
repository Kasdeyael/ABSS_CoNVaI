package networkBuilding;

import repast.simphony.context.Context;
import au.com.bytecode.opencsv.CSVReader;
import exception.IncompatibleParameterException;
import repast.simphony.space.graph.Network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import agent.Agent;



public class NetworkLoader {

	  private Context<Object> context;
	  private String fileName;
	  
	  /**
	   * Constructor
	   * @param context
	   * @param fileName
	   */
	  public NetworkLoader(Context<Object> context, String fileName) {
	    this.context = context;
	    this.fileName = fileName;
	  }
	  
		

	  /**
	   * Creates edges using the nodes in the specified network. The
	   * semantics of edge creation is determined by implementing classes.
	   *
	   * @param network a network containing nodes
	   * @return the generated network
	 * @throws IOException 
	   */
	  public Network<Object> createNetwork(Network<Object> network) throws IncompatibleParameterException {

		try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileName))) {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
				try (CSVReader csvReader = new CSVReader(reader)) {
					String[] line;
		        	line = csvReader.readNext(); //get rid of the first one (header)
		            while ((line = csvReader.readNext()) != null) {
		            	List<String> nodeList = Arrays.asList(line);
		            	//System.out.println(nodeList.toString());
		            	if (line.length < 1) throw new IncompatibleParameterException("Network file does not comply with format: source, target(s) separated by commas");
		            	
		            	
		            	//System.out.println(source_s + "->" + target_s);
		            	//System.out.println("Finding source! List was: "+line);
		            	
		            	List<Object> nodes = context.getObjectsAsStream(Agent.class).filter(c -> nodeList.stream().anyMatch(node -> node.equals(((Agent)c).getLabel()))).collect(Collectors.toList());
		            	//System.out.println(nodes.toString());
		            	if (nodes.size()== 0) throw new IncompatibleParameterException("Errow while connecting the network. Found nonexistent users. Filtered: "+nodes.size());
		            	Agent source = null;
		            	
		            	for (int i=0; i< nodes.size(); i++) {
		            		source = (Agent)nodes.get(i);
		            		if (source.getLabel().equals(line[0])) { //source is the first
		            			break;
		            		}
		            	}
		            	nodes.remove(source);
		            	for (Object node : nodes) {//adding edges
		            		network.addEdge(source, node, 1);
		            	}
		            	
		            	
		            }
			    }
			} catch (FileNotFoundException ex) {
				throw new IncompatibleParameterException("File was not found for the network: "+ex.getMessage());
			} catch (IOException ex) {
				throw new IncompatibleParameterException("I/O Error for the network: "+ex.getMessage());
			} 
		} catch (IOException ex) {
			throw new IncompatibleParameterException("I/O Error for the buffer in the network: "+ex.getMessage());
		}
		
		
	    


	    return network;
	  }

	  
	}