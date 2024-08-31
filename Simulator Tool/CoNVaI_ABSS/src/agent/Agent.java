package agent;


import message.Message;
import message.State;
import message.Thread;
import model.Model;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.graph.Network;
import repast.simphony.util.ContextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public abstract class Agent implements Comparable<Object>{
	
	private Network<Object>net;
	private Model model;
	
	private double lastAccess;
	private String label;
	protected boolean debug;
	protected double influence;
	//private double lastConnect;
	
	private HashMap<String, State> roles;
	private HashMap<String, State> nextRoles;
	private Stack<Message> messages;
	private ArrayList<Message> nextMessages;
	
	
	/**
	 * Constructor for an agent in the Model.
	 * @param label label or name of the node.
	 */
	public Agent(String label) {
		setLabel(label);
		setInfluence(0);
		roles = new HashMap<String, State>();
		nextRoles = new HashMap<String, State>();
		nextMessages = new ArrayList<>();
		messages = new Stack<>();
	}
	
	/**
	 * Set debug to print msgs
	 * @param debug
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
		
	}
	
	/**
	 * Returns the Network where all the nodes and links are located.
	 * @return Network
	 */
	@SuppressWarnings("unchecked")
	public Network<Object> getNet() {
		if(net == null) {
			
			Context<Object> context = ContextUtils.getContext(this);
			net = (Network<Object>)context.getProjection("network");
		}
		return net;
	}

	
	/**
	 * Sets the Network.
	 * @param net
	 */
	public void setNet(Network<Object> net) {
		this.net = net;
	}
	
	
	/**
	 * Returns the News Spreading Model.
	 * @return model
	 */
	public Model getModel() {
		return model;
	}

	
	/**
	 * Sets the News Spreading Model.
	 * @param model
	 */
	public void setModel(Model model) {
		this.model = model;
	}
	
	
	/**
	 * Sets next State for the agent.
	 * @param nextState
	 */
	public void setNextState(String newsID, State nextState) {
		
		lastAccess = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		nextRoles.put(newsID, nextState);
			
	}
	
	/**
	 * Updates the next State for the agent. 
	 * Scheduled at the beginning of each tick, before each agent acts.
	 */
	@ScheduledMethod(start = 0, interval = 1, priority = ScheduleParameters.LAST_PRIORITY)
	public void updateNextRoleAndMsg() {
		//System.out.println("UPDATING!");
		if (!nextRoles.isEmpty()) { // there are roles to assign
			//System.out.println("Roles was:"+ roles.values().toString());
			for (String elem: nextRoles.keySet()) {
				if (!roles.containsKey(elem) || nextRoles.get(elem) != State.NEUTRAL) { //if not neutral (opinion) or doesnt have an opinion
					
					roles.put(elem, nextRoles.get(elem)); // for each key, substitute values
				}
				
			}
			//System.out.println("Roles is: "+roles.values().toString());
		}
		for (Message msg : nextMessages){
			//System.out.println("USER: "+this.getLabel()+", GOT "+msg.toString());
			messages.add(msg);
		}
		nextMessages.clear();
		nextRoles.clear();
	}
	
	/**
	 * Returns the influence of the user.
	 * @return influence factor
	 */
	public double getInfluence() {
		return influence;
	}

	/**
	 * Sets the influence of the user.
	 * @param influence
	 */
	public void setInfluence(double influence) {
		//System.out.println("User "+label+"has: "+influence);
		this.influence = influence;
	}

	/**
	 * Get userID.
	 * @return userID
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets userID
	 * @param label userID
	 */
	public void setLabel(String label) {
		this.label = label;
	}


	/**
	 * Gets roles for the news it has seen.
	 * @return hashmap of roles
	 */
	public HashMap<String, State> getRoles() {
		return roles;
	}


	/**
	 * Sets roles for the news it has seen.
	 * @param roles
	 */
	public void setRoles(HashMap<String,State> roles) {
		this.roles = roles;
	}

	/**
	 * Returns the full feed of messages received.
	 * @return message stack
	 */
	public Stack<Message> getMessages() {
		return messages;
	}

	/**
	 * Sets the feed of messages.
	 * @param messages
	 */
	public void setMessages(Stack<Message> messages) {
		this.messages = messages;
	}
	
	/**
	 * Adds a message to the feed.
	 * @param message
	 */
	public void addMessage(Message message) {
		if (!nextMessages.contains(message)){
			nextMessages.add(message); // add a message if not there already (could get a notification bc follower or participant)
		}
	}
	
	/**
	 * Returns the next message of the feed (top message).
	 * @return LIFO message
	 */
	public Message getNextMessage() {
		return messages.pop();
	}
	
	/**
	 * Check if user has no more messages (now or in the next tick).
	 * @return true if empty, false if has messages
	 */
	public boolean hasNoMessages() {
		
		return messages.empty() && nextMessages.isEmpty(); // T y T -> T (vacio ) else F (lleno)
	}

	/**
	 * Check if user has no more messages in the current tick.
	 * @return true if empty, false if has messages
	 */
	public boolean stackIsEmpty() {
		
		return messages.empty(); // T y T -> T (vacio ) else F (lleno)
	}

	
	/**
	 * 
	 * @param newsID
	 * @return
	 */
	public boolean checkOpinion(String newsID) {
		return roles.containsKey(newsID);
	}
	
	/**
	 * 
	 * @param newsID
	 * @return
	 */
	public State getStateForNews(String newsID) {
		if (roles.containsKey(newsID)) {
			return roles.get(newsID);
		}
		return null;
	}
	
	/**
	 * Return main state of the agent: SPREADER, DEBUNKER, NEUTRAL
	 * @return
	 */
	public State getMainState() {
		

		long neutral = roles.values().stream().filter(c -> c==State.NEUTRAL).count();
		long vacc = roles.values().stream().filter(c -> c==State.VACCINATED).count();
		long inf = roles.values().stream().filter(c -> c==State.INFECTED).count() + roles.values().stream().filter(c -> c==State.CURED).count();
		
		if (neutral >= vacc && neutral >= inf) return State.NEUTRAL;
		if (vacc >= neutral && vacc >= inf) return State.VACCINATED;
		return State.INFECTED;
		
	}
	
	/**
	 * Step method that implements the behavior of each agent.
	 */
	public abstract void step();
	
	/**
	 * Method to post a message to a thread with the specified state
	 * @param thread
	 * @param state
	 */
	public abstract void postMessage(Thread thread, State state);
	

	/**
	 * Resets the state of the users. For restarting simulations
	 */
	public void resetState() {
		
		roles = new HashMap<String, State>();
		nextRoles = new HashMap<String, State>();
		nextMessages = new ArrayList<>();
		messages = new Stack<>();
	}
	
	@Override
	public int compareTo(Object o) { //sorted by network edges
		if(o instanceof Agent) {
			
			if (Long.valueOf(((Agent) o).getLabel()) > Long.valueOf(label)) return -1;
				
			else if (Long.valueOf(((Agent) o).getLabel()) < Long.valueOf(label)) return 1;
				
			return 0;
			
			
		} else return 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		 if (obj instanceof Agent) {
			 Agent objAgent = (Agent) obj;
			 if (objAgent.getLabel().equals(this.label)) 
				 return true;
			
		}
		 return false;
	}
	
	@Override
	public String toString() {
		return this.label;
	}
	
	@Override
	public int hashCode() {
		return this.label.hashCode();
	}


	/**
	 * Check if there's been recent changes (last 30 ticks) in its state.
	 * @return
	 */
	public boolean hasChanged() {
		double current = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		if (current - lastAccess > 20) return false; // if current time - past is over 20 minutes, it has not changed. Otherwise, it has
		return true;
	}
}
