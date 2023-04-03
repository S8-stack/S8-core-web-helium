package com.s8.stack.arch.helium.rx;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 * @author pc
 *
 */
public abstract class RxConnection {

	public enum State {
		NOT_INITIATED, WAITING_FOR_CONNECTION_COMPLETION, CONNECTED, CLOSING, CLOSED;
	}

	
	public static class Options {
		
	}

	/**
	 * true: is used
	 * false : can be acquired
	 */
	private AtomicBoolean isBusy;


	/**
	 * the pool this connection belongs to
	 */
	Pool pool;

	/**
	 * the slot
	 */
	long id;


	/**
	 * the current state of connection
	 */
	State state;

	/**
	 * Keep ref of selector for waking-up
	 */
	Selector selector;

	/**
	 * selection key
	 */
	SelectionKey key;

	/**
	 * the key interests operations cache.
	 */
	private int observerFilter;

	/**
	 * the encapsulated channel
	 */
	SocketChannel socketChannel;

	// private AtomicBoolean isConnectingRequested;

	/**
	 * request closing at the next update
	 */
	// private AtomicBoolean isClosingRequested;

	/**
	 * Rx layer verbosity
	 */
	private boolean isRxLayerVerbose;

	public abstract RxInbound getInbound();

	public abstract RxOutbound getOutbound();

	/**
	 * 
	 * @param id
	 * @param socketChannel
	 */
	public RxConnection(SocketChannel socketChannel) {
		super();
		this.socketChannel = socketChannel;
		this.isBusy = new AtomicBoolean(false);
	}


	public long getIdentifier() {
		return id;
	}


	public abstract RxEndpoint getEndpoint();

	/**
	 * Must be called right after connection creation
	 * 
	 * @throws IOException
	 */
	public void Rx_initialize(RxWebConfiguration configuration) throws IOException {

		this.isRxLayerVerbose = configuration.isRxVerbose;
		if (isRxLayerVerbose) {
			System.out.println("[RxWebEnpoint] endpoint has just been created");
		}

		this.selector = getEndpoint().getSelector();

		// setup channel as NON-BLOCKING (always)
		socketChannel.configureBlocking(false);

		// configure socket
		RxSocketConfiguration socketConfiguration = configuration.socketConfiguration;
		if (socketConfiguration != null) {
			socketConfiguration.setup(socketChannel.socket(), isRxLayerVerbose);
		}

		if (isRxLayerVerbose) {
			RxSocketConfiguration.read(socketChannel.socket());
		}

		// no selection so far, but build key
		this.key = socketChannel.register(selector, 0);

		// attach this connection to the key
		key.attach(this);


		state = socketChannel.isConnected() ? State.CONNECTED : State.NOT_INITIATED;


		// this.isClosingRequested = new AtomicBoolean(false);
		// this.isConnectingRequested = new AtomicBoolean(false);
		/* </flags> */
		

		/**
		 * bind bounds
		 */
		getInbound().Rx_bind(this);
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
		selector.wakeup();
	}

	/**
	 * @throws IOException 
	 */
	public void receive() {

		// update flag
		getInbound().receive();
	}

	public void send() {

		// setup out-bound to switch back into send mode
		getOutbound().send();

	}

	/**
	 * Not thread safe
	 */
	public void close() {
		state = State.CLOSING;
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
		selector.wakeup();
	}


	/**
	 * Update interest set
	 */
	public void pullInterestOps() {

		/**
		 * Concurrency is handled at this point: if the connection is busy, we skid this
		 * step (knowing that it will be call back soon). Note that, if it is never callback
		 */
		if(isBusy.compareAndSet(false, true)) {

			if(socketChannel.isOpen()) {

				int ops = 0;
				switch(state) {

				case WAITING_FOR_CONNECTION_COMPLETION:

					/* <update-observed> */
					ops |= SelectionKey.OP_CONNECT;

					break;

				case CONNECTED :

					if(getInbound().getState() == RxInbound.Need.RECEIVE) {
						ops |= SelectionKey.OP_READ;
					}

					if(getOutbound().getState() == RxOutbound.Need.SEND) {
						ops |= SelectionKey.OP_WRITE;
					}
					break;

				case NOT_INITIATED:
				case CLOSING:
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

			isBusy.set(false);
		}
		//else {
		/* in case we cannot access the connection, we presume that interest has not changed */


	}


	/**
	 * exploit ready set
	 */
	public void pushReadyOps() {
		/**
		 * Concurrency is handled at this point: if the connection is busy, we skip this
		 * step (knowing that it will be call back soon). Note that, in most case, only one operation at a time on
		 * a single connection (HTTP2 is per stream, multiplexed and continuous flow).
		 */
		if(isBusy.compareAndSet(false, true)) {

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
							if(isNowConnected) {
								state = State.CONNECTED;
							}
						}
						break;
						
					case CONNECTED :

						// filter OP_READ
						if (key.isReadable()) {
							RxInbound.Need result = getInbound().read();
							if(result == RxInbound.Need.SHUT_DOWN) {
								// close connection
								this.state = State.CLOSING;
							}
						}

						// filter OP_WRITE
						if (key.isWritable()) {
							RxOutbound.Need result = getOutbound().write();
							if(result == RxOutbound.Need.SHUT_DOWN) {
								// close connection
								this.state = State.CLOSING;
							}
						}
						
						break;
						
					case CLOSING :

						// close underlying channel
						try {
							socketChannel.close();			
						}
						catch (IOException exception) {
							exception.printStackTrace();
						}

						// Requests that the registration of this key's channel with its selector be cancelled.
						key.cancel();

						// detach from end-points list
						pool.remove(id);
						
						// switch state
						state = State.CLOSED;
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
				if(isRxLayerVerbose) {
					System.out.println("[RxConnection]: connection push has encountered an error: "+exception.getMessage());
					System.out.println("[RxConnection]: SKIPPE and continued");	
				}

				// close connection
				state = State.CLOSING;
			}

			isBusy.set(false);
		}

	}

	// public abstract void RX_onClosed();

}
