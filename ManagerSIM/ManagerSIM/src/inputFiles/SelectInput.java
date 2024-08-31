package inputFiles;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import exception.IncompatibleParameterException;

public class SelectInput {

	private String filePrefix;
	private String fileSuffix;
	private String newDir;

	/**
	 * Selects the input file
	 * @param newDir directory where the files will be copied to
	 * @param filePrefix the prefix for the simulations
	 * @param fileSuffix the suffix for the simulations
	 */
	public SelectInput(String newDir, String filePrefix, String fileSuffix) {
		this.newDir = newDir;
		this.filePrefix = filePrefix;
		this.fileSuffix = fileSuffix;
	}

	/**
	 * Checks if more files with the specified preffix and suffix in the folder can be found.
	 * @return true if there are more files to load, false otherwise.
	 */
	public boolean hasNext(String folder) throws IncompatibleParameterException{

		File directory = new File(folder);

		if(!directory.exists()) throw new IncompatibleParameterException("folder "+folder+" doesn't exist.");

		String[] str = directory.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {

				if(name.startsWith(filePrefix) && name.endsWith(fileSuffix)) return true;
				return false;
			}
		});
		if(str.length < 1) return false;
		return true;
	}

	/**
	 * With the folder, return a file with first date appearance of files matching the prefix and suffix provided.
	 * @return the next file to load into the DB
	 */
	public File selectParameterFile(String folder) throws IncompatibleParameterException{

		File directory = new File(folder);

		if(!directory.exists()) throw new IncompatibleParameterException("Folder "+folder+" doesn't exist.");
		String[] str = directory.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {

				if(name.startsWith(filePrefix) && name.endsWith(fileSuffix)) return true;
				return false;
			}
		});
		if(str.length < 1) throw new IncompatibleParameterException("The file name specified didn't exist in current folder "+folder);

		Arrays.parallelSort(str); 

		//the first we find
		return new File(directory.getAbsolutePath()+File.separator+str[0]);

	}

	/**
	 * With the previous file of the configuration, return the matching for the simulation steps.
	 * @param file the configuration file
	 * @return the file to load
	 */
	public File matchingSimulationFile(File file) {

		String nombre = file.getName();
		//configuration file without suffix
		nombre = nombre.replaceAll(fileSuffix, ".txt"); 

		return new File(file.getParent()+File.separator+nombre);

	}

	/**
	 * For the files already uploaded into the DB, we create a new folder for them (if doesn't exist) and place them there
	 * @param file1 
	 * @param file2
	 * @throws IncompatibleParameterException 
	 */
	public void storeFiles(File file1, File file2) throws IncompatibleParameterException {
		System.out.println("File "+ file2.getName()+" imported.");
		File dir = new File(newDir);

		if(dir.isFile()) dir.delete();
		if(!dir.exists()) {
			if(dir.mkdir() == false) throw new IncompatibleParameterException("Directory could not be created");
		}

		boolean file1Out = file1.renameTo(new File(dir.getAbsolutePath()+File.separator+file1.getName()));
		boolean file2Out =file2.renameTo(new File(dir.getAbsolutePath()+File.separator+file2.getName()));
		if(!file1Out || !file2Out) {
			file1.delete();
			file2.delete();
		}

	}

}
