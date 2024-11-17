package com.s8.core.web.helium.rx;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * reactive web server 
 * @author pc
 *
 */
public abstract class RxServer implements RxEndpoint {


	public final static int INITIAL_POOL_SIZE = 1024;

	private int port;

	private int backlog;

	private ServerSocketChannel serverSocketChannel;

	Selector selector;

	AtomicBoolean isRunning;

	AtomicBoolean isSelecting;


	public boolean rxIsVerbose;

	final Object lock = new Object();


	private List<RxConnection> pool = new ArrayList<>(INITIAL_POOL_SIZE);


	public RxServer() {
		super();
	}




	/**
	 * Up-casting definition method
	 * @return
	 */
	/*
	@Override
	public abstract RxWebConfiguration getConfiguration();
	 */

	/**
	 * 
	 * @param selector
	 * @param channel
	 * @return
	 * @throws IOException
	 */
	public abstract RxConnection createConnection(SelectionKey key, SocketChannel channel) throws IOException;



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
		rxIsVerbose = configuration.isRxVerbose;

		isSelecting = new AtomicBoolean(false);

		isRunning = new AtomicBoolean(false);


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
		getSiliconEngine().pushWatchTask(new SelectKeysTask(this));
	}

	



	/**
	 * 
	 */
	void serve() {

		try {

			if(rxIsVerbose) {
				System.out.println("\t->server loop++ (Selecting new keys)");	
			}

			// update observables
			pool.forEach(connection -> connection.updateInterestOps());

			// select the right channels
			selector.select();

			/*
			extract the selected key set
			 */
			Set<SelectionKey> selectedKeys = selector.selectedKeys();

			Iterator<SelectionKey> iterator = selectedKeys.iterator();

			while (iterator.hasNext()) {

				SelectionKey key = iterator.next();

				// filter OP_ACCEPT
				if(key.isValid()) {
					if (key.isAcceptable()) {

						acceptConnection();
					}
					/*
					 *  perform other types of operations
					 *  (the connection has already been created)
					 */
					else { 
						((RxConnection) key.attachment()).processReadyOps();
					}
				}


				/* </connection-IO> */


				/*
				 * Remove this key from selection set. Because the Selector never does that, it
				 * only adds to the set, so if you don't do it you will reprocess the event
				 * yourself next time the Selector returns.
				 * 
				 * I just recently learned that my foreach loop over the selected keys set is
				 * bad. foreach uses the set's iterator. Modifying a collection directly (not
				 * via the iterator's methods) while iterating over it may result in
				 * "arbitrary, undeterministic" behavior.
				 * 
				 * The selected keys set may provide a fail-fast iterator. Fail-fast iterators
				 * detect such modifications and throw a ConcurrentModificationException upon
				 * the next iteration. So modifying the set in a foreach either risks
				 * undeterministic behavior or may cause exceptions - depending on the iterator
				 * implementation.
				 * 
				 * Solution: don't use foreach. Use the iterator and remove the key via
				 * iterator.remove().
				 */
				iterator.remove();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}	
	}


	private void acceptConnection() throws IOException {
		try {
			
			/* accept triggers creation of socket channel */
			SocketChannel socketChannel = serverSocketChannel.accept();
			
			/* setup channel as NON-BLOCKING (always) */
			socketChannel.configureBlocking(false);
			

			if(socketChannel!=null) {

				// no selection so far, but build key
				SelectionKey selectionKey = socketChannel.register(selector, 0);

				/* create connection */
				RxConnection connection = createConnection(selectionKey, socketChannel);				

				/* attach this connection to the key */
				selectionKey.attach(connection);

				/* add connection to the pool */
				pool.add(connection);						

			}	
		}
		catch (ClosedChannelException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void stop() throws Exception {
		//getApp().stopProcessingUnits(); --> MUST now be external
	}


	@Override
	public void keySelectorWakeup() {
		selector.wakeup();
	}
	
	
	

}
