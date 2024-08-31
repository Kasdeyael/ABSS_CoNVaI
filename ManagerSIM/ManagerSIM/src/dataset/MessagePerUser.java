package dataset;

public class MessagePerUser {
	
	private String name;
	private int debunker;
	private int infected;
	
	/**
	 * Constructor for number of messages a user has sent in a specific time frame (tick).
	 */
	public MessagePerUser() {
		debunker = 0;
		infected = 0;
	}
	
	/**
	 * Gets debunker msgs per user
	 * @return debunker msgs
	 */
	public int getDebunker() {
		return debunker;
	}
	
	/**
	 * Adds debunker msg at this tick
	 * @param debunker msgs
	 */
	public void addDebunker(int debunker) {
		this.debunker += debunker;
	}
	
	/**
	 * Adds infected msg at this tick
	 * @param infected msgs
	 */
	public void addInfected(int infected) {
		this.infected += infected;
	}
	
	/**
	 * Gets infected msgs per user
	 * @return infected msgs
	 */
	public int getInfected() {
		return infected;
	}
	
	/**
	 * Returns name user
	 * @return name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets name user
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	
}
