package dataset;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import exception.IncompatibleParameterException;

public class DatasetInput {

	private String filename;
	private MsgSpread msgSpread;
	//boolean isSmooth;
	
	/**
	 * Empty constructor
	 */
	public DatasetInput() {
		
	}
	
	/**
	 * Constructor for DatasetInput.
	 * @param filename file where it's located.
	 * @throws IncompatibleParameterException 
	 */
	public DatasetInput(String filename) throws IncompatibleParameterException { //, boolean isSmooth) {
		this.filename = filename;
		setSpread();
		//this.isSmooth = isSmooth;
	}

	/**
	 * Returns file where it's located.
	 * @return
	 */
	
	public String getFilename() {
		return filename;
	}

	/**
	 * Gets name of dataset.
	 * @return
	 */
	public String getName() {
		return msgSpread.getName();
	}


	/**
	 * Sets spread. This method is to map through Json a file
	 * @throws IncompatibleParameterException
	 */
	public void setSpread() throws IncompatibleParameterException {
		File fl = new File(filename);
		if (fl.exists() && fl.isFile()) {
			ObjectMapper mapper = new ObjectMapper();
			// Read a single attribute
			try {
				msgSpread = mapper.readValue(fl, MsgSpread.class);
				System.out.println(msgSpread.toString());
			} catch (IOException e) {
				throw new IncompatibleParameterException("Error reading and creating the dataset from the file: "+e.getMessage());
			}
			
		} else throw new IncompatibleParameterException("Dataset file does not exist or is not file.");
		
	}
	
	/**
	 * Returns probInfl
	 * @return probInfl
	 */
	public double getProbInfl() {
		return msgSpread.getProbInfl();
	}

	/**
	 * Returns novelty
	 * @return novelty
	 */
	public double getNovelty() {
		return msgSpread.getNovelty();
	}
	
	/**
	 * Returns list of spread
	 * @return list spread
	 */
	public List<DiffusionString> getSpread() {
		return msgSpread.getDiffusion();
	}
	
	/**
	 * Returns list of states
	 * @return list states
	 */
	public List<Diffusion> getStates() {
		return msgSpread.getStates();
	}
	
	/**
	 * Returns author
	 * @return
	 */
	public String getAuthor() {
		return msgSpread.getAuthor();
	}
	
}
