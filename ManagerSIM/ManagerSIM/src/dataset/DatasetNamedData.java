package dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class DatasetNamedData {
	
	private String errorName;
	private int newsUID;
	private String newsName;
	private int configurationUID;
	private int execution;
	private HashMap<Integer, DiffusionString> diffusion;
	private int realLength;

	/**
	 * Constructor for Dataset
	 * @param newsUID
	 */
	public DatasetNamedData(int newsUID) {
		this.setNewsUID(newsUID);
		setConfigurationUID(0);
		setExecution(0);
		diffusion = new HashMap<Integer, DiffusionString>(); 

	}

	/**
	 * Constructor for dataset, used for simulations.
	 */
	public DatasetNamedData() {

		diffusion = new HashMap<Integer, DiffusionString>();
	}

	


	/**
	 * Adds values to the dataset.
	 * @param tick tick
	 * @param user
	 * @param endorses infected
	 * @param denies debunkers
	 */
	public void addValues(int tick, String user, int endorses, int denies) {
		DiffusionString dfStr = null;
		if (diffusion.containsKey(tick)) {
			dfStr = diffusion.get(tick);
		} else {
			
			dfStr = new DiffusionString();
			dfStr.setTick(tick);
			diffusion.put(tick, dfStr);
		}

		dfStr.addInfectedName(user, endorses);
		dfStr.addDebunkerName(user, denies);
		realLength = tick;
	}

	/**
	 * Returns the diffusion for a tick
	 * @return DiffusionString
	 */
	public DiffusionString getDiffusion(int tick){
		if (diffusion.containsKey(tick)) {
			return diffusion.get(tick);
		} else {
			return null;
		}
	}

	
	/**
	 * Returns the newsUID
	 * @return newsUID
	 */
	public int getNewsUID() {
		return newsUID;
	}
	
	/**
	 * Returns the error for this sim
	 * @return newsUID
	 */
	public String getErrorName() {
		return errorName;
	}

	/**
	 * Sets the newsUID
	 * @param newsUID
	 */
	public void setNewsUID(int newsUID) {
		this.newsUID = newsUID;
	}
	
	/**
	 * Sets the errorName
	 * @param errorName
	 */
	public void setErrorName(String errorName) {
		this.errorName = errorName;
	}


	/**
	 * Gets the minimum value of this dataset, either for endorsers or deniers if its appliable.
	 * @return minimum value.
	 */
	public Double getMinValue() {
		double minScore = Double.MAX_VALUE;

		for (int val : diffusion.keySet()) {
			minScore = Math.min(minScore, diffusion.get(val).getInfected());
			minScore = Math.min(minScore, diffusion.get(val).getDebunker());
		}
		
		return minScore;
	}


	/**
	 * Gets the maximum value of this dataset, for one or the other
	 * @param dens indicates if it should take into account the deniers
	 * @return maximum value.
	 */

	public Double getMaxValue() {
		double maxScore = Double.MIN_VALUE;

		for (int val : diffusion.keySet()) {
			maxScore = Math.max(maxScore, diffusion.get(val).getInfected());
			maxScore = Math.max(maxScore, diffusion.get(val).getDebunker());
		}
		
		return maxScore;
	}

	/**
	 * Returns total number of spreaders
	 * @return number spreaders
	 */
	public long getTotalNumberSprs() {
		long total= 0;
		for (int val : diffusion.keySet()) {
			total += diffusion.get(val).getInfected();
		}
		return total;
	}
	
	/**
	 * Returns total number of debunkers
	 * @return number debunkers
	 */
	public long getTotalNumberDebs() {
		long total= 0;
		for (int val : diffusion.keySet()) {
			total += diffusion.get(val).getDebunker();
		}
		return total;
	}

	/**
	 * Trims dataset based on engagement. Only for the real dataset.
	 */
	public void trimForMetrics() {
		int last_value = -1;
		int counter = 0;
		SortedSet<Integer> keys = new TreeSet<>(diffusion.keySet());
		for (int tick : keys) { //same size
			DiffusionString dfStr = diffusion.get(tick); //get current
			int val = dfStr.getInfected() + dfStr.getDebunker();
			
			if (val == last_value) { //if they are the same, increase
				counter++;
			} else { //reset (if 0, 0)
				counter = 0;
				last_value = val;
			}
			if (counter == 120) { //120 min without info, cut spread to i-30 (to include some in case sim grows)
				realLength = tick-30;
				break;
			}
			
			
		}
		trimToReal();
		
	}
	
	/**
	 * Trims dataset based on the last val
	 */
	private void trimToReal() {
		SortedSet<Integer> keys = new TreeSet<>(diffusion.keySet());
		for (int val : keys) { //same size
			if (val > realLength) {
				diffusion.remove(val);
			}
			
		}
	}
	
	/**
	 * Gets length real data
	 * @return
	 */
	public int getLength() {
		return realLength;
	}
	
	/**
	 * Fills data with zero val (if shorter) or trims based on length. For simulations
	 * @param length total length
	 */
	public void fillWithLastOrZero(int length) {
		
		if (length < diffusion.size()) { 
			realLength = length;
			trimToReal();
			
		}else {
			SortedSet<Integer> keys = new TreeSet<>(diffusion.keySet());
			int last_val = keys.last(); //last tick
			while((last_val < length)) {
				last_val += 1;
				diffusion.put(last_val, new DiffusionString(last_val));
				
			} 

		}
	}
	
	/**
	 * Gets spreaders until tick i
	 * @param i tick i
	 * @return name of spreaders
	 */
	public ArrayList<String> getSpreadersUntil(int i) {
		ArrayList<String> users = new ArrayList<>();
		SortedSet<Integer> keys = new TreeSet<>(diffusion.keySet());
		for (int val : keys) { //same size
			if (val <= i) {
				HashMap<String, Integer> dfs = diffusion.get(val).getSpreaderNames();
				for (String user: dfs.keySet()) {
					for (int j=0; j< dfs.get(user); j++) {
						users.add(user);
					}
					
				}
				
			} else { 
				break;
			}
			
		}
		
		return users;
	}
	
	/**
	 * Gets debunkers until tick i
	 * @param i tick i
	 * @return name of spreaders
	 */
	public ArrayList<String> getDebunkersUntil(int i) {
		ArrayList<String> users = new ArrayList<>();
		SortedSet<Integer> keys = new TreeSet<>(diffusion.keySet());
		for (int val : keys) { //same size
			if (val <= i) {
				HashMap<String, Integer> dfs = diffusion.get(val).getDebunkerNames();
				for (String user: dfs.keySet()) {
					for (int j=0; j< dfs.get(user); j++) {
						users.add(user);
					}
					
				}
				
			} else {
				break;
			}
			
		}
		
		return users;
	}
	
	/**
	 * Returns name
	 * @return
	 */
	public String getNewsName() {
		return newsName;
	}

	/**
	 * Sets name
	 * @param newsName
	 */
	public void setNewsName(String newsName) {
		this.newsName = newsName;
	}
	
	/**
	 * Returns configUID
	 * @return
	 */
	public int getConfigurationUID() {
		return configurationUID;
	}
	
	/**
	 * Sets configUID
	 * @param configurationUID
	 */
	public void setConfigurationUID(int configurationUID) {
		this.configurationUID = configurationUID;
	}
	
	/**
	 * Returns execution
	 * @return
	 */
	public int getExecution() {
		return execution;
	}
	
	/**
	 * Sets execution
	 * @param execution
	 */
	public void setExecution(int execution) {
		this.execution = execution;
	}
	
}
