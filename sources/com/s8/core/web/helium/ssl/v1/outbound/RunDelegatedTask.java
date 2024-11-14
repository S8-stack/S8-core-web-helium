package com.s8.core.web.helium.ssl.v1.outbound;

import com.s8.core.arch.silicon.SiliconEngine;
import com.s8.core.arch.silicon.async.AsyncSiTask;
import com.s8.core.arch.silicon.async.MthProfile;


/**
 * 
 */
class RunDelegatedTask implements SSL_Outbound.Operation {

	
	@Override
	public void operate(SSL_Outbound out) {
		
			Runnable runnable = out.engine.getDelegatedTask();
			if(runnable != null) { 

				runDelegated(out, runnable);

				/* stop here, will be continued by task runner*/
			}
			else {
				/* continue */
				out.pushOp(new Wrap());
			}	
		

	}


	/**
	 * 
	 * @return isTerminated
	 */
	private void runDelegated(SSL_Outbound out, Runnable runnable) {

		SiliconEngine ng = out.getConnection().getEndpoint().getSiliconEngine();
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
				Runnable additionalRunnable = out.engine.getDelegatedTask();

				/* relaunch process if not more task */
				if(additionalRunnable != null) { 
					runDelegated(out, additionalRunnable);
				}
				else {
					out.pushOp(new Wrap());
				}
			}
		});
	}
	
	
}
