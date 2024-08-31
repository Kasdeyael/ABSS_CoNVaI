package context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


import dataCollection.OutputThread;
import exception.IncompatibleParameterException;
import message.Message;
import message.Thread;

public class ThreadManager {

	private static ThreadManager instance;
	private HashMap<Message, String> messageThreadMap;
	private HashMap<String, Thread> threadIDMap;
	
	/**
	 * Constructor
	 */
	private ThreadManager() {
		messageThreadMap = new HashMap<>();
		threadIDMap = new HashMap<>();
	}

	/**
	 * Repast keeps the statics from one run to another. We reset the ThreadManager when the model is being created. 
	 */
	public static void resetThreadManager() {
		//System.out.println("RESETING THREAD MGR AND MSG COUNT!");
		instance = new ThreadManager();
		Message.resetInstanceCount();
	}
	
	/**
	 * Return ThreadManager instance
	 * @return
	 */
	public static ThreadManager getInstance() {
		if (instance == null)
			instance = new ThreadManager();

	    return instance;
	}
	
	/**
	 * Assigns a message to a thread
	 * @param message
	 * @param thread
	 */
	public void assignThread(Message message, Thread thread) {
		threadIDMap.put(thread.getNewsPiece().getNewsID(), thread);
		messageThreadMap.put(message, thread.getNewsPiece().getNewsID());
	}

	/**
	 * Gets the thread of a message
	 * @param message
	 * @return
	 */
	public Thread getThread(Message message) {
		return threadIDMap.get(messageThreadMap.get(message));
	}
	
	/**
	 * Gets threadID from a message
	 * @param message
	 * @return
	 */
	public String getThreadID(Message message) {
		return messageThreadMap.get(message);
	}
	
	/**
	 * Returns all threads for output
	 * @return
	 */
	public List<Thread> getAllThreads(){
		
		return threadIDMap.values().stream().collect(Collectors.toList());
		
	}

	/**
	 * Exports the threads. Needs the output folder, batch and run numbers, and debug to print output
	 * @param folder
	 * @param batch
	 * @param runNumber
	 * @param debug
	 * @throws IOException
	 */
	public void exportThreads(String folder, int batch, int runNumber, boolean debug) throws IOException {
		//Create if not exist
		File outputDir = new File(folder);
		if (!outputDir.exists()){
			outputDir.mkdirs();
		}
		
		//create folder with timestamp
		LocalDateTime now = LocalDateTime.now();
		
		String output = now.toString().replace( ":" , "-" );
		File f = new File(outputDir,output);
		f.mkdir();
		//System.out.println(getAllThreads());
		for (Thread th : getAllThreads()) {
			System.out.println(th);
			String name = th.getNewsPiece().getNewsID() +"_"+ batch +"_" + runNumber + ".json";
			File fil = new File(f,name);
			if (fil.createNewFile()) {
				String jsonString = OutputThread.getJsonThread(th, batch, runNumber);
				if (debug) System.out.println("Thread: " + jsonString);
				try (PrintStream out = new PrintStream(new FileOutputStream(fil))) {
				    out.print(jsonString);
				} 
			} else throw new IncompatibleParameterException("Output thread could not be created");
		}
		


		
		
	}
	
}
