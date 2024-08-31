package inputFiles;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import exception.IncompatibleParameterException;

public class Parameters {

	private Properties prop;
	private final String fileName = "db.properties";

	/**
	 * Constructor for Parameters.
	 */
	public Parameters() throws IncompatibleParameterException{
		prop = new Properties();
		try (FileInputStream fis = new FileInputStream(fileName)) {
			prop.load(fis);
		} catch (FileNotFoundException ex) {
			throw new IncompatibleParameterException("Properties file was not found");
		} catch (IOException ex) {
			throw new IncompatibleParameterException("Properties file could not be loaded");
		}
	}

	/**
	 * Constructor for Parameters using a configuration file provided
	 * @param configFile name of the configuration file
	 */
	public Parameters(String configFile) throws IncompatibleParameterException{
		prop = new Properties();
		try (FileInputStream fis = new FileInputStream(configFile)) {
			prop.load(fis);
		} catch (FileNotFoundException ex) {
			throw new IncompatibleParameterException("File was not found");
		} catch (IOException ex) {
			throw new IncompatibleParameterException("File could not be loaded");
		}
	}

	/**
	 * Gets a specific parameter from the param file.
	 * @param param name of the parameter
	 * @return the value of said parameter
	 * @throws IncompatibleParameterException
	 */
	public String getParameter(String param) throws IncompatibleParameterException{

		if(prop.getProperty(param) != null) return prop.getProperty(param);
		

		throw new IncompatibleParameterException("The property name doesn't exist.");

	}


}
