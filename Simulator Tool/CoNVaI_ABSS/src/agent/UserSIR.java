package agent;

import context.ThreadManager;
import message.Message;
import model.ModelSIR;
import model.ModelSIRExtended;
import message.State;
import message.Thread;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;

public class UserSIR extends Agent {

	private UserType userType;
	
	/**
	 * Constructor
	 * @param label
	 */
	public UserSIR(String label) {
		super(label);
		userType = UserType.STANDARD;
	}
	
	/**
	 * Sets the influence level. After a value, the user is an influencer
	 */
	public void setInfluence(double influence) {
		this.influence = influence;
		if (influence != 0) { //set type based on user influence
			if (influence > 0.1) {
				userType = UserType.INFLUENCER;
			}
			
		}
	}
	
	@ScheduledMethod(start = 1, interval = 1,priority = ScheduleParameters.FIRST_PRIORITY)
	public void readMessages() {
		while (!stackIsEmpty()) { //comprueba todos en una, pueden cambiar su opinión
			Message msg = this.getNextMessage();
			//System.out.println("User "+this.getLabel()+" is reading: "+msg.getObjCount());
			Thread thread = ThreadManager.getInstance().getThread(msg);
			State neighState = msg.getState();
			String newsUID = thread.getNewsPiece().getNewsID();
			
			double probChange =  ((ModelSIR)this.getModel()).getProbChange();
			if (this.getModel() instanceof ModelSIRExtended && this.userType == UserType.INFLUENCER) { //If influencer, influence is higher
				probChange += ((ModelSIRExtended)this.getModel()).getProbInfl();
			}
			
			if(neighState == State.VACCINATED && RandomHelper.nextDoubleFromTo(0, 1) <= probChange) {

				if(this.getStateForNews(newsUID) == null) {
					this.setNextState(newsUID, State.VACCINATED); //if neutral, vaccinate
					//System.out.println("Vaccinated! Changing to: "+State.VACCINATED);
				}
				else if(this.getStateForNews(thread.getNewsPiece().getNewsID()) == State.INFECTED) {
					this.setNextState(newsUID, State.CURED); //if infected, cure
					//System.out.println("Vaccinated! Changing to: "+State.CURED);
				}
			}
			
			if(neighState == State.INFECTED) { //if infected

				//System.out.println("INFECTED!");
				double probInfect =  ((ModelSIR)this.getModel()).getProbInfect();
				if (this.getModel() instanceof ModelSIRExtended && this.userType == UserType.INFLUENCER) { //If influencer, influence is higher
					probInfect += ((ModelSIRExtended)this.getModel()).getProbInfl();
				}
				
				if (RandomHelper.nextDoubleFromTo(0,1) <= probInfect && this.getStateForNews(newsUID) == null) {

					//System.out.println("Changing to: "+State.INFECTED);
					this.setNextState(newsUID, State.INFECTED); //can infect with possible influence
				}
				else if (RandomHelper.nextDoubleFromTo(0,1) <= ((ModelSIR)this.getModel()).getProbDebunk() && this.getStateForNews(newsUID) == null) {

					//System.out.println("Changing to: "+State.VACCINATED);
					this.setNextState(newsUID, State.VACCINATED); //can vaccinate (no influence)
				}

			}
			
		}
		
	}
	
	

	//@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		
		
		for (String newsUID : this.getRoles().keySet()) { //for each news I know
			State current = this.getStateForNews(newsUID);
			if (current == null || current == State.CURED) return; //if cured (or neutral) I STOP SENDING MESSAGES
			
			for (Thread thr : ThreadManager.getInstance().getAllThreads()) {
				if (thr.getNewsPiece().getNewsID().equals(newsUID)) { //same news, we add msg
					if (this.getModel() instanceof ModelSIRExtended) { //if model SIR extended, only send a message if engagement is high
						double eng = ((ModelSIRExtended)this.getModel()).getEngagementForTime(RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
						
						if(RandomHelper.nextDoubleFromTo(0,1) > eng) continue;
					}
					postMessage(thr, current);
					break;
				}
			}
		}
		
	}

	@Override
	public void postMessage(Thread thread, State state) {
		Message message = new Message(this, state, // default is to SPREAD something
				RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
		ThreadManager.getInstance().assignThread(message, thread);
		if(debug)System.out.println("User "+this.getLabel()+", is posting a message ("+message.getObjCount()+") at: " + RunEnvironment.getInstance().getCurrentSchedule().getTickCount()+", State: "+state+", for Thread: " +thread);
		
		thread.addReply(message);
		if (message.getObjCount() == 1) this.setNextState(thread.getNewsPiece().getNewsID(), state);
		// sends message to followers
		//System.out.println("SENDING TO FOLLOWERS!");
		for (Object neigh : this.getNet().getPredecessors(this)) {
			//System.out.println("SENT TO "+((Agent)neigh).getLabel());
			((Agent)neigh).addMessage(message); // we add the message to the followers of this user
			
		}
	}
	

}
