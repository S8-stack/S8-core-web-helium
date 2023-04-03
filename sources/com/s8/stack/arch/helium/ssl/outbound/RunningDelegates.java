package com.s8.stack.arch.helium.ssl.outbound;

class RunningDelegates extends Mode {


	private Mode callback;

	public RunningDelegates(Mode callback) {
		super();
		this.callback = callback;
	}
	
	@Override
	public String declare() {
		return "is running delegates...";
	}
	

	@Override
	public void run(SSL_Outbound.Flow flow) {

		Runnable taskRunnable = flow.getEngine().getDelegatedTask();

		if(taskRunnable!=null) {
			if(flow.isVerbose()) {
				System.out.println("\trunning delegated task...");	
			}
			// perform the task
			taskRunnable.run();
		}

		/*
		 * Switch back to callback
		 */
		flow.then(callback);

	}
}