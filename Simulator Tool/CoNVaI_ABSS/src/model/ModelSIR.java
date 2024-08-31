package model;

import exception.IncompatibleParameterException;

public class ModelSIR extends Model{
	
	private double probChange;
	private double probInfect;
	private double probDebunk;
	
	/**
	 * Constructor for ModelM1.
	 * @param probAcceptDeny probability to accept the deny (probChange)
	 * @param probInfect probability to infect
	 * @param probMakeDenier probability to make a denier (probDebunk)
	 * @param randomSeed random seed
	 */
	public ModelSIR(double probAcceptDeny, double probInfect, double probMakeDenier, int randomSeed) throws IncompatibleParameterException{
		super(randomSeed);
		//System.out.println("probAcceptDeny: "+probAcceptDeny+", probInfect: "+probInfect+", probMakeDenier "+probMakeDenier);
		if(!checkProbability(probAcceptDeny))  //limits would be [0,1]
			throw new IncompatibleParameterException("The probability probAcceptDeny has to be in the range [0,1]");
		if(!checkProbability(probInfect))  //limits would be [0,1]
			throw new IncompatibleParameterException("The probability probInfect has to be in the range [0,1]");
		if(!checkProbability(probMakeDenier))  //limits would be [0,1]
			throw new IncompatibleParameterException("The probability probMakeDenier has to be in the range [0,1]");
		this.probChange = probAcceptDeny;
		this.probInfect = probInfect;
		this.probDebunk = probMakeDenier;
	}
	
	/**
	 * Returns the probability to accept the deny (probChange).
	 * @return probAcceptDeny
	 */
	public double getProbChange() {
		return probChange;
	}

	/**
	 * Sets the probability to accept the deny.
	 * @param probAcceptDeny
	 */
	public void setProbChange(double probAcceptDeny) {
		this.probChange = probAcceptDeny;
	}
	
	/**
	 * Returns the probability of getting infected.
	 * @return probInfect
	 */
	public double getProbInfect() {
		return probInfect;
	}
	
	
	/**
	 * Sets the probability of getting infected.
	 * @param probInfect
	 */
	public void setProbInfect(double probInfect) {
		this.probInfect = probInfect;
	}
	
	/**
	 * Returns the probability of making a denier (probDebunk).
	 * @return probMakeDenier
	 */
	public double getProbDebunk() {
		return probDebunk;
	}
	
	/**
	 * Sets the probability of making a denier (probDebunk).
	 * @param probMakeDenier
	 */
	public void setProbDebunk(double probMakeDenier) {
		this.probDebunk = probMakeDenier;
	}
	

	
}
