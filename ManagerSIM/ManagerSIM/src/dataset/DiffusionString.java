package dataset;

import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DiffusionString {

		
		private int tick;
		private ArrayList<String> infected;
		private ArrayList<String> debunker;
		
		/**
		 * Empty Constructor
		 */
		public DiffusionString() {
			infected = new ArrayList<String>();
			debunker = new ArrayList<String>();
		}
		
		/**
		 * Constructor for a tick, no spreads
		 */
		public DiffusionString(int tick) {
			this.tick = tick;
			infected = new ArrayList<String>();
			debunker = new ArrayList<String>();
		}
		/**
		 * Constructor for the diffusion. Name-based, so each tick lists the users not a number
		 * @param tick
		 * @param infected
		 * @param debunker
		 */
		public DiffusionString(int tick, ArrayList<String> infected, ArrayList<String> debunker) {
			this.setTick(tick);
			this.setInfected(infected);
			this.setDebunker(debunker);
		}
		
		/**
		 * Returns all the users that participated in a convo, and their messages at this tick
		 * @return map with user and message list
		 */
		public HashMap<String, MessagePerUser> getTotalNames() {
			HashMap<String, MessagePerUser> map = new HashMap <String, MessagePerUser>();
			for (String user : infected) {
				
				if (map.containsKey(user)) {
					map.get(user).addInfected(1);
				}else {
					MessagePerUser msg = new MessagePerUser();
					msg.addInfected(1);
					map.put(user, msg);
				}
			}
			
			for (String user : debunker) {
				if (map.containsKey(user)) {
					map.get(user).addDebunker(1);
				}
				else {
					MessagePerUser msg = new MessagePerUser();
					msg.addDebunker(1);
					map.put(user, msg);
				}
			}
			return map;
		}
		
		/**
		 * Returns all the debunkers that participated in a convo, and their messages at this tick
		 * @return map with user and message list
		 */
		public HashMap<String, Integer> getDebunkerNames() {

			HashMap<String, Integer> map = new HashMap <String, Integer>();
			for (String user : debunker) {
				if (map.containsKey(user))map.put(user, map.get(user) + 1);
				else {
					map.put(user, 1);
				}
			}
			return map;
		
		}
		
		/**
		 * Returns all the spreaders that participated in a convo, and their messages at this tick
		 * @return map with user and message list
		 */
		public HashMap<String, Integer> getSpreaderNames() {

			HashMap<String, Integer> map = new HashMap <String, Integer>();
			for (String user : infected) {
				if (map.containsKey(user))map.put(user, map.get(user) + 1);
				else {
					map.put(user, 1);
				}
			}
			return map;
		
		}
		
		/**
		 * Returns infected size
		 * @return infected size
		 */
		public int getInfected() {
			return infected.size();
		}

		@JsonProperty("infected")
		public void setInfected(ArrayList<String> infected) {
			this.infected = infected;
		}
		

		/**
		 * Returns debunker size
		 * @return debunker size
		 */
		public int getDebunker() {

			return debunker.size();
		
		}

		@JsonProperty("debunker")
		public void setDebunker(ArrayList<String> debunker) {
			this.debunker = debunker;
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
		
		/**
		 * Adds infected user a specific number of times 
		 * @param name
		 * @param number
		 */
		public void addInfectedName(String name, int number) {
			for (int i = 0; i < number; i++) {
				infected.add(name);
			}
		}
		
		/**
		 * Adds debunker user a specific number of times 
		 * @param name
		 * @param number
		 */
		public void addDebunkerName(String name, int number) {
			for (int i = 0; i < number; i++) {
				debunker.add(name);
			}
			
		}
		
		/**
		 * Gets debunker msgs by user
		 * @param user
		 * @return number of debunker msgs by user
		 */
		public int getDebunkerUser(String user) {
			int count = 0;
			for (String userName : debunker) {
				if (userName.equals(user)) count++;
			}
			return count;
		}
		
		/**
		 * Gets infected msgs by user
		 * @param user
		 * @return number of infected msgs by user
		 */
		public int getInfectedUser(String user) {
			int count = 0;
			for (String userName : infected) {
				if (userName.equals(user)) count++;
			}
			return count;
		}
		
		public String toString() {
			return "tick: "+tick+", spreader: "+infected+", debunker: "+debunker;
		}
		
		
		
}
