package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exception.IncompatibleParameterException;

public class ModelCoNVaIConfidence extends ModelCoNVaI{


	private double confidence;
	

	/**
	 * Constructor model 6
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
	 * @param confidence
	 * @throws IncompatibleParameterException
	 */
	public ModelCoNVaIConfidence(double probRead, double probReply, Map<String,Double> probsReply, double probOpinion, double probChange,
			double probInfect, double probDebunk,
			double probInfluence, double noveltyFactor, int randomSeed, double k, HashMap<String, List<Double>> probsInteract, double confidence) throws IncompatibleParameterException {
		super(probRead, probReply, probsReply, probOpinion, probChange, probInfect, probDebunk, probInfluence, noveltyFactor,
				randomSeed, k, probsInteract);
		if(!checkProbability(confidence))
			throw new IncompatibleParameterException("The confidence has to be in the range [0,1]");


		setConfidence(confidence);
	}
	
	/**
	 * Gets confidence factor
	 * @return
	 */
	public double getConfidence() {
		return confidence;
	}

	/**
	 * Sets confidence factor
	 * @param confidence
	 */
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
	
	@Override
	public String toString() {
		return "probRead: "+this.getProbRead()+", probReply: "+this.getProbReply()+", probInfect: "
				+ this.getProbInfect() +", probDebunk: " + this.getProbDebunk() +", probChange: " + this.getProbChange()
				+ ", probOpinion: " + this.getProbOpinion() + ", novelty: "+this.getNoveltyFactor()+", probInfluence: "
				+ this.getProbInfluence() + ", confidence: "+confidence;
	}


}
