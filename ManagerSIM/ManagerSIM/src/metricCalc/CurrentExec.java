package metricCalc;


public class CurrentExec {

	private int maxExec;


	/**
	 * Constructor for CurrentExec.
	 */
	public CurrentExec() {
		setMaxExec(0);

	}

	/**
	 * Returns total execs
	 * @return
	 */
	public int getMaxExec() {
		return maxExec;
	}

	/**
	 * Sets total execs
	 * @param maxExec
	 */
	public void setMaxExec(int maxExec) {
		this.maxExec = maxExec;
	}

	/**
	 * Clears last results
	 */
	public void clear() {
		maxExec = 0;
	}	

}
