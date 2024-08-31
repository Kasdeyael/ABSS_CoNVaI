package model;

public class Model {
	
	
	private int randomSeed;
	private int nAgents;
	
	/**
	 * Constructor for Model.
	 * @param randomSeed random seed
	 */
	public Model (int randomSeed){

		this.randomSeed = randomSeed;
	}
	
	/**
	 * Check if a probability is in the range of [0,1].
	 * @param prob the probability to check
	 * @return true if it's in range, false if it's not.
	 */
	public boolean checkProbability(double prob) {
		
		if(prob > 1.0 || prob < 0.0) return false;
		return true;
	}
	
	
	/**
	 * Get random seed.
	 * @return randomSeed
	 */
	public int getRandomSeed() {
		return randomSeed;
	}
	
	/**
	 * Set random seed
	 * @param randomSeed
	 */
	public void setRandomSeed(int randomSeed) {
		this.randomSeed = randomSeed;
	}
	
	/**
	 * Get nAgents
	 * @return nAgents
	 */
	public int getnAgents() {
		return nAgents;
	}
	
	/**
	 * Set nAgents
	 * @param nAgents
	 */
	public void setnAgents(int nAgents) {
		this.nAgents = nAgents;
	}
	
}
