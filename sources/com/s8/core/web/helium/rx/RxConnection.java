package com.s8.core.web.helium.rx;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * 
 * @author pc
 *
 */
public abstract class RxConnection {

	public enum State {
		NOT_INITIATED, WAITING_FOR_CONNECTION_COMPLETION, CONNECTED, CLOSING, CLOSED;
	}
	

	
	/**
	 * selection key
	 */
	private final SelectionKey key;
	
	
	/**
	 * the encapsulated channel
	 */
	private final SocketChannel socketChannel;
	
	
	/**
	 * lock for needs
	 */
	private final Object needLock = new Object();
	
	
	private int need = 0;
	

	/**
	 * the key interests operations cache.
	 */
	int observerFilter;
	

	/**
	 * the current state of connection
	 */
	State state;


	// private AtomicBoolean isConnectingRequested;

	/**
	 * request closing at the next update
	 */
	// private AtomicBoolean isClosingRequested;

	/**
	 * Rx layer verbosity
	 */
	private boolean rxIsLayerVerbose;

	public abstract RxInbound getInbound();

	public abstract RxOutbound getOutbound();

	/**
	 * 
	 * @param id
	 * @param socketChannel
	 */
	public RxConnection(SelectionKey selectionChannel, SocketChannel socketChannel) {
		super();
		this.key = selectionChannel;
		this.socketChannel = socketChannel;
		
		addNeed(Need.RECEIVE);
	}





	public abstract RxEndpoint getEndpoint();

	/**
	 * Must be called right after connection creation
	 * 
	 * @throws IOException
	 */
	public void Rx_initialize(RxWebConfiguration configuration) throws IOException {

		this.rxIsLayerVerbose = configuration.isRxVerbose;
		if (rxIsLayerVerbose) {
			System.out.println("[RxWebEnpoint] endpoint has just been created");
		}

		// configure socket
		RxSocketConfiguration socketConfiguration = configuration.socketConfiguration;
		if (socketConfiguration != null) {
			socketConfiguration.setup(socketChannel.socket(), rxIsLayerVerbose);
		}

		if (rxIsLayerVerbose) {
			RxSocketConfiguration.read(socketChannel.socket());
		}

		

		state = socketChannel.isConnected() ? State.CONNECTED : State.NOT_INITIATED;


		// this.isClosingRequested = new AtomicBoolean(false);
		// this.isConnectingRequested = new AtomicBoolean(false);
		/* </flags> */


		/**
		 * bind bounds
		 */
		getInbound().rxBind(this);
		getOutbound().Rx_bind(this);	
	}



	/**
	 * must be called right after initialize
	 */
	/*
	public void bind() {

		// sub-bind
		getInbound().bind(this);
		getOutbound().bind(this);
	}
	 */


	/**
	 * connect
	 */
	public void connect() {

		// update status
		state = State.WAITING_FOR_CONNECTION_COMPLETION;

		// notify selector
		getEndpoint().keySelectorWakeup();
	}

	/**
	 * @throws IOException 
	 */
	public void receive() {

		/* <DEBUG> 

		System.out.println(">>Receive asked (previously: "+need+")");
		if(need==Need.SHUT_DOWN) {
			throw new RuntimeException("Cannot ask for sending once shut down has been requested");
		}
		</DEBUG> */

		// update flag
		addNeed(Need.RECEIVE);

		// notify selector
		getEndpoint().keySelectorWakeup();
	}

	public void send() {
		/* <DEBUG> 
		System.out.println(">>Send asked (previously: "+need+")");
		if(need==Need.SHUT_DOWN) {
			throw new RuntimeException("Cannot ask for sending once shut down has been requested");
		}
	 	</DEBUG> */

		// update flag
		addNeed(Need.SEND);
	
		// notify selector
		getEndpoint().keySelectorWakeup();

	}

	
	public void addNeed(int code) {
		synchronized (needLock) { need |= code; }
	}
	
	public void clearNeed(int code) {
		synchronized (needLock) { need &= ~code; }
	}

	public boolean hasNeed(int code) {
		synchronized (needLock) { return (need & code) == code; }
	}


	/**
	 * The underlying socket channel
	 * 
	 * @return
	 */
	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	public void wakeup() {
		getEndpoint().keySelectorWakeup();
	}

	
	

	/* <connection-processing> */
	
	/**
	 * /!\ ENDPOINT OPERATED, for thread safety reasons
	 * 
	 * MUST only be called by the endpoint
	 * Update interest set
	 */
	void updateInterestOps() {


		if(socketChannel.isOpen()) {

			int ops = 0;
			switch(state) {

			case WAITING_FOR_CONNECTION_COMPLETION:

				/* <update-observed> */
				ops |= SelectionKey.OP_CONNECT;

				break;

			case CONNECTED :

				if(hasNeed(Need.RECEIVE)) { ops |= SelectionKey.OP_READ; }

				if(hasNeed(Need.SEND)) { ops |= SelectionKey.OP_WRITE; }
				
				break;

			case NOT_INITIATED:
			case CLOSING :
			case CLOSED : 
				// no interest ops
				break;
			}

			// if filter has been updated
			if (ops != observerFilter) {

				// update cache
				observerFilter = ops;

				// update key
				key.interestOps(observerFilter);
			}
			/* </update-observed> */

		}
		else {

			/*
			 * No reason to observe anything else now
			 */
			key.interestOps(0);

			/*
			 * Initiate closing sequence
			 */
			state = State.CLOSING;
		}
	}


	/**
	 * exploit ready set
	 */
	void processReadyOps() {
		try {
			// Duplicate security line
			if(key.isValid()) {

				switch(state) {

				case NOT_INITIATED : // idle, do nothing
					break;

				case WAITING_FOR_CONNECTION_COMPLETION :


					// filter OP_CONNECT
					if (key.isConnectable() && socketChannel.isConnectionPending()) {

						// try to finish connection
						boolean isNowConnected = socketChannel.finishConnect();

						// stop requesting connection if now connected, continue otherwise
						if(isNowConnected) { state = State.CONNECTED; }
					}
					break;

				case CONNECTED :

					// filter OP_READ
					if (key.isReadable()) { getInbound().read(); }

					// filter OP_WRITE
					if (key.isWritable()) { getOutbound().write(); }

					break;

				case CLOSING: 
					rx_close();
					break;

				case CLOSED:
					// idle, nothing to do
					break;
				}
			}
		}
		catch (IOException exception) {

			// try to re-launch, so don't stop
			//
			if(rxIsLayerVerbose) {
				System.out.println("[RxConnection]: connection push has encountered an error: "+exception.getMessage());
				System.out.println("[RxConnection]: SKIPPED and continued");	
			}

			// close connection
			rx_close();
		}
		
		
		/**
		 * If a shut down has been initiated, then close
		 */
		if(hasNeed(Need.SHUT_DOWN)) { rx_close(); }
	}

	// public abstract void RX_onClosed();
	
	
	
	
	protected void rx_close() {
		// close underlying channel
		try {
			socketChannel.close();	
		}
		catch (IOException exception) {
			exception.printStackTrace();
		}

		// Requests that the registration of this key's channel with its selector be cancelled.
		key.cancel();
		

		state = State.CLOSED;
	}
	
	
	/* </connection-processing> */

	

}
