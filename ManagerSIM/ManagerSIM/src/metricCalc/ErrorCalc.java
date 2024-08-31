package metricCalc;

public class ErrorCalc {
	
	private double maeAggSpr;
	private double maeAggDeb;
	private double rmseAggSpr;
	private double rmseAggDeb;
	private double rmseAgg;
	private double maeAgg;
	private double msdAggSpr;
	private double msdAggDeb;
	private double msdAgg;
	
	private int configurationUID;
	private int exec;
	private int newsUID;
	/**
	 * Constructor for ErrorCalc
	 */
	public ErrorCalc() {}
	
	/**
	 * Constructor for ErrorCalc
	 * @param configUID
	 * @param exec
	 * @param newsUID
	 */
	public ErrorCalc(int configUID, int exec, int newsUID) {
		this.setConfigurationUID(configUID);
		this.setExec(exec);
		this.setNewsUID(newsUID);
	}
	
	/**
	 * Sets MAE for agg
	 * @param maeAggSpr spreaders
	 * @param maeAggDeb debunkers
	 * @param maeAgg total added
	 */
	public void setMaeAgg(double maeAggSpr, double maeAggDeb, double maeAgg) { //, double maeNonWei) {

		this.maeAgg = maeAgg;
		this.maeAggSpr = maeAggSpr;
		this.maeAggDeb = maeAggDeb;
	}
	
	/**
	 * Sets RMSE for agg
	 * @param maeAggSpr spreaders
	 * @param maeAggDeb debunkers
	 * @param maeAgg total added
	 */
	public void setRmseAgg(double rmseAggSpr, double rmseAggDeb, double rmseAgg) { //, double rmseNonWei) {

		this.rmseAgg = rmseAgg;
		this.rmseAggSpr = rmseAggSpr;
		this.rmseAggDeb = rmseAggDeb;
	}
	
	/**
	 * Sets MSD (ablation) for agg
	 * @param maeAggSpr spreaders
	 * @param maeAggDeb debunkers
	 * @param maeAgg total added
	 */
	public void setMSD(double msdAggSpr, double msdAggDeb, double msdAgg) { //, double rmseNonWei) {
		this.msdAggSpr = msdAggSpr;
		this.msdAggDeb = msdAggDeb;
		this.setMsdAgg(msdAgg);
	}
	
	/**
	 * Return MAE spreaders
	 * @return MAE
	 */
	public double getAggMaeSpr() {
		return maeAggSpr;
	}
	
	/**
	 * Return MAE debunkers
	 * @return MAE
	 */
	public double getAggMaeDeb() {
		return maeAggDeb;
	}
	/**
	 * Return MAE total
	 * @return MAE
	 */
	public double getAggMae() {
		return maeAgg;
	}
	
	/**
	 * Return RMSE total
	 * @return RMSE
	 */
	public double getAggRmse() {
		return rmseAgg;
	}
	/**
	 * Return RMSE spreaders
	 * @return RMSE
	 */
	public double getAggRmseSpr() {
		return rmseAggSpr;
	}
	
	/**
	 * Return RMSE debunkers
	 * @return RMSE
	 */
	public double getAggRmseDeb() {
		return rmseAggDeb;
	}

	/**
	 * Gets configuration for this simulation
	 * @return
	 */
	public int getConfigurationUID() {
		return configurationUID;
	}

	/**
	 * Sets configuration for this simulation
	 * @param configurationUID
	 */
	public void setConfigurationUID(int configurationUID) {
		this.configurationUID = configurationUID;
	}

	/**
	 * Gets exec for this simulation
	 * @return
	 */
	public int getExec() {
		return exec;
	}

	/**
	 * Sets exec for this simulation
	 * @param exec
	 */
	public void setExec(int exec) {
		this.exec = exec;
	}
	
	/**
	 * Return newsUID
	 * @return newsUID
	 */
	public int getNewsUID() {
		return newsUID;
	}	
	
	/**
	 * Sets newsUID
	 * @param newsUID
	 */
	public void setNewsUID(int newsUID) {
		this.newsUID = newsUID;
	}
	
	/**
	 * Return MSD spreaders
	 * @return MSD
	 */
	public double getMsdAggSpr() {
		return msdAggSpr;
	}
	
	/**
	 * Sets MSD spreaders
	 * @param msdAggSpr
	 */
	public void setMsdAggSpr(double msdAggSpr) {
		this.msdAggSpr = msdAggSpr;
	}
	
	/**
	 * Return MSD debunkers
	 * @return MSD
	 */
	public double getMsdAggDeb() {
		return msdAggDeb;
	}
	
	/**
	 * Sets MSD debunkers
	 * @param msdAggDeb
	 */
	public void setMsdAggDeb(double msdAggDeb) {
		this.msdAggDeb = msdAggDeb;
	}
	
	/**
	 * Return MSD total
	 * @return MSD
	 */
	public double getMsdAgg() {
		return msdAgg;
	}
	
	/**
	 * Sets MSD total
	 * @param msdAgg
	 */
	public void setMsdAgg(double msdAgg) {
		this.msdAgg = msdAgg;
	}




}
