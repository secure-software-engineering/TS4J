package example2;

public abstract class AsyncRemoteCall {
	
	private boolean finishedSuccessfully;
	
	public abstract void execute();
	
	protected void finished() {
		// this one is finished, execute the next one in the list
		finishedSuccessfully = true;
		notifyAll();
	}
	
	protected void aborted() {
		// this one failed, abort the remaining list
		notifyAll();
	}
	
	public boolean isFinishedSuccessfully() {
		return finishedSuccessfully;
	}

}
