package dataset;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Diffusion {
	
	private int tick;
	private int infected;
	private int debunker;
	private int neutral;
	
	/**
	 * Empty constructor
	 */
	public Diffusion() {
		
	}
	
	/**
	 * Constructor for the infected/debunker/neutral users at X tick
	 * @param tick
	 * @param infected
	 * @param debunker
	 */
	public Diffusion(int tick, int infected, int debunker) {
		this.setTick(tick);
		this.setInfected(infected);
		this.setDebunker(debunker);
	}
	
	/**
	 * Gets infected at this tick
	 * @return infected
	 */
	public int getInfected() {
		return infected;
	}

	@JsonProperty("infected")
	public void setInfected(int infected) {
		this.infected = infected;
	}
	
	/**
	 * Gets debunkers at this tick
	 * @return debunkers
	 */ 
	public int getDebunker() {
		
		return debunker;
		
	}

	@JsonProperty("debunker")
	public void setDebunker(int debunker) {
		this.debunker = debunker;
	}
	
	/**
	 * Gets neutrals at this tick
	 * @return neutrals
	 */ 
	public int getNeutral() {
		
		return neutral;
		
	}

	@JsonProperty("neutral")
	public void setNeutral(int neutral) {
		this.neutral = neutral;
	}
	
	/**
	 * Returns this tick
	 * @return
	 */
	public int getTick() {
		return tick;
	}

	@JsonProperty("tick")
	public void setTick(int tick) {
		this.tick = tick;
	}
	
	public String toString() {
		return "tick: "+tick+", spreader: "+infected+", debunker: "+debunker;
	}
}
