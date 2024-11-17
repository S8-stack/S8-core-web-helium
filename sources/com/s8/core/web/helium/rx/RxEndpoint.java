package com.s8.core.web.helium.rx;

import com.s8.core.arch.silicon.SiliconEngine;

public interface RxEndpoint {

	public abstract RxWebConfiguration getWebConfiguration();

	//public Selector getSelector();

	
	/**
	 * 
	 * @return the app layer
	 */
	public abstract SiliconEngine getSiliconEngine();
	
	
	public abstract void start() throws Exception;

	public abstract void stop() throws Exception;

	

	public abstract void keySelectorWakeup();
}
