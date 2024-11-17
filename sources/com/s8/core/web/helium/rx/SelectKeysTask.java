package com.s8.core.web.helium.rx;

import com.s8.core.arch.silicon.watch.WatchSiTask;


/**
 * 
 * @author pierreconvert
 *
 */
public class SelectKeysTask implements WatchSiTask {


	/**
	 * 
	 */
	private final RxServer server;
	
	public SelectKeysTask(RxServer server) {
		this.server = server;
	}
	
	@Override
	public WatchSiTask run() {

		server.serve();

		
		/*
		 * WHATEVER happened, we push a new SelectKeys task to re-iterate
		 */
		return new SelectKeysTask(server);
	}

	

	@Override
	public String describe() {
		return "(Rx) SELECT_KEYS";
	}
}
