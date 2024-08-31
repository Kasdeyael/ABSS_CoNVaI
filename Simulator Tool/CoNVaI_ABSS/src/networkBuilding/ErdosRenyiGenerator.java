package networkBuilding;

import exception.IncompatibleParameterException;
import repast.simphony.context.Context;
import repast.simphony.context.space.graph.AbstractGenerator;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.util.collections.IndexedIterable;

public class ErdosRenyiGenerator extends AbstractGenerator<Object>{

	
	private int networkSeed;
	private int totalNodes;
	private double probability;
	private boolean isDirected;
	private Context<Object> ctx;
	private Class<?> classObj;
	/**
	 * Constructor for ErdosGenerator.
	 * @param ctx context where all agents are.
	 * @param networkSeed random seed for the network.
	 * @param totalNodes total nodes in network
	 * @param probability probability to connect each two nodes
	 * @param isDirected if the network is directed or not
	 */
	public ErdosRenyiGenerator(Context <Object> ctx, int networkSeed, int totalNodes, double probability, Class<?> classObj, boolean isDirected) throws IncompatibleParameterException{
		if(probability > 1.0 || probability < 0.0) throw new IncompatibleParameterException("Probability for the network must be in range [0,1]");
		if(totalNodes < 1) throw new IncompatibleParameterException("The total nodes in the network cannot be negative or zero.");
		this.ctx = ctx;
		this.setProbability(probability);
		this.setNetworkSeed(networkSeed);
		this.setTotalNodes(totalNodes);
		this.setIsDirected(isDirected);
		this.classObj = classObj;
		
	}
	
	/**
	 * Sets directed/indirected net
	 * @param isDirected
	 */
	public void setIsDirected(boolean isDirected) {
		this.isDirected = isDirected;
		
	}
	
	/**
	 * Gets directed/indirected net
	 */
	public boolean getIsDirected() {
		return isDirected;
		
	}

	/**
	 * Returns the seed used to create the network.
	 * @return network seed
	 */
	public int getNetworkSeed() {
		return networkSeed;
	}
	
	/**
	 * Sets the seed to create the network.
	 * @param networkSeed
	 */
	public void setNetworkSeed(int networkSeed) {
		this.networkSeed = networkSeed;
	}
	
	/**
	 * Returns the number of nodes used to create the network.
	 * @return number of total nodes
	 */
	public int getTotalNodes() {
		return totalNodes;
	}
	
	/**
	 * Sets the number of nodes used to create the network.
	 * @param totalNodes
	 */
	public void setTotalNodes(int totalNodes) {
		this.totalNodes = totalNodes;
	}
	
	/**
	 * Returns the probability used to connect nodes in the network.
	 * @return probability 
	 */
	public double getProbability() {
		return probability;
	}
	
	/**
	 * Sets the probability used to connect nodes in the network.
	 * @param probability
	 */
	public void setProbability(double probability) {
		this.probability = probability;
	}
	
	@Override
	public Network<Object> createNetwork(Network<Object> network) {
		RandomHelper.setSeed(networkSeed);
		
		IndexedIterable<Object> indCo = ctx.getObjects(classObj);
		
		for(int i=0; i<(totalNodes - 1); i++){ //de 0 a total -1
			
			Object ob1 = indCo.get(i); 
			for(int j = i + 1; j < totalNodes; j++){ //de i a total
				
				Object ob2 = indCo.get(j);
				if(probability > RandomHelper.nextDoubleFromTo(0, 1)) {
					network.addEdge(ob1, ob2);
					if (isDirected /*&& probability > RandomHelper.nextDoubleFromTo(0, 1)*/){ //if directed
						network.addEdge(ob2, ob1);
					}
				}
			}
			
		}
		return network;
	}
	
}
