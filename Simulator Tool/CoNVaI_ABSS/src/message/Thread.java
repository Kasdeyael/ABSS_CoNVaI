package message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import agent.Agent;

public class Thread {
	
	private NewsPiece newsPiece;
	private double iniTick;
	private Map<Double, List<Message>> replies;
	private List<Agent> userList;
	
	/**
	 * Constructor
	 * @param newsPiece
	 * @param message
	 */
	public Thread(NewsPiece newsPiece, double tick) {
		this.newsPiece = newsPiece;
		replies = new TreeMap<Double, List<Message>>();
		userList = new ArrayList<Agent>();
		this.iniTick = tick;
	}

	/**
	 * Get News piece associated (tweet id)
	 * @return
	 */
	public NewsPiece getNewsPiece() {
		return newsPiece;
	}

	/**
	 * Set News piece associated (tweet id)
	 * @param newsPiece
	 */
	public void setNewsPiece(NewsPiece newsPiece) {
		this.newsPiece = newsPiece;
	}

	
	/**
	 * Get messages in thread
	 * @return
	 */
	public Map<Double, List<Message>> getMessages() {
		return replies;
	}

	/**
	 * Sets messages in thread
	 * @param replies
	 */
	public void setMessages(Map<Double, List<Message>> replies) {
		this.replies = replies;
	}

	
	/**
	 * Gets initial message
	 * @return
	 */
	public Message getInitMessage() {
		return replies.get(iniTick).get(0); //init is iniTick, and only 1 element

	}
	
	
	/**
	 * Adds a reply to a thread, also updates the user list
	 * @param message
	 */
	public void addReply(Message message) {
		if (replies.containsKey(message.getTick())) {
			
			replies.get(message.getTick()).add(message);
			
		}else {
			List<Message> messages = new ArrayList<Message>();
			messages.add(message);
			
			replies.put(message.getTick(), messages);	
		}
		if (!userList.contains(message.getUserID())) //we do not add a prev participant
			userList.add(message.getUserID());
	}
	
	/**
	 * Returns user list
	 * @return
	 */
	public List<Agent> getUserList() {
		return userList;
	}

	/**
	 * Sets user list
	 * @param userList
	 */
	public void setUserList(List<Agent> userList) {
		this.userList = userList;
	}
	
	/**
	 * Returns tick first msg was sent
	 * @return
	 */
	public double getIniTick() {
		return iniTick;
	}
	
	@Override
	public String toString() {
		if (replies.isEmpty()) {
			return "This thread was just started!";
		}
			
		return "This thread was started by " +replies.get(iniTick).get(0).getUserID().getLabel() +", at: "+iniTick+
		", and has " + userList.size() +" participants.";
	}
	
	@Override
	public boolean equals(Object threadObj) {
		if (threadObj instanceof Thread) {
			Thread thread2 = (Thread) threadObj;
			if (thread2.getNewsPiece().getNewsID().equals(this.newsPiece.getNewsID())){
				return true;
			//if (Math.abs(thread2.getIniTick() - this.iniTick) < epsilon) { //same start (doubles, check with precision)
			//	if (thread2.getNewsPiece().equals(this.newsPiece)){ //same news shared
			//		if (thread2.getInitMessage().getUserID().getLabel().equals(this.getInitMessage().getUserID().getLabel())) //same poster user
			//			return true;
			//	}
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {

		return  this.getNewsPiece().getNewsID().hashCode(); //newsID is tweetId, so it is a unique code and should only appear once

	}

	
}
