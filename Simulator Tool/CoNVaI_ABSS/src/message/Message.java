package message;

import agent.Agent;

public class Message {
	
	private static long instanceCount = 0;
	private State state;
	private Agent userID;
	private double tick;
	private long objCount;
	
	/**
	 * Constructor
	 * @param userID
	 * @param state
	 * @param tick
	 */
	public Message(Agent userID, State state, double tick) {
		setState(state);
		setUserID(userID);
		setTick(tick);
		instanceCount ++;
		objCount = instanceCount;
	}
	
	/**
	 * Repast keeps statics around, we reset with each context.
	 */
	public static void resetInstanceCount() {
		instanceCount = 0;
	}
	
	/**
	 * Gets the state of msg
	 * @return
	 */
	public State getState() {
		return state;
	}
	
	/**
	 * Sets state
	 * @param state
	 */
	public void setState(State state) {
		this.state = state;
	}
	
	/**
	 * Gets author msg
	 * @return
	 */
	public Agent getUserID() {
		return userID;
	}
	
	/**
	 * Sets author msg
	 * @param userID
	 */
	public void setUserID(Agent userID) {
		this.userID = userID;
	}

	/**
	 * Gets tick in which msg was sent
	 * @return
	 */
	public double getTick() {
		return tick;
	}
	
	/**
	 * Sets tick in which msg sent
	 * @param tick
	 */
	public void setTick(double tick) {
		this.tick = tick;
	}

	

	@Override
	public String toString() {
		return "Message "+this.getObjCount()+", by " +userID.getLabel()+", at "+ tick +", with State: "+state;
	}
	
	/**
	 * Returns 1 if msg neutral
	 * @return
	 */
	public int getNeutral() {
		return state == State.NEUTRAL ? 1: 0;
	}
	
	/**
	 * Returns 1 if msg infected
	 * @return
	 */
	public int getInfected() {
		return state == State.INFECTED ? 1: 0;
	}
	
	/**
	 * Returns 1 if msg vaccinated
	 * @return
	 */
	public int getDebunker() {
		return state == State.VACCINATED ? 1: 0;
	}

	public long getObjCount() {
		return objCount;
	}
	
	@Override
	public boolean equals(Object objMs) {
		if (objMs instanceof Message) {
			Message otherMsg = (Message) objMs;
			if (this.objCount == otherMsg.getObjCount()) {
				return true; //same message ID (user could send MORE than 1 message in the same tick)
			}
			//double epsilon = 0.00001;
			//if (otherMsg.getState() == this.state && Math.abs(otherMsg.getTick() - this.tick) < epsilon) { //same time and state
			//	if (otherMsg.getUserID().getLabel().equals(this.userID.getLabel()) && ThreadManager.getInstance().getThreadID(otherMsg).equals(ThreadManager.getInstance().getThreadID(this))) { //same user AND same threadID (unique)
					 
			//	}
			//}

		}
		return false;
		
	}
	
	@Override
	public int hashCode() {
		return (int)this.objCount;
	}

}
