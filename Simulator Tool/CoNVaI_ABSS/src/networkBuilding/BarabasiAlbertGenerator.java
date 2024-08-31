package networkBuilding;

import exception.IncompatibleParameterException;
import repast.simphony.context.Context;
import repast.simphony.context.space.graph.AbstractGenerator;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.util.collections.IndexedIterable;

public class BarabasiAlbertGenerator extends AbstractGenerator<Object>{

	private int networkSeed;
	private int linksPerNode;
	private int initialNodes;
	private boolean isDirected;
	private Context<Object> ctx;
	private Class<?> classObj;
	
	/**
	 * Constructor for BarabasiAlbertGenerator.
	 * @param ctx context where all agents are.
	 * @param networkSeed random seed for the network.
	 * @param linksPerNode maximum number of links per node.
	 * @param initialNodes number of initial nodes
	 * @param totalNodes total nodes in network
	 * @param isDirected if the network is directed or not
	 */
	public BarabasiAlbertGenerator(Context <Object> ctx, int networkSeed, int linksPerNode, int initialNodes, int totalNodes, Class<?> classObj, boolean isDirected) throws IncompatibleParameterException{

		if(linksPerNode < 1 || initialNodes < 1 || totalNodes < 1) throw new IncompatibleParameterException("No network parameter can be negative or zero.");
		if(totalNodes < initialNodes)  throw new IncompatibleParameterException("The number of initial nodes can't be higher than the total nodes.");
		if(linksPerNode > initialNodes) throw new IncompatibleParameterException("Links per node can't be more than the initial nodes in the network.");
		if(initialNodes < 2) throw new IncompatibleParameterException("The initial network has to be fully connected, it needs more than one initial node.");
		this.setNetworkSeed(networkSeed);
		this.setLinksPerNode(linksPerNode);
		this.setInitialNodes(initialNodes);
		this.ctx = ctx;
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
	 * Returns the links to add per iteration with each node.
	 * @return links per node
	 */
	public int getLinksPerNode() {
		return linksPerNode;
	}
	
	/**
	 * Sets the links per node.
	 * @param number of links per node
	 */
	public void setLinksPerNode(int linksPerNode) {
		this.linksPerNode = linksPerNode;
	}
	
	/**
	 * Returns the number of nodes used to create the initial network.
	 * @return number of initial nodes
	 */
	public int getInitialNodes() {
		return initialNodes;
	}
	
	/**
	 * Sets the number of nodes used to create the initial network.
	 * @param initialNodes
	 */
	public void setInitialNodes(int initialNodes) {
		this.initialNodes = initialNodes;
	}
	
	@Override
	public Network<Object> createNetwork(Network<Object> network) {
		
		RandomHelper.setSeed(networkSeed);
		
		IndexedIterable<Object> indCo = ctx.getObjects(classObj);
		//first, make the net fully connected, using the initial nodes
		for(int i=0; i < initialNodes; i++) {
			for(int j = 0; j < initialNodes; j++) {
				if(indCo.get(i) != indCo.get(j) && !network.isAdjacent(indCo.get(i), indCo.get(j))) { //not same and not adjacent
					network.addEdge(indCo.get(i), indCo.get(j));
					if (isDirected) network.addEdge(indCo.get(j), indCo.get(i));
				}
			}
		}
		
		//now we connect the rest of the nodes
		for(int i=initialNodes; i < indCo.size(); i++) {
			int currentDegree = 0; //count for current degree of the node we're adding
			
			int linksToAdd = RandomHelper.nextIntFromTo(1,linksPerNode); 

			while (currentDegree < linksToAdd) {//we make as many connections as the selected ones
				
				Object obj = indCo.get(RandomHelper.nextIntFromTo(0, i-1)); //choose one randomly from all the ones already in net [0,added)
				
				while(obj.equals(indCo.get(i)) || network.isAdjacent(indCo.get(i), obj) ){ //make sure it's not the same and not adjacent already (regardless direction)
					obj = indCo.get(RandomHelper.nextIntFromTo(0, i-1));
				}
				
				double b = (double)network.getDegree(obj) / (double)network.getDegree(); //get degree
				double chance = RandomHelper.nextDoubleFromTo(0, 1); //compute random
				if(b > chance) {
					network.addEdge(obj, indCo.get(i));
					currentDegree++;
					if (isDirected) {
						network.addEdge(indCo.get(i), obj);
						currentDegree++;
					}
				}

				
			}
			
		}
		
		return network;
	}

}
