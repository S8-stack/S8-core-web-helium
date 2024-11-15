package com.s8.core.web.helium.ssl.v1;

public abstract class NonBlockingConcurrentLoop {
	

	private final Object lock = new Object();


	private boolean isRunning = false;

	private int nExternallyRequiredLaunchs = 0;
	
	public NonBlockingConcurrentLoop() {
		super();
	}
	
	/**
	 * 
	 * @return true if iterate block requires another turn, false otherwise
	 */
	public abstract boolean onIterate();

	
	public abstract void onTerminate();

	
	


	private boolean isEnterAllowed() {
		/* equivalent to compare and set */
		synchronized (lock) {
			if(!isRunning) { /* idle, so can enter */
				isRunning = true;
				return true;
			}
			else { /* rejected, so ask for another turn of active loop */
				nExternallyRequiredLaunchs++;
				return false;
			}
		}
	}



	private boolean isLooping(boolean isContinued) {
		synchronized (lock) {
			/* Direct continuation */
			if(isContinued) { 
				/* note that you don't need nUnwraps > 0 to continue */
				return true; 
			}
			/* External continuation */
			else if(!isContinued && nExternallyRequiredLaunchs > 0) {
				
				/* consume one */
				nExternallyRequiredLaunchs--;
				return true;
			}
			/* Termination */
			else {
				/* clear running flag on exit */
				isRunning = false;
				return false;
			}
		}
	}


	/**
	 * 
	 */
	public void run() {

		if(isEnterAllowed()) {
			
			boolean isContinued = true;
			
			while(isLooping(isContinued)) {

				isContinued = onIterate();
			}

			onTerminate();
		}
	}

}
