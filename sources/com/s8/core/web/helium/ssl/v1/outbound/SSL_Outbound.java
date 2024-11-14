package com.s8.core.web.helium.ssl.v1.outbound;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.LinkedList;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;

import com.s8.core.web.helium.rx.RxOutbound;
import com.s8.core.web.helium.ssl.v1.SSL_Connection;
import com.s8.core.web.helium.ssl.v1.SSL_WebConfiguration;
import com.s8.core.web.helium.ssl.v1.inbound.SSL_Inbound;


/**
 * <p>
 * SSL_Outbound
 * </p>
 * <p>
 * SSL_Outbound is a state machine. States are called <code>Mode</code>
 * </p>
 * 
 * @author pc
 *
 */
public abstract class SSL_Outbound extends RxOutbound {

	/**
	 * Typical required NETWORK_OUTPUT_STARTING_CAPACITY is 16709. Instead, we add 
	 * security margin up to: 2^14+2^10 = 17408
	 */
	public final static int NETWORK_OUTPUT_STARTING_CAPACITY = 17408;


	/**
	 * Typical required APPLICATION_OUTPUT_STARTING_CAPACITY is 16704. Instead, we add 
	 * security margin up to: 2^14+2^10 = 17408.
	 * Replace by 2^15 (for beauty purposes)
	 */
	public final static int APPLICATION_OUTPUT_STARTING_CAPACITY = 17408;


	private final Object lock = new Object();



	private boolean isWrapping = false;

	SSLEngine engine;



	/**
	 * Application buffer is left in WRITE mode.
	 */
	ByteBuffer applicationBuffer;

	SSL_Inbound inbound;




	boolean SSL_isVerbose;

	/** operations */
	private final Deque<Operation> operations;

	private boolean isSendRequired = false;


	/**
	 * 
	 * @param channel
	 */
	public SSL_Outbound(String name, SSL_WebConfiguration configuration) {
		super(name, configuration);


		this.SSL_isVerbose = configuration.SSL_isVerbose;


		/* </buffer> */

		this.operations = new LinkedList<>();
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
	public void increaseApplicationBufferCapacity(int capacity) {
		if(capacity > applicationBuffer.capacity()) {
			/* allocate new buffer */
			ByteBuffer extendedBuffer = ByteBuffer.allocate(capacity);

			/* switch application buffer to READ mode */
			applicationBuffer.flip();

			/* copy remaining content */
			extendedBuffer.put(applicationBuffer);

			/* replace (application buffer is in WRITE mode) */
			applicationBuffer = extendedBuffer;
		}
	}



	@Override
	public void onPreRxSending() throws IOException {
		ssl_wrap();
	}

	@Override
	public void onPostRxSending(int nBytesWritten) throws IOException {
		ssl_wrap();
		
		// flush network output if any
		if(networkBuffer.position() > 0) { send(); }
	}

	@Override
	public void onRxRemotelyClosed() {
		getConnection().isClosed = true;
		pushOp(new Close());
	}

	@Override
	public void onRxFailed(IOException exception) {
		if(SSL_isVerbose) {
			exception.printStackTrace();
		}
		getConnection().isClosed = true;
		pushOp(new Close());
	}


	/**
	 * 
	 * @param connection
	 */
	public void SSL_bind(SSL_Connection connection) {

		this.engine = connection.ssl_getEngine();
		this.inbound = connection.getInbound();


		/* <buffers> */

		/* 
		 * MUST be left in write mode.
		 */

		initializeNetworkBuffer(engine.getSession().getPacketBufferSize());
		initializeApplicationBuffer(engine.getSession().getApplicationBufferSize());

	}


	private void initializeApplicationBuffer(int capacity) {
		applicationBuffer = ByteBuffer.allocate(capacity);	
	}

	/**
	 * handshaking has been successfully completed, connection is now ready
	 */
	public abstract void ssl_onHandshakingCompleted();


	/**
	 * 
	 * @param buffer
	 */
	public abstract void SSL_onSending(ByteBuffer buffer);





	private boolean isLoopEnterable() {
		synchronized (lock) {
			if(isWrapping) {
				return false;
			}
			else {
				/* idle, so can enter */
				isWrapping = true;
				return true;
			}
		}
	}

	/**
	 * Exiting synchronized section
	 * @return
	 */
	private boolean isLooping() {
		synchronized (lock) {
			if(isSendRequired || operations.size() == 0) {
				isSendRequired = false; // clear flag
				isWrapping = false;
				return false;
			}
			else {
				return true;
			}
		}
	}



	public void ssl_wrap() {
		pushOp(new Wrap());
	}


	/**
	 * 
	 */
	private void boot() {

		/* entering critical section */ 
		if(!getConnection().isClosed && isLoopEnterable()) {

			Operation operation;
			boolean isContinuing;

			while(isLooping()) {

				synchronized (lock) { operation = operations.pollFirst(); }

				isContinuing = operation.operate(this);
				
				synchronized (lock) { this.isSendRequired = !isContinuing; }
			}
		}
		
		if(SSL_isVerbose) {
			System.out.println("[SSL_Outbound] "+name+" Exiting run...");
		}
	}





	public void pump() {


		/* peform the "pumping" operation */
		SSL_onSending(applicationBuffer);
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
