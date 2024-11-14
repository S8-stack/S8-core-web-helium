package com.s8.core.web.helium.ssl.v1.inbound;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.LinkedList;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;

import com.s8.core.web.helium.rx.RxInbound;
import com.s8.core.web.helium.ssl.v1.SSL_Connection;
import com.s8.core.web.helium.ssl.v1.SSL_WebConfiguration;
import com.s8.core.web.helium.ssl.v1.outbound.SSL_Outbound;


/**
 * Inbound part of the SSL_Endpoint
 * 
 * @author pc
 *
 */
public abstract class SSL_Inbound extends RxInbound {


	/**
	 * Typical required NETWORK_INPUT_STARTING_CAPACITY is 16709. Instead, we add 
	 * security margin up to: 2^14+2^10 = 17408
	 */
	public final static int NETWORK_INPUT_STARTING_CAPACITY = 17408;


	/**
	 * Typical required APPLICATION_INPUT_STARTING_CAPACITY is 16704. Instead, we add 
	 * security margin up to: 2^14+2^10 = 17408
	 */
	public final static int APPLICATION_INPUT_STARTING_CAPACITY = 17408;


	//private RxInbound base;


	private final Object lock = new Object();

	private boolean isRunning = false;


	SSLEngine engine;


	/**
	 * Mostly left in READ mode 
	 */
	ByteBuffer applicationBuffer;

	SSL_Outbound outbound;

	boolean SSL_isVerbose;

	
	/** operations */
	private final Deque<Operation> operations;

	

	/**
	 * 
	 * @param channel
	 */
	public SSL_Inbound(String name, SSL_WebConfiguration configuration) {
		super(name, configuration);

		this.SSL_isVerbose = configuration.isSSLVerbose();

		/* <buffers> */

		
		
		operations = new LinkedList<>();
	}

	@Override
	public abstract SSL_Connection getConnection();



	/**
	 * Compares <code>sessionProposedCapacity<code> with buffer's capacity. If buffer's capacity is smaller,
	 * returns a buffer with the proposed capacity. If it's equal or larger, returns a buffer
	 * with capacity twice the size of the initial one.
	 *
	 * @param buffer - the buffer to be enlarged.
	 * @param sessionProposedCapacity - the minimum size of the new buffer, proposed by {@link SSLSession}.
	 * @return A new buffer with a larger capacity.
	 */
	void increaseApplicationBufferCapacity(int capacity) {
		if(capacity > applicationBuffer.capacity()) {
			
			/* allocate new buffer */
			ByteBuffer extendedBuffer = ByteBuffer.allocate(capacity);

			/* copy remaining content */
			extendedBuffer.put(applicationBuffer);

			/* replace */
			applicationBuffer = extendedBuffer;

			/* network buffer is now in READ mode */
			applicationBuffer.flip();		
		}
	}


	@Override
	public void onRxReceived() throws IOException {
		ssl_unwrap();
	}

	@Override
	public void onRxRemotelyClosed() throws IOException {
		getConnection().isClosed = true;
		pushOp(new Close());
	}


	@Override
	public void onRxReceptionFailed(IOException exception) throws IOException {
		getConnection().isClosed = true;
		pushOp(new Close());
	}

	public abstract void SSL_onReceived(ByteBuffer buffer);




	/**
	 * 
	 * @param connection
	 */
	public void SSL_bind(SSL_Connection connection) {

		// bind 0
		this.engine = connection.ssl_getEngine();

		this.outbound = connection.getOutbound();
		
		initializeNetworkBuffer(engine.getSession().getPacketBufferSize());
		initializeApplicationBuffer(engine.getSession().getApplicationBufferSize());
		
	}


	
	private void initializeApplicationBuffer(int capacity) {
		
		/* 
		 * Left in read mode outside retrieve state. So initialize with nothing to read
		 */
		applicationBuffer = ByteBuffer.allocate(capacity);
		applicationBuffer.position(0);
		applicationBuffer.limit(0);
		/* </buffer> */
	}


	/*
	public void unwrap() {
		new Process(new Unwrapping()).launch();
	}
	 */
	
	@FunctionalInterface
	public static interface Operation {
		
		public abstract void operate(SSL_Inbound in);
		
	}


	private boolean isLoopEnterable() {
		/* equivalent to compare and set */
		synchronized (lock) {
			if(isRunning) {
				return false;
			}
			else {
				/* idle, so can enter */
				isRunning = true;
				return true;
			}
		}
	}



	private boolean isLooping() {
		synchronized (lock) {
			if(operations.size() > 0) {
				return true;
			}
			else {
				isRunning = false;
				return false;
			}
		}
	}


	/**
	 * 
	 */
	public void ssl_unwrap() {
		pushOp(new Unwrap());
	}


	/**
	 * Main SSL method
	 */
	private void boot() {

		if(isLoopEnterable()) {

			Operation operation = null;
			int count = 0;
			while(isLooping()) {
				
				synchronized (lock) { operation = operations.pollFirst(); }
				
				operation.operate(this);
			
			count++;
			System.out.println("[SSL_Inbound] " + name + " unwrap loop count : " + count);
			}
		}

	}
	
	
	void pushOpFirst(Operation operation) {
		synchronized (lock) { operations.addFirst(operation); }
		boot();
	}
	
	
	/**
	 * 
	 * @param operation
	 */
	void pushOp(Operation operation) {
		synchronized (lock) { operations.addLast(operation); }
		boot();
	}
	





}
