package dataset;

import java.util.ArrayList;




public class DatasetData {
	
	
	private int newsUID;
	private String errorName;
	private String newsName;
	private int configurationUID;
	private int execution;
	private ArrayList<Integer> endorsers; //if there's only one
	private ArrayList<Integer> deniers; //if there are two values
	private boolean isSpread; //spread is aggregate (NOT MSG)
	private int realLength;
	private String name;

	/**
	 * Constructor for real Dataset
	 * @param newsUID
	 */
	public DatasetData(int newsUID, boolean isSpread) {
		this.setNewsUID(newsUID);
		this.isSpread = isSpread;
		setConfigurationUID(0);
		setExecution(0);
		setName("");
		endorsers = new ArrayList<Integer>();
		deniers = new ArrayList<Integer>();
		newsName = Integer.toString(newsUID);

	}

	/**
	 * Constructor for dataset, used for simulations.
	 */
	public DatasetData() {

		endorsers = new ArrayList<Integer>();
		deniers = new ArrayList<Integer>();
		setName("");
	}

	/**
	 * Fills the dataset with the last known value if there are no more records and we need a specific size to compare.
	 * @param size
	 */
	public void fillWithLastOrZero(int size) {
		if (size < endorsers.size()) {
			realLength = size;
			trimToReal();
		}else {
			while((endorsers.size() < size)) {
				if (isSpread) {
					endorsers.add(endorsers.get(endorsers.size()-1)); //add last
					
					deniers.add(deniers.get(deniers.size()-1)); //add last
				} else {
					endorsers.add(0); //add 0
					deniers.add(0); //add 0
				}
			} 

		}
		
	}


	/**
	 * Adds values to the dataset.
	 * @param endorses infected
	 * @param denies debunkers
	 */
	public void addValues(int endorses, int denies) {
		endorsers.add(endorses);
		deniers.add(denies);
		realLength = endorsers.size();

	}

	/**
	 * Returns the first array (infected)
	 * @return arraylist with infected
	 */
	public ArrayList<Integer> getSpreaders(){
		return endorsers;
	}

	/**
	 * Returns the second array (debunkers)
	 * @return arraylist with debunkers
	 */
	public ArrayList<Integer> getDeniers(){
		return deniers;
	}
	

	/**
	 * Returns the newsUID
	 * @return newsUID
	 */
	public int getNewsUID() {
		return newsUID;
	}

	/**
	 * Sets the newsUID
	 * @param newsUID
	 */
	public void setNewsUID(int newsUID) {
		this.newsUID = newsUID;
	}

	/**
	 * Returns the size of the dataset.
	 * @return
	 */
	public int getLengthLoad() {
		return endorsers.size();
	}

	/**
	 * Prints the values of this dataset.
	 */
	public void print() {
		
		System.out.println("number endorsers, deniers, neutrals:");
		for(int i = 0; i < endorsers.size(); i++) {
			System.out.println(endorsers.get(i)+", "+deniers.get(i));
		}
		
	}



	/**
	 * Gets the minimum value of this dataset, either for endorsers or deniers if its appliable.
	 * @return minimum value.
	 */
	public Double getMinValue() {
		double minScore = Double.MAX_VALUE;

		for (int i=1; i< deniers.size(); i++) {
			minScore = Math.min(minScore, deniers.get(i));
			minScore = Math.min(minScore, endorsers.get(i));
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

		maxScore = Double.MIN_VALUE; //reset
		for (int i=1; i< deniers.size(); i++) {
			maxScore = Math.max(maxScore, deniers.get(i));
			maxScore = Math.max(maxScore, endorsers.get(i));
		}
		
		return maxScore;
	}

	/**
	 * Check if dataset is state-based
	 * @return true if state-based, false if msg
	 */
	public boolean isSpread() {
		return isSpread;
	}

	/**
	 * Sets state-based dataset
	 * @param isSpread
	 */
	public void setIsSpread(boolean isSpread) {
		this.isSpread = isSpread;
	}
	
	/**
	 * Trims dataset to real 
	 */
	public void trimToReal() {
		while(realLength < endorsers.size()) {
			endorsers.remove(endorsers.size() - 1);
			deniers.remove(deniers.size() - 1);
		}
		
		//getTotalNumberSprs();
		//getTotalNumberDebs();
		
	}
	
	/**
	 * Returns total number spreaders
	 * @return spreaders
	 */
	public long getTotalNumberSprs() {
		if (isSpread) {
			//System.out.println("Total spr: "+endorsers.get(endorsers.size() - 1));
			return endorsers.get(endorsers.size() - 1); //last
		}
			
		long total= 0;
		for (int el : endorsers) {
			total += el;
		}
		//System.out.println("Total spr: "+total);
		return total;
	}
	
	/**
	 * Returns total number debunkers
	 * @return debunkers
	 */
	public long getTotalNumberDebs() {
		if (isSpread) {

			//System.out.println("Total debs: "+deniers.get(deniers.size() - 1));
			return deniers.get(deniers.size() - 1); //last
		}
			
		long total= 0;
		for (int el : deniers) {
			total += el;
		}
		//System.out.println("Total deb: "+total);
		return total;
	}
	
	/**
	 * Trims data for metrics
	 */
	public void trimForMetrics() {
		// trim to 120 min without info
		int counter = 0;
		int last_value = -1;
		for(int i = 0; i< endorsers.size(); i++) { //same size
			int val = deniers.get(i) + endorsers.get(i); //adding works for states and msg
			if (val == last_value) { //if they are the same, increase
				counter++;
			} else { //reset (if 0, 0)
				counter = 0;
				last_value = val;
			}
			if (counter == 120) { //120 min without info, cut spread to i-30 (to include some in case sim grows)
				realLength = i-30;
				break;
			}
			
		}
		trimToReal();
		
	}

	
	public String toString() {
		String res = "news: "+newsUID+", is state-based: "+isSpread;
		res+= " Spreaders: "+endorsers.toString();
		res+= " Deniers: "+deniers.toString();
		return res;
	}

	public String getErrorName() {
		// TODO Auto-generated method stub
		return errorName;
	}
	
	public void setErrorName(String errorName) {
		// TODO Auto-generated method stub
		this.errorName = errorName;
	}

	public String getNewsName() {
		return newsName;
	}

	public void setNewsName(String newsName) {
		this.newsName = newsName;
	}

	public int getConfigurationUID() {
		return configurationUID;
	}

	public void setConfigurationUID(int configurationUID) {
		this.configurationUID = configurationUID;
	}

	public int getExecution() {
		return execution;
	}

	public void setExecution(int execution) {
		this.execution = execution;
	}



	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
