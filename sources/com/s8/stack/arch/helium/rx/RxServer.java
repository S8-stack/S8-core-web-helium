package com.s8.stack.arch.helium.rx;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.s8.arch.silicon.SiliconEngine;


/**
 * reactive web server 
 * @author pc
 *
 */
public abstract class RxServer implements RxEndpoint {

	private int port;

	private int backlog;

	ServerSocketChannel serverSocketChannel;

	Selector selector;

	AtomicBoolean isRunning;

	AtomicBoolean isSelecting;

	Pool pool;

	public boolean isRxVerbose;




	public RxServer() {
		super();
	}
	
	
	/**
	 * 
	 * @return the app layer
	 */
	public abstract SiliconEngine getEngine();


	/**
	 * Up-casting definition method
	 * @return
	 */
	/*
	@Override
	public abstract RxWebConfiguration getConfiguration();
	 */

	@Override
	public Selector getSelector() {
		return selector;
	}

	/**
	 * 
	 * @param selector
	 * @param socketChannel
	 * @return
	 * @throws IOException
	 */
	public abstract RxConnection open(SocketChannel socketChannel) throws IOException;



	@Override
	public abstract RxWebConfiguration getWebConfiguration();
	
	
	@Override
	public void start() throws Exception {
		
		//app.startProcessingUnits(); --> MUST now be external
		
		startRxLayer();
		
	}

	/**
	 * Start web server in a thread
	 * 
	 * @throws IOException
	 * @throws Exception 
	 */
	public void startRxLayer() throws Exception {

		// initialize
		RxWebConfiguration configuration = getWebConfiguration();
		port = configuration.port;
		backlog = configuration.backlog;
		isRxVerbose = configuration.isRxVerbose;

		isSelecting = new AtomicBoolean(false);

		isRunning = new AtomicBoolean(false);

		pool = new Pool(configuration.poolCapacity);

		// open selector
		selector = Selector.open();

		// create new server socket
		serverSocketChannel = ServerSocketChannel.open();

		// bind it to its address and port
		serverSocketChannel.bind(new InetSocketAddress(port), backlog);

		// activate non-blocking mode
		serverSocketChannel.configureBlocking(false);

		// register socket channel for integrating for accepting new connection with selector
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

		// start the system
		getEngine().pushWatchTask(new SelectKeys(this));
	}



	/**
	 * Perform the select operation
	 * (Blocking)
	 * @return
	 * @throws IOException
	 */
	Set<SelectionKey> selectKeys() throws IOException {

		// select the right channels
		selector.select();

		// extract the selected key set
		Set<SelectionKey> selectedKeys = selector.selectedKeys();

		return selectedKeys;
	}


	@Override
	public void stop() throws Exception {
		//getApp().stopProcessingUnits(); --> MUST now be external
	}


}
