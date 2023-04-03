package com.s8.stack.arch.helium.rx;

import java.nio.channels.Selector;

public interface RxEndpoint {

	public RxWebConfiguration getWebConfiguration();

	public Selector getSelector();
	
	public void start() throws Exception;

	public void stop() throws Exception;
}
