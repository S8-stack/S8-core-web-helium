package com.s8.core.web.helium.ssl.v1.inbound;

import com.s8.core.web.helium.ssl.v1.inbound.SSL_Inbound.Flow;

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
