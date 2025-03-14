package com.s8.core.web.helium.rx;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class RxClient implements RxEndpoint {

	private RxConnection connection;

	private Selector selector;

	private String hostname;

	private int port;

	private AtomicBoolean isRunning;


	public RxClient() {
		super();
	}


	/**
	 * Up-casting definition method
	 * @return
	 */
	@Override
	public abstract RxWebConfiguration getWebConfiguration();

	


	public abstract RxConnection createConnection(SelectionKey selectionKey, SocketChannel socketChannel) throws IOException;


	@Override
	public void start() throws IOException, Exception {

		startRxLayer();
	}

	
	public void startRxLayer() throws IOException {
		RxWebConfiguration configuration = getWebConfiguration();

		// setup
		this.hostname = configuration.hostname;
		this.port = configuration.port;
		isRunning = new AtomicBoolean(false);

		
		SocketChannel socketChannel = SocketChannel.open();
		boolean isEstablished = socketChannel.connect(new InetSocketAddress(hostname, port));
		socketChannel.configureBlocking(false);

		selector = Selector.open();

		// no selection so far, but build key
		SelectionKey selectionKey = socketChannel.register(selector, 0);

		connection = createConnection(selectionKey, socketChannel);

		if(!isEstablished) {
			connection.connect();			
		}

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				RxClient.this.run();
			}
		});
		thread.start();

	}

	public void run() {
		isRunning.set(true);

		while(isRunning.get()) {
			System.out.println("\t->client loop started");
			try {

				/* <update> */

				connection.updateInterestOps();

				/* </update> */

				// use timeout here
				selector.select();

				Set<SelectionKey> keySet = selector.selectedKeys();
				Iterator<SelectionKey> keyIterator = keySet.iterator();

				if(keyIterator.hasNext()) {
					SelectionKey key = keyIterator.next();
					keyIterator.remove();

					if(key!=null) {
						connection.processReadyOps();
					}
				}



				//Thread.sleep(250);
			}
			catch (Exception e) {
				e.printStackTrace();
			}	
		}
	}

	public void send() {
		connection.send();
	}

	public void connect() {
		connection.connect();
	}


	@Override
	public void keySelectorWakeup() {
		selector.wakeup();
	}
}
