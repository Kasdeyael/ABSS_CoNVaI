package dataset;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MsgSpread {
	
	private double probInfl;
	private double novelty;
	private String name;
	private String author;
	private List<DiffusionString> diffusion;
	private List<Diffusion> states;
	
	
	/**
	 * Empty Constructor
	 */
	public MsgSpread() {
		diffusion = null;
		states = null;
	}
	
	/**
	 * Constructor with MsgSpread data
	 * @param name
	 * @param author
	 * @param probInfl
	 * @param novelty
	 * @param diffusion
	 * @param states
	 */
	public MsgSpread(String name, String author, double probInfl, double novelty, List<DiffusionString> diffusion, List<Diffusion> states) {
		this.setProbInfl(probInfl);
		this.setName(name);
		this.setAuthor(author);
		this.setNovelty(novelty);
		this.setDiffusion(diffusion);
		this.setStates(states);
	}

	/**
	 * Gets probInfl
	 * @return
	 */
	public double getProbInfl() {
		return probInfl;
	}
	
	
	@JsonProperty("probInfl")
	public void setProbInfl(double probInfl) {
		this.probInfl = probInfl;
	}
	
	/**
	 * Gets novelty
	 * @return
	 */
	public double getNovelty() {
		return novelty;
	}

	@JsonProperty("novelty")
	public void setNovelty(double novelty) {
		this.novelty = novelty;
	}
	
	/**
	 * Gets diffusion
	 * @return
	 */
	public List<DiffusionString> getDiffusion() {
		return diffusion;
	}
	
	/**
	 * Sets diffusion
	 * @param diffusion
	 */
	public void setDiffusion(List<DiffusionString> diffusion) {
		this.diffusion = diffusion;
	}
	
	/**
	 * Gets states
	 * @return
	 */
	public List<Diffusion> getStates() {
		return states;
	}
	
	/**
	 * Sets states list
	 * @param states
	 */
	public void setStates(List<Diffusion> states) {
		this.states = states;
	}
	
	/**
	 * Name of the spread
	 * @return
	 */
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets author
	 * @return
	 */
	public String getAuthor() {
		return author;
	}

	@JsonProperty("author")
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String toString() {
		return "probInfl: "+ probInfl +", novelty: " +novelty + ", name: "+name+", author: "+ author +", spread[0]: " + diffusion.get(0).toString();
	}
}
