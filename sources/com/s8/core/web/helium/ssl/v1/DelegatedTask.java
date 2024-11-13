package com.s8.core.web.helium.ssl.v1;

import com.s8.core.arch.silicon.async.AsyncSiTask;
import com.s8.core.arch.silicon.async.MthProfile;


/**
 * 
 */
public class DelegatedTask implements AsyncSiTask {
	
	public final Runnable runnable;

	public DelegatedTask(Runnable runnable) {
		super();
		this.runnable = runnable;
	}

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
		runnable.run();
	}

}
