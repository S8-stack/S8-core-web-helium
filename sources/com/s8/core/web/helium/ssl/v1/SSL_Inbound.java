package com.s8.core.web.helium.ssl.v1;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

import com.s8.core.arch.silicon.SiliconEngine;
import com.s8.core.arch.silicon.async.AsyncSiTask;
import com.s8.core.arch.silicon.async.MthProfile;
import com.s8.core.web.helium.rx.RxInbound;
import com.s8.core.web.helium.utilities.HeUtilities;


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

	private boolean isUnwrapping = false;

	private int nUnwraps = 0;

	private boolean isServer;


	SSLEngine engine;


	/**
	 * Mostly left in READ mode 
	 */
	ByteBuffer applicationBuffer;

	SSL_Outbound outbound;

	boolean SSL_isVerbose;



	/**
	 * 
	 * @param channel
	 */
	public SSL_Inbound(String name, SSL_WebConfiguration configuration) {
		super(name, NETWORK_INPUT_STARTING_CAPACITY, configuration);

		this.SSL_isVerbose = configuration.isSSLVerbose();

		/* <buffers> */

		/* 
		 * Left in read mode outside retrieve state. So initialize with nothing to read
		 */
		applicationBuffer = ByteBuffer.allocate(APPLICATION_INPUT_STARTING_CAPACITY);
		applicationBuffer.position(0);
		applicationBuffer.limit(0);

		/* </buffer> */
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
	private void increaseApplicationBufferCapacity(int capacity) {
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
		close();
	}


	@Override
	public void onRxReceptionFailed(IOException exception) throws IOException {
		getConnection().isClosed = true;
		close();
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

		this.isServer = connection.getEndpoint().getWebConfiguration().isServer;
	}




	/*
	public void unwrap() {
		new Process(new Unwrapping()).launch();
	}
	 */


	private boolean isLoopEnterable() {
		synchronized (lock) {
			if(isUnwrapping) {
				return false;
			}
			else {
				/* idle, so can enter */
				isUnwrapping = true;
				return true;
			}
		}
	}



	private boolean isLooping() {
		synchronized (lock) {
			if(nUnwraps > 0) {
				nUnwraps--;
				return true;
			}
			else {
				isUnwrapping = false;
				return false;
			}
		}
	}


	private void addOneMoreUnwrap() {
		synchronized (lock) { nUnwraps++; }	
	}


	/**
	 * Main SSL method
	 */
	void ssl_unwrap() {

		/* add one more loop turn */
		synchronized (lock) { nUnwraps++; }

		if(isLoopEnterable()) {

			while(isLooping()) {

				try {

					
					/* switch application buffer into write mode */
					try {
						applicationBuffer.compact();	
					}
					catch(IllegalArgumentException e) {
						e.printStackTrace();
					}

					SSLEngineResult	result = engine.unwrap(networkBuffer, applicationBuffer);


					/* switch back application buffer into read mode */
					applicationBuffer.flip();

					if(SSL_isVerbose) { 
						System.out.println("[SSL_Inbound] " + name + " :"); 
						System.out.println("\tunwrap result: " + result); 
						System.out.println("\tnetwork buffer: " + HeUtilities.printInfo(networkBuffer)); 
					}

					// drain as soon as bytes available
					if(result.bytesProduced() > 0) {
						drain();
					}


					switch(result.getHandshakeStatus()) {

					/*
					 * From javadoc -> The SSLEngine needs to receive data from the remote side
					 * before handshaking can continue.
					 */
					case NEED_UNWRAP:
					case NEED_UNWRAP_AGAIN:

						switch(result.getStatus()) {

						/* everything is fine, so process normally -> one more run */
						case OK: addOneMoreUnwrap(); break;

						/* stop the flow, because need to pump more data */
						case BUFFER_UNDERFLOW: handleNetworkBufferUnderflow(); break;

						/* continue wrapping, because no additional I/O call involved */
						case BUFFER_OVERFLOW: handleApplicationBufferOverflow(); break;

						/* this side has been closed, so initiate closing */
						case CLOSED: close(); break;

						}
						break;

					case NEED_WRAP: 

						switch(result.getStatus()) {

						/* launch outbound side, do not add a loop */
						case OK: outbound.ssl_wrap(); break;

						/* stop the flow, because need to pump more data */
						case BUFFER_UNDERFLOW: handleNetworkBufferUnderflow(); outbound.ssl_wrap(); break;

						/* continue wrapping, because no additional I/O call involved */
						case BUFFER_OVERFLOW: handleApplicationBufferOverflow(); outbound.ssl_wrap(); break;

						/* this side has been closed, so initiate closing */
						case CLOSED: close(); break;

						}
						break;


						/*
						 * (From java doc): The SSLEngine needs the results of one (or more) delegated
						 * tasks before handshaking can continue.
						 */
					case NEED_TASK: 
						switch(result.getStatus()) {

						/* handle delegated task */
						case OK: handleDelegatedTask(); break;

						/* stop the flow, because need to pump more data */
						case BUFFER_UNDERFLOW: handleDelegatedTask(); handleNetworkBufferUnderflow(); break;

						/* continue wrapping, because no additional I/O call involved */
						case BUFFER_OVERFLOW: handleDelegatedTask(); handleApplicationBufferOverflow(); break;

						/* this side has been closed, so initiate closing */
						case CLOSED: close(); break;

						}
						break;



						/*
						 * From java doc: The SSLEngine has just finished handshaking.
						 */
					case FINISHED: 

						switch(result.getStatus()) {

						/* handle delegated task */
						case OK: addOneMoreUnwrap(); break;

						/* stop the flow, because need to pump more data */
						case BUFFER_UNDERFLOW: handleNetworkBufferUnderflow(); addOneMoreUnwrap(); break;

						/* continue wrapping, because no additional I/O call involved */
						case BUFFER_OVERFLOW: handleApplicationBufferOverflow(); addOneMoreUnwrap(); break;

						/* this side has been closed, so initiate closing */
						case CLOSED: close(); break;

						}
						break;

						// -> continue to next case

					case NOT_HANDSHAKING: 
						
						switch(result.getStatus()) {

						/* handle delegated task */
						case OK: drain(); break;

						/* stop the flow, because need to pump more data */
						case BUFFER_UNDERFLOW: handleNetworkBufferUnderflow(); drain(); break;

						/* continue wrapping, because no additional I/O call involved */
						case BUFFER_OVERFLOW: handleApplicationBufferOverflow(); break;

						/* this side has been closed, so initiate closing */
						case CLOSED: close(); break;

						}
						break;

					default : throw new SSLException("Unsupported case : "+result.getHandshakeStatus());

					}


				}
				catch (SSLException exception) {

					// analyze exception as much as possible
					analyzeException(exception);
				}
			}
		}

	}



	/**
	 * ALWAYS drain to supply the upper layer with app data
	 * as EARLY as possible
	 */
	private void drain() {

		/* Trigger SSL_onReceived
			we ignore the fact that receiver can potentially read more bytes */
		SSL_onReceived(applicationBuffer);

		/* /!\ since endPoint.onReceived read ALL data, nothing left, so clear
		application input buffer -> READ */
		applicationBuffer.clear();	

	}


	/**
	 * 
	 * @return
	 */
	private void handleNetworkBufferUnderflow() {

		/**
		 *
		 */
		if(networkBuffer.capacity() < engine.getSession().getPacketBufferSize()) {
			/* new capacity first guess */
			int nc = 2 * networkBuffer.capacity();

			/* required capacity */
			int sc = engine.getSession().getPacketBufferSize();

			while(nc < sc) { nc*=2; }

			increaseNetworkBufferCapacity(nc);
		}
		/* network buffer is likely to need more inbound data */
		else if(networkBuffer.remaining() < networkBuffer.capacity() / 2) {
			/* nothing to do */
		}
		else {
			/* new capacity first guess */
			int nc = 2 * networkBuffer.capacity();
			increaseNetworkBufferCapacity(nc);
		}

		/* in any case, need to load more inbound data in networkBuffer */
		receive();

	}


	/**
	 * Could attempt to drain the destination (application) buffer of any already obtained data, 
	 * but we'll just increase it to the size needed.
	 */
	private void handleApplicationBufferOverflow() {

		/**
		 * From Javadoc: 
		 * For example, unwrap() will return a SSLEngineResult.Status.BUFFER_OVERFLOW result if the engine 
		 * determines that there is not enough destination buffer space available. Applications should 
		 * call SSLSession.getApplicationBufferSize() and compare that value with the space available in 
		 * the destination buffer, enlarging the buffer if necessary
		 */
		if(applicationBuffer.capacity() < engine.getSession().getApplicationBufferSize()) {

			/* new capacity first guess */
			int nc = 2 * applicationBuffer.capacity();

			/* required capacity */
			int sc = engine.getSession().getApplicationBufferSize() + applicationBuffer.remaining();

			while(nc < sc) { nc*=2; }

			increaseApplicationBufferCapacity(nc);
		}
		/* application buffer is likely to be filled, so drain */
		else if(applicationBuffer.position() > applicationBuffer.capacity()) {
			drain();
		}
		/* ... try to increase application buffer capacity, because no other apparent reasons */
		else {
			/* new capacity first guess */
			int nc = 2 * applicationBuffer.capacity();

			increaseApplicationBufferCapacity(nc);
		}

		/* since no I/O involved, we can immediately retry */
		addOneMoreUnwrap();
	}



	private void handleDelegatedTask() {
		Runnable runnable = engine.getDelegatedTask();
		if(runnable != null) { 

			runDelegated(runnable);

			/* stop here, will be continued by task runner*/
		}
	}


	/**
	 * 
	 * @return isTerminated
	 */
	private void runDelegated(Runnable runnable) {
		SiliconEngine ng = getConnection().getEndpoint().getSiliconEngine();
		ng.pushAsyncTask(new AsyncSiTask() {

			@Override
			public String describe() { 
				return "[he] SSL Engine delegated task";
			}

			@Override
			public MthProfile profile() {
				return MthProfile.FX2;
			}

			@Override
			public void run() {

				/* run delegated task */
				runnable.run();

				/* try to launch execution of the next delegated task */
				Runnable additionalRunnable = engine.getDelegatedTask();

				if(additionalRunnable != null) { 
					runDelegated(additionalRunnable);
				}
				else {
					/* relaunch process if not more task */
					ssl_unwrap();
				}
			}
		});
	}


	private void analyzeException(SSLException exception) {
		exception.printStackTrace();
		if(SSL_isVerbose) {
			int nBytes = networkBuffer.limit();
			networkBuffer.position(0);
			byte[] bytes = new byte[nBytes];
			networkBuffer.get(bytes);

			System.out.println("Try casting to plain text: "+new String(bytes));	

		}
	}


	private void close() {
		try {
			engine.closeInbound();
		} 
		catch (SSLException e) {
			//e.printStackTrace();
			// --> javax.net.ssl.SSLException: closing inbound before receiving peer's close_notify
			// We don't care...
		}


		engine.closeOutbound();

		getConnection().close();

	}

}
