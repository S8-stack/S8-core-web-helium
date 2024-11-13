package com.s8.core.web.helium.ssl.v1;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

import com.s8.core.arch.silicon.SiliconEngine;
import com.s8.core.arch.silicon.async.AsyncSiTask;
import com.s8.core.arch.silicon.async.MthProfile;
import com.s8.core.web.helium.rx.RxOutbound;
import com.s8.core.web.helium.utilities.HeUtilities;


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


	private boolean isServer;

	private int nWraps = 0;

	private boolean isWrapping = false;

	private SSLEngine engine;



	/**
	 * Application buffer is left in WRITE mode.
	 */
	private ByteBuffer applicationBuffer;

	private SSL_Inbound inbound;




	private boolean SSL_isVerbose;



	/**
	 * 
	 * @param channel
	 */
	public SSL_Outbound(String name, SSL_WebConfiguration configuration) {
		super(name, NETWORK_OUTPUT_STARTING_CAPACITY, configuration);


		this.SSL_isVerbose = configuration.SSL_isVerbose;


		/* <buffers> */

		/* 
		 * MUST be left in write mode.
		 */
		applicationBuffer = ByteBuffer.allocate(APPLICATION_OUTPUT_STARTING_CAPACITY);

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

			/* switch application buffer to READ mode */
			applicationBuffer.flip();

			/* copy remaining content */
			extendedBuffer.put(applicationBuffer);

			/* replace (application buffer is in WRITE mode) */
			applicationBuffer = extendedBuffer;
		}
	}



	@Override
	public void onRxSending() throws IOException {
		ssl_wrap();
	}

	@Override
	public void onRxRemotelyClosed() {
		getConnection().isClosed = true;;
		close();
	}

	@Override
	public void onRxFailed(IOException exception) {
		if(SSL_isVerbose) {
			exception.printStackTrace();
		}
		getConnection().isClosed = true;
		close();
	}


	/**
	 * 
	 * @param connection
	 */
	public void SSL_bind(SSL_Connection connection) {

		this.engine = connection.ssl_getEngine();
		this.inbound = connection.getInbound();
		this.isServer = connection.getEndpoint().getWebConfiguration().isServer;
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
	 * 
	 * @return
	 */
	private boolean isLooping() {
		synchronized (lock) {
			if(nWraps > 0) {
				nWraps--;
				return true;
			}
			else {
				/* exiting synchronized section */
				isWrapping = false;
				return false;
			}
		}
	}





	private void addOneMoreWrap() {
		synchronized (lock) { nWraps++; }	
	}



	/**
	 * 
	 */
	void ssl_wrap() {

		/* add one more loop turn */
		synchronized (lock) { nWraps++; }

		/* entering critical section */ 
		if(!getConnection().isClosed && isLoopEnterable()) {

			/* clear bytes produced */
			boolean hasProducedBytes = false;

			while(isLooping()) {


				/* <retrieve> */

				/*
				 * Wrapping is greedy, i.e. it retrieves as much application data as possible
				 * before retrying to push to the network
				 */
				pump();

				/* </retrieve> */




				try {

					/* Actually wrapping... */
					applicationBuffer.flip();

					SSLEngineResult result = engine.wrap(applicationBuffer, networkBuffer);

					if(SSL_isVerbose) { 
						System.out.println("[SSL_Outbound] " + name + " :"); 
						System.out.println("\tunwrap result: " + result); 
						System.out.println("\tnetwork buffer: " + HeUtilities.printInfo(networkBuffer)); 
					}

					if(!hasProducedBytes && result.bytesProduced() > 0) {
						hasProducedBytes = true;
					}

					applicationBuffer.compact();


					/* <handshake-status> 
					 * handshake status is higher order than wrapResult
					 * */

					switch(result.getHandshakeStatus()) {

					/*
					 * From javadoc -> The SSLEngine needs to receive data from the remote side
					 * before handshaking can continue.
					 */
					case NEED_WRAP: 
						switch(result.getStatus()) {

						case OK: addOneMoreWrap(); break;

						case BUFFER_UNDERFLOW: handleApplicationBufferUnderflow(); addOneMoreWrap(); break;

						case BUFFER_OVERFLOW: handleNetworkBufferOverflow(); break;

						case CLOSED: close(); break;
						}
						break;


					case NEED_UNWRAP:
					case NEED_UNWRAP_AGAIN: 

						/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
						if(networkBuffer.position() > 0) { send(); }

						switch(result.getStatus()) {

						/* before switching to unwrap, release what has been written */
						case OK: inbound.ssl_unwrap(); break;

						case BUFFER_UNDERFLOW: handleApplicationBufferUnderflow(); inbound.ssl_unwrap(); break;

						case BUFFER_OVERFLOW: handleNetworkBufferOverflow(); inbound.ssl_unwrap(); break;

						case CLOSED: close(); break;
						}
						break;


						/*
						 * (From java doc): The SSLEngine needs the results of one (or more) delegated
						 * tasks before handshaking can continue.
						 */
					case NEED_TASK:

						/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
						if(networkBuffer.position() > 0) { send(); }


						switch(result.getStatus()) {

						case OK: 
							if(result.bytesConsumed() > 0 || result.bytesProduced() > 0) {
								handleDelegatedTask(); 	
							}
							break;

						case BUFFER_UNDERFLOW: handleApplicationBufferUnderflow(); handleDelegatedTask(); break;

						case BUFFER_OVERFLOW: handleNetworkBufferOverflow(); handleDelegatedTask(); break;

						case CLOSED: close(); break;
						}
						break;


						/*
						 * From java doc: The SSLEngine has just finished handshaking.
						 */
					case FINISHED: 

						/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
						if(networkBuffer.position() > 0) { send(); }

						switch(result.getStatus()) {

						case OK: addOneMoreWrap(); break;

						case BUFFER_UNDERFLOW: handleApplicationBufferUnderflow(); break;

						case BUFFER_OVERFLOW: handleNetworkBufferOverflow(); break;

						case CLOSED: close(); break;
						}
						break;


						// -> continue to next case

					case NOT_HANDSHAKING:

						/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
						if(networkBuffer.position() > 0) { send(); }

						switch(result.getStatus()) {

						case OK: pump(); break;

						case BUFFER_UNDERFLOW: handleApplicationBufferUnderflow(); pump(); break;

						case BUFFER_OVERFLOW: handleNetworkBufferOverflow(); pump(); break;

						case CLOSED: close(); break;
						}
						break;

					default : throw new SSLException("Unsupported case : "+result.getHandshakeStatus());

					}




				}
				catch (SSLException exception) {	
					if(SSL_isVerbose) {
						System.out.println("[Wrapping]: SSL_Exception causes endpoint to close.");
						exception.printStackTrace();
					}
					// Everything went wrong, so try launching the closing procedure
					close();
				}
			}
		}
	}





	public void pump() {


		/* peform the "pumping" operation */
		SSL_onSending(applicationBuffer);
	}





	/**
	 * 
	 * @return
	 */
	private void handleApplicationBufferUnderflow() {

		/**
		 *
		 */
		if(applicationBuffer.capacity() < engine.getSession().getApplicationBufferSize()) {
			/* new capacity first guess */
			int nc = 2 * applicationBuffer.capacity();

			/* required capacity */
			int sc = engine.getSession().getPacketBufferSize();

			while(nc < sc) { nc*=2; }

			increaseApplicationBufferCapacity(nc);
		}
		/* application buffer is likely to need more incoming data */
		else if(applicationBuffer.position() < applicationBuffer.capacity() / 2) {
			/* nothing to do */
		}
		else {
			/* new capacity first guess */
			int nc = 2 * networkBuffer.capacity();
			increaseApplicationBufferCapacity(nc);
		}

		pump();

		addOneMoreWrap();

	}


	/**
	 * Could attempt to drain the destination (application) buffer of any already obtained data, 
	 * but we'll just increase it to the size needed.
	 */
	private void handleNetworkBufferOverflow() {

		/**
		 * From Javadoc: 
		 * For example, unwrap() will return a SSLEngineResult.Status.BUFFER_OVERFLOW result if the engine 
		 * determines that there is not enough destination buffer space available. Applications should 
		 * call SSLSession.getApplicationBufferSize() and compare that value with the space available in 
		 * the destination buffer, enlarging the buffer if necessary
		 */
		if(networkBuffer.capacity() < engine.getSession().getPacketBufferSize()) {

			/* new capacity first guess */
			int nc = 2 * networkBuffer.capacity();

			/* required capacity */
			int sc = engine.getSession().getPacketBufferSize() + networkBuffer.remaining();

			while(nc < sc) { nc*=2; }

			increaseNetwordBufferCapacity(nc);

			addOneMoreWrap();
		}
		/* application buffer is likely to be filled, so drain */
		else if(networkBuffer.position() > networkBuffer.capacity()) {
			/* nothing to do */
			send();
		}
		/* ... try to increase application buffer capacity, because no other apparent reasons */
		else {
			/* new capacity first guess */
			int nc = 2 * applicationBuffer.capacity();

			increaseApplicationBufferCapacity(nc);

			addOneMoreWrap();
		}
	}





	private void handleDelegatedTask() {
		Runnable runnable = engine.getDelegatedTask();
		if(runnable != null) { 

			runDelegated(runnable);

			/* stop here, will be continued by task runner*/
		}
		else {
			addOneMoreWrap(); /* continue */
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

				/* relaunch process if not more task */
				if(additionalRunnable != null) { 
					runDelegated(additionalRunnable);
				}
				else {
					ssl_wrap();
				}
			}
		});
	}


	private void close() {




		/*
		 * Closing this side of the engine
		 */
		engine.closeOutbound();


		/* wrapping */
		try {
			boolean isWrapCompleted = false;
			while(!isWrapCompleted) {

				/*
				 * (JAVA doc) states that:
				 * 
				 * In all cases, closure handshake messages are generated by the engine, and
				 * wrap() should be repeatedly called until the resulting SSLEngineResult's
				 * status returns "CLOSED", or isOutboundDone() returns true.
				 * 
				 */
				/* wrapping */
				SSLEngineResult result = engine.wrap(applicationBuffer, networkBuffer);

				if(SSL_isVerbose) {
					System.out.println("[SSL_Outbound] : "+result);
				}

				// end point listening to result for updating phase
				getConnection().onResult(result);

				if(result.getStatus()==Status.CLOSED || engine.isOutboundDone() || result.bytesProduced()==0) {
					isWrapCompleted = true;
				}
			}

			/*
			 * All data obtained from the wrap() method should be sent to the peer.
			 */
			flush();
		}
		catch (SSLException e) {
			e.printStackTrace();
		}
	}


	private void flush() {


		// if there is actually new bytes, send them
		if(networkBuffer.position()>0) {


			/*
			 *  stop this process here (trigger sending)
			 * setup callback as this to continue on this mode asynchronously
			 */
			send();
		}
	}

}
