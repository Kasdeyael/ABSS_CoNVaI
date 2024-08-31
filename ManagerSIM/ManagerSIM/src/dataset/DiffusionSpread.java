package dataset;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DiffusionSpread {
	private int run_number;
	private int batch_number;
	private String newsID;
	private int started;
	private List<DiffusionString> diffusion;
	
	/**
	 * Empty constructor
	 */
	public DiffusionSpread(){
		
	}
	
	/**
	 * Constructor
	 * @param batch_number
	 * @param run_number
	 * @param newsID
	 * @param started
	 * @param diffusion
	 */
	public DiffusionSpread(int batch_number, int run_number, String newsID, int started, List<DiffusionString> diffusion) {
		this.setBatch_number(batch_number);
		this.setRun_number(run_number);
		this.setNewsID(newsID);
		this.setStarted(started);
		this.diffusion = diffusion;
	}

	/**
	 * Returns diffusion
	 * @return
	 */
	public List<DiffusionString> getDiffusion() {
		return diffusion;
	}

	/**
	 * Sets diffusion
	 * @param diffusion
	 */
	public void setDiffusion(List<DiffusionString> diffusion) {
		this.diffusion = diffusion;
	}


	/**
	 * Returns run number
	 * @return
	 */
	public int getRun_number() {
		return run_number;
	}


	@JsonProperty("run_number")
	public void setRun_number(int run_number) {
		this.run_number = run_number;
	}

	/**
	 * Returns batch number
	 * @return
	 */
	public int getBatch_number() {
		return batch_number;
	}

	@JsonProperty("batch_number")
	public void setBatch_number(int batch_number) {
		this.batch_number = batch_number;
	}

	/**
	 * Returns newsUID
	 * @return
	 */
	public String getNewsID() {
		return newsID;
	}


	@JsonProperty("newsID")
	public void setNewsID(String newsID) {
		this.newsID = newsID;
	}

	/**
	 * Returns started tick
	 * @return
	 */
	public int getStarted() {
		return started;
	}


	@JsonProperty("started")
	public void setStarted(int started) {
		this.started = started;
	}

}
