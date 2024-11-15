package com.s8.core.web.helium.rx;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.s8.core.arch.silicon.SiliconEngine;

public interface RxEndpoint {

	public RxWebConfiguration getWebConfiguration();

	//public Selector getSelector();

	
	/**
	 * 
	 * @return the app layer
	 */
	public abstract SiliconEngine getSiliconEngine();
	
	
	public void start() throws Exception;

	public void stop() throws Exception;

	public SelectionKey buildKey(SocketChannel socketChannel) throws ClosedChannelException;

	public void keySelectorWakeup();
}
