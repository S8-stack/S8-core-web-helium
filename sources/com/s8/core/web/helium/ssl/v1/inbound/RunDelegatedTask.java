package com.s8.core.web.helium.ssl.v1.inbound;

import com.s8.core.arch.silicon.SiliconEngine;
import com.s8.core.arch.silicon.async.AsyncSiTask;
import com.s8.core.arch.silicon.async.MthProfile;

/**
 * 
 */
class RunDelegatedTask implements Operation {

	@Override
	public boolean operate(SSL_Inbound in) {
		Runnable runnable = in.engine.getDelegatedTask();
		if(runnable != null) { 

			runDelegated(in, runnable);

			/* stop here, will be continued by task runner*/
		}
		
		return true;
	}



	/**
	 * 
	 * @return isTerminated
	 */
	private void runDelegated(SSL_Inbound in, Runnable runnable) {
		
		SiliconEngine ng = in.getConnection().getEndpoint().getSiliconEngine();
		
		ng.pushAsyncTask(new AsyncSiTask() {

			@Override
			public String describe() { 
				return "[he] SSL Engine delegated task";
			}

			@Override
			public MthProfile profile() {
				return MthProfile.FX2;
			}

			@Override
			public void run() {

				/* run delegated task */
				runnable.run();

				/* try to launch execution of the next delegated task */
				Runnable additionalRunnable = in.engine.getDelegatedTask();

				if(additionalRunnable != null) { 
					runDelegated(in, additionalRunnable);
				}
				else {
					/* relaunch process if not more task */
					in.pushOp(new Unwrap());
				}
			}
		});
	}


}
