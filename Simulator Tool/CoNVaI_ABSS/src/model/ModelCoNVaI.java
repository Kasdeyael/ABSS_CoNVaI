package model;


import java.util.Map;
import java.util.HashMap;
import java.util.List;

import exception.IncompatibleParameterException;

public class ModelCoNVaI extends ModelSIR{
	
	
	private double probInfluence;
	private double probRead;
	private double probReply;
	private double probOpinion;
	private double noveltyFactor;
	private double k;
	private Map<String,List<Double>> probsInteract; 
	private Map<String,Double> probsReply; 
	
	/**
	 * Constructor for model 5
	 * @param probRead
	 * @param probReply
	 * @param probsReply
	 * @param probOpinion
	 * @param probChange
	 * @param probInfect
	 * @param probDebunk
	 * @param probInfluence
	 * @param noveltyFactor
	 * @param randomSeed
	 * @param k
	 * @param probsInteract
	 * @throws IncompatibleParameterException
	 */
	public ModelCoNVaI(double probRead, double probReply, Map<String,Double> probsReply, double probOpinion, double probChange,
			double probInfect, double probDebunk,
			double probInfluence, double noveltyFactor, int randomSeed, double k, HashMap<String, List<Double>> probsInteract) throws IncompatibleParameterException {
		// TODO Auto-generated constructor stub
		super(probChange,probInfect,probDebunk,randomSeed);
		if(!checkProbability(probRead))
			throw new IncompatibleParameterException("The probability probRead has to be in the range [0,1]");
		if(!checkProbability(probReply))
			throw new IncompatibleParameterException("The probability probReply has to be in the range [0,1]");
		if(!checkProbability(probOpinion))
			throw new IncompatibleParameterException("The probability probOpinion has to be in the range [0,1]");
		if(!checkProbability(probInfluence))
			throw new IncompatibleParameterException("The probability probInfluence has to be in the range [0,1]");
		if(!checkProbability(noveltyFactor))
			throw new IncompatibleParameterException("The probability noveltyFactor has to be in the range [0,1]");
		this.setProbRead(probRead);
		this.setProbReply(probReply); // we have dist based on news partial info
		this.setProbsReply(probsReply);
		this.setProbOpinion(probOpinion);
		this.setNoveltyFactor(noveltyFactor);
		this.setProbInfluence(probInfluence);
		this.setProbsInteract(probsInteract);
		this.setK(k); 
		//System.out.println("k: "+k+", novelty: "+noveltyFactor);
	}
	
	/**
	 * Sets probsReply
	 * @param probsReply
	 */
	private void setProbsReply(Map<String, Double> probsReply) {
		this.probsReply = probsReply;
		
	}
	/**
	 * Gets novelty factor
	 * @return
	 */
	public double getNoveltyFactor() {
		return noveltyFactor;
	}
	/**
	 * Sets novelty factor
	 * @param noveltyFactor
	 */
	public void setNoveltyFactor(double noveltyFactor) {
		this.noveltyFactor = noveltyFactor;
	}
	
	/**
	 * Gets probInfl
	 * @return
	 */
	public double getProbInfluence() {
		return probInfluence;
	}
	
	/**
	 * Sets probInfl
	 * @param probInfluence
	 */
	public void setProbInfluence(double probInfluence) {
		this.probInfluence = probInfluence;
	}

	/**
	 * Returns the value of the engagement(probReply), following the timeline (parameter), or a default exponential decline (with an init value if available):
	 * 	engagement(t) = engagement_i \cdot \exp(- \frac{1}{8} \cdot (current_time-start_time)) where time is computed in sets of 4 ticks (4 minutes).
	 * @param init_tck
	 * @param current_tck
	 * @param newsUID
	 * @return engagement based on current tick and init tick of the news.
	 */
	public double getProbReply(double init_tck, double current_tck, String newsUID){

		if (k != 0 && probsInteract != null && probsInteract.containsKey(newsUID)) { //k ! 0, probsInteract exists and has info, we use its diffusion
			List<Double> this_spread = probsInteract.get(newsUID);
			double real = current_tck - init_tck;
			double value = 0;
			if (real >= this_spread.size()) {
				value = this_spread.get(this_spread.size() - 1);
				
			} else {
				value = this_spread.get((int)real);
				
			}
			//System.out.println("Value: "+ (k * value > 1 ? 1 : k * value));
			return k * value > 1 ? 1 : k * value; //return last if we are over the max //return current (if under 1, else 1).
			
		}
		else { //use probReply generic or probsReply val from list
			
			double current = current_tck / 4;
			double start = init_tck / 4;
			
			if (probsReply != null && probsReply.containsKey(newsUID) && probsReply.get(newsUID) != 0) { //if probsReply is given, contains value and not 0
				//System.out.println("Value: "+ (probsReply.get(newsUID) * Math.exp(-(current-start)/8) > 1? 1 : probsReply.get(newsUID) * Math.exp(-(current-start)/8)));
				return probsReply.get(newsUID) * Math.exp(-(current-start)/8) > 1? 1 : probsReply.get(newsUID) * Math.exp(-(current-start)/8);
			}

			//System.out.println("Value: "+ probReply * Math.exp(-(current-start)/8));
			//System.out.println("Prob reply (decreases from "+probReply+"): "+probReply * Math.exp(-(current-start)/8));
			return probReply * Math.exp(-(current-start)/8);
		} 
	}
	
	
	/**
	 * Gets pobReply
	 * @return
	 */
	public double getProbReply() {
		
		return probReply;
	}
	
	/**
	 * Gets probRead
	 * @return
	 */
	public double getProbRead() {
		
		return probRead ;
	}

	/**
	 * Gets probRead
	 * @param counter
	 * @return
	 */
	public double getProbRead(int counter) {
		
		return probRead * 1 / counter;
	}
	

	/**
	 * Sets probRead
	 * @param probRead
	 */
	public void setProbRead(double probRead) {
		this.probRead = probRead;
	}

	/**
	 * Sets probReply
	 * @param probReply
	 */
	public void setProbReply(double probReply) {
		this.probReply = probReply;
	}

	/**
	 * Gets probOpinion
	 * @return
	 */
	public double getProbOpinion() {
		return probOpinion;
	}

	/**
	 * Sets probOpinion
	 * @param probOpinion
	 */
	public void setProbOpinion(double probOpinion) {
		this.probOpinion = probOpinion;
	}


	
	/**
	 * Returns k (multiplier of timeline). Default 1
	 * @return
	 */
	public double getK() {
		return k;
	}

	/**
	 * Sets k (multiplier of timeline). Default 1
	 * @param k
	 */
	public void setK(double k) {
		this.k = k;
	}

	/**
	 * Gets probsInteract
	 * @return
	 */
	public Map<String,List<Double>> getProbsInteract() {
		return probsInteract;
	}

	/**
	 * Sets probsInteract
	 * @param probsInteract
	 */
	public void setProbsInteract(Map<String,List<Double>> probsInteract) {
		this.probsInteract = probsInteract;
	}
	@Override
	public String toString() {
		return "probRead: "+this.getProbRead()+", probReply: "+this.getProbReply()+", probInfect: "
				+ this.getProbInfect() +", probDebunk: " + this.getProbDebunk() +", probChange: " + this.getProbChange()
				+ ", probOpinion: " + this.getProbOpinion() + ", novelty: "+this.getNoveltyFactor()+", probInfluence: "
				+ this.getProbInfluence();
	}


}
