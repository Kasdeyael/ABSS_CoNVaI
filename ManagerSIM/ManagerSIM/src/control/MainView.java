package control;

public interface MainView {

	/**
	 * Controller signals the view that the operation has finished and can resume.
	 */
	public void finishOperation(long started);

}

