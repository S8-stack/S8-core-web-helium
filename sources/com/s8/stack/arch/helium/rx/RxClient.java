package com.s8.stack.arch.helium.rx;

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

	@Override
	public Selector getSelector() {
		return selector;
	}


	public abstract RxConnection open(Selector selector, SocketChannel socketChannel) throws IOException;


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

		connection = open(selector, socketChannel);

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

				connection.pullInterestOps();

				/* </update> */

				// use timeout here
				selector.select();

				Set<SelectionKey> keySet = selector.selectedKeys();
				Iterator<SelectionKey> keyIterator = keySet.iterator();

				if(keyIterator.hasNext()) {
					SelectionKey key = keyIterator.next();
					keyIterator.remove();

					if(key!=null) {
						connection.pushReadyOps();
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

}
