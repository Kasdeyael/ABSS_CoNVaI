package agent;

import context.ThreadManager;
import message.Message;
import message.NewsPiece;
import message.State;
import message.Thread;
import model.ModelCoNVaI;
import model.ModelCoNVaIConfidence;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.random.RandomHelper;

public class UserCoNVaI extends Agent {


	/**
	 * Constructor.
	 * @param label
	 */
	public UserCoNVaI(String label) {
		super(label);
	}
	


	@Override
	//@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		//if (RandomHelper.nextDoubleFromTo(0, 1) <= (RunEnvironment.getInstance().getCurrentSchedule().getTickCount() - getLastConnect())/4) {

			int counter = 1;
			while (!stackIsEmpty()) {
				
				if ( RandomHelper.nextDoubleFromTo(0,1) <= ((ModelCoNVaI)this.getModel()).getProbRead(counter)) { // if user WANTS to read them
					
					double current_tck = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
					//this.setLastConnect(current_tck);
					counter += 1;
					
					Message msg = this.getNextMessage();
					Thread thread = ThreadManager.getInstance().getThread(msg);
					String newsID = thread.getNewsPiece().getNewsID();
					
					if (!this.checkOpinion(newsID)) { // new information
						readSource(thread);
						
					} else { // he already knows it
						double init_msg_tck = thread.getInitMessage().getTick();
						
						double noveltyFactor = ((ModelCoNVaI)this.getModel()).getNoveltyFactor() * thread.getNewsPiece().getNovelty(thread.getInitMessage().getTick(), current_tck);
						double probInfluence = ((ModelCoNVaI)this.getModel()).getProbInfluence() * msg.getUserID().getInfluence();
						if ( RandomHelper.nextDoubleFromTo(0,1-(probInfluence+noveltyFactor)) <= ((ModelCoNVaI)this.getModel()).getProbReply(init_msg_tck, current_tck, newsID)) { //he replies

							State state = readMessage(thread, msg);
							if (state != State.NEUTRAL) postMessage(thread, state);
						}
						
					}
				
					
				} else {
					break; //stop loop if user DOES NOT want to read them
				}
			}
		//}
		
	}

	
	/**
	 * User sends a message (used for initial or for replies).
	 * @param message
	 */
	public void postMessage(Thread thread, State state) {
		
		Message message = new Message(this, state, // default is to SPREAD something
				RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
		ThreadManager.getInstance().assignThread(message, thread);
		if(debug)System.out.println("User "+this.getLabel()+", is posting a message ("+message.getObjCount()+") at: " + RunEnvironment.getInstance().getCurrentSchedule().getTickCount()+", State: "+state+", for Thread: " +thread);
		this.setNextState(thread.getNewsPiece().getNewsID(), state);
		
		thread.addReply(message);
		
		// sends message to followers
		for (Object neigh : this.getNet().getPredecessors(this)) {
			//System.out.println("SENT TO "+((Agent)neigh).getLabel());
			((Agent)neigh).addMessage(message); // we add the message to the followers of this user
			
		}
		
		//sends message to users that took part in the conversation
		for (Agent userID : thread.getUserList()) {
			if (userID != this) {
				//System.out.println("SENT TO "+(userID).getLabel()); 
				userID.addMessage(message);
			}
			
		}
		
	}
	
	/**
	 * Reads a message and determines the state
	 * @param thread
	 * @param msg
	 * @return 
	 */
	private State readMessage(Thread th, Message msg) {
		State st = msg.getState();
		State newState = State.NEUTRAL; //new State for the message issued
		
		String newsID = th.getNewsPiece().getNewsID();
		String label = msg.getUserID().getLabel();
		boolean following = false;
		
		for (Object neigh : this.getNet().getSuccessors(this)) { //confidence based on follow or not
			if (((Agent)neigh).getLabel().equals(label)) {
				following = true;
				break;
			}
			
		}
		double val = 0;
		if (this.getModel() instanceof ModelCoNVaIConfidence && following) { //confidence
			val = ((ModelCoNVaIConfidence)this.getModel()).getConfidence();
		}

			
		//if they agree, a message might be issued with their SHARED opinion
		if (st == this.getStateForNews(newsID) && RandomHelper.nextDoubleFromTo(0, 1) <= ((ModelCoNVaI)this.getModel()).getProbOpinion()) { 
			newState = st;
				
		//if they DON'T AGREE (with state not being neutral)
		}else if(st != this.getStateForNews(newsID) && RandomHelper.nextDoubleFromTo(0, 1 - val) <= ((ModelCoNVaI)this.getModel()).getProbChange()) {
			
			//user change their mind
			newState = st;
				
		//if they DON'T AGREE (with state not being neutral) - user doubles down -- DEBUNK/SPR
		} else if(st != this.getStateForNews(newsID) && this.getStateForNews(newsID) != State.NEUTRAL && RandomHelper.nextDoubleFromTo(0, 1) <= ((ModelCoNVaI)this.getModel()).getProbOpinion()) { 
			newState = this.getStateForNews(newsID);
		}

		
		return newState;
	}
	
	/**
	 * Reads the source from a thread
	 * @param th
	 */
	private void readSource(Thread th) {
		
		Agent spreader = th.getInitMessage().getUserID();
		NewsPiece news =th.getNewsPiece();
		double current_tck = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		
		boolean following = false;
		
		for (Object neigh : this.getNet().getSuccessors(this)) { //confidence based on follow or not
			if (((Agent)neigh).getLabel().equals(spreader.getLabel())) {
				following = true;
				break;
			}
			
		}
		
		double probInfluenceNews = ((ModelCoNVaI)this.getModel()).getNoveltyFactor() == 0 ? 0 : news.getProbInfluence(); //model 3-4
		double probInfluence = ((ModelCoNVaI)this.getModel()).getProbInfluence() * spreader.getInfluence();
		double probConf = 0;
		if (this.getModel() instanceof ModelCoNVaIConfidence && following) { //confidence
			probConf = ((ModelCoNVaIConfidence)this.getModel()).getConfidence();
		}
		double noveltyFactor = ((ModelCoNVaI)this.getModel()).getNoveltyFactor() * news.getNovelty(th.getInitMessage().getTick(), current_tck);
		State state;
		
		state = null;
		
		if (RandomHelper.nextDoubleFromTo(0,1-noveltyFactor) <= ((ModelCoNVaI)this.getModel()).getProbReply(th.getInitMessage().getTick(), current_tck, news.getNewsID())) {

			//infect
			if (RandomHelper.nextDoubleFromTo(0,1-(probInfluenceNews + probInfluence + probConf)) <= ((ModelCoNVaI)this.getModel()).getProbInfect() ) { //affected by diffusion AND user influencing me
				state = State.INFECTED;
			//vaccinate
			}else if (RandomHelper.nextDoubleFromTo(0,1-(probInfluenceNews + probInfluence - probConf)) <= ((ModelCoNVaI)this.getModel()).getProbDebunk() ) { //affected by diffusion AND user COMPELLING me to answer (confidence against)
				state = State.VACCINATED;
				
			}
				
				
			if (state!= null) postMessage(th, state);
			
		} else {
			state =State.NEUTRAL;
			this.setNextState(news.getNewsID(), state); //user KNOWS this news now
		}
		

	}

	
}
