package com.s8.stack.arch.helium.ssl.inbound;

import com.s8.stack.arch.helium.ssl.inbound.SSL_Inbound.Flow;

class RunningDelegates extends Mode {

	private Mode callback;
	
	public RunningDelegates(Mode callback) {
		super();
		this.callback = callback;
	}


	@Override
	public String advertise() {
		return "is running delegated task...";
	}

	
	@Override
	public void run(Flow flow) {

		Runnable taskRunnable = flow.getEngine().getDelegatedTask();

		if(taskRunnable!=null) {
			
			// perform the task
			taskRunnable.run();

			flow.then(callback);
		}	
	}

}
