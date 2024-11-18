package com.s8.core.web.helium.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

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


	private SSLEngine engine;



	/**
	 * Application buffer is left in WRITE mode.
	 */
	private ByteBuffer applicationBuffer;

	private SSL_Inbound inbound;




	boolean SSL_isVerbose = false;


	private final Object lock = new Object();

	
	private final static int WRAP = 0x01;
	
	private final static int STOP = 0x02;
	private final static int UNWRAP = 0x04;
	private final static int SEND = 0x08;
	
	private final static int RECEIVE = 0x10;
	
	/**
	 * 
	 * @param channel
	 */
	public SSL_Outbound(String name, SSL_WebConfiguration configuration) {
		super(name, configuration);


		this.SSL_isVerbose = configuration.SSL_isVerbose;

	}

	@Override
	public abstract SSL_Connection getConnection();




	@Override
	public void rx_onPreSending() throws IOException {
		ssl_launchWrap();
	}

	@Override
	public void rx_onPostSending(int nBytesWritten) throws IOException {
		ssl_launchWrap();
	}

	@Override
	public void rx_onRemotelyClosed() {
		getConnection().isClosed = true;
		close();
	}

	@Override
	public void rx_onFailed(IOException exception) {
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



	/**
	 * Key entry point
	 */
	void ssl_launchWrap() {
		
		int extOps = 0x00;
		
		synchronized (lock) {
			
			int ops = WRAP;
			
			while((ops & WRAP) == WRAP) {
				
				/* perform operation */
				ops = wrap();
				
				/* accumulate externale operations */
				extOps |= ops;
			}
			
			if(SSL_isVerbose) {
				System.out.println("[SSL_Outbound] "+name+" Exiting run...");
			}
		}
		
		/* avoid dead lock by performaing following up operations outside critical section */
		if((extOps & SEND) == SEND) { send(); }
		
		if((extOps & UNWRAP) == UNWRAP) { inbound.ssl_launchUnwrap(); }
		
		if((extOps & RECEIVE) == RECEIVE) { inbound.receive(); }
	}





	public void pump() {


		/* peform the "pumping" operation */
		SSL_onSending(applicationBuffer);
	}



	/* <main> */

	private int wrap() {
		/* <retrieve> */

		/*
		 * Wrapping is greedy, i.e. it retrieves as much application data as possible
		 * before retrying to push to the network
		 */
		pump();

		/* </retrieve> */




		try {

			if(SSL_isVerbose) { 
				System.out.println("\t\t * application buffer: " + HeUtilities.printInfo(applicationBuffer)); 
				System.out.println("\t\t * network buffer: " + HeUtilities.printInfo(networkBuffer)); 
			}

			
			/* Actually wrapping... */
			applicationBuffer.flip();


			SSLEngineResult result = engine.wrap(applicationBuffer, networkBuffer);

			/*
			if(!hasProducedBytes && result.bytesProduced() > 0) {
				hasProducedBytes = true;
			}
			 */

			applicationBuffer.compact();
			

			if(SSL_isVerbose) { 
				System.out.println("[SSL_Outbound] " + name + " :"); 
				System.out.println("\tunwrap result: " + result); 
				System.out.println("\tnetwork buffer: " + HeUtilities.printInfo(networkBuffer));
				System.out.println("\tapplication buffer: " + HeUtilities.printInfo(applicationBuffer));
				System.out.println("\n"); 
			}


			if(result.bytesConsumed() > 0) {
				System.out.println("[SSL_Outbound] " + name + " : begin transfer"); 
			}


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

				case OK: return WRAP; /* continue */

				case BUFFER_UNDERFLOW: 
					handleApplicationBufferUnderflow(); 
					return WRAP; /* continue */

				case BUFFER_OVERFLOW: 
					boolean isSendingRequired = handleNetworkBufferOverflow(); // will trigger send if necessary
					if(isSendingRequired) {
						return STOP | SEND;
					}
					else {
						return WRAP; /* continue */
					}

				case CLOSED: close(); return WRAP; /* stop */

				default: throw new SSLException("Unsupported result status : "+result.getStatus());
				}


			case NEED_UNWRAP:
			case NEED_UNWRAP_AGAIN: 

				switch(result.getStatus()) {

				/* before switching to unwrap, release what has been written */
				case OK: 

					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					if(networkBuffer.position() > 0) {  return STOP | SEND | UNWRAP; }
					else { return STOP | UNWRAP; } /* in any case, stop here */ 


				case BUFFER_UNDERFLOW: 
					handleApplicationBufferUnderflow(); 

					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					if(networkBuffer.position() > 0) {  return STOP | SEND | UNWRAP; }
					else { return STOP | UNWRAP; } /* in any case, stop here */ 

				case BUFFER_OVERFLOW: 
					boolean isSendingRequired = handleNetworkBufferOverflow();

					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					if(isSendingRequired || networkBuffer.position() > 0) {  return STOP | SEND | UNWRAP; }
					else { return STOP | UNWRAP; } /* in any case, stop here */ 

				case CLOSED: 
					inbound.ssl_launchUnwrap();
					close(); return STOP; /* stop */

				default: throw new SSLException("Unsupported result status : "+result.getStatus());
				}


				/*
				 * (From java doc): The SSLEngine needs the results of one (or more) delegated
				 * tasks before handshaking can continue.
				 */
			case NEED_TASK:

				switch(result.getStatus()) {

				case OK: 
					runDelegatedTasks();
					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					if(networkBuffer.position() > 0) { return STOP | SEND; }
					else { return WRAP; /* continue */  }

				case BUFFER_UNDERFLOW: 
					runDelegatedTasks();
					handleApplicationBufferUnderflow(); 
					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					if(networkBuffer.position() > 0) { return STOP | SEND; }
					else { return WRAP; /* continue */  }

				case BUFFER_OVERFLOW: 
					runDelegatedTasks();
					boolean isSendingRequired = handleNetworkBufferOverflow();
					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					if(isSendingRequired || networkBuffer.position() > 0) { return STOP | SEND; }
					else { return WRAP; /* continue */  }

				case CLOSED: 
					runDelegatedTasks();
					close(); return STOP; /* stop */

				default: throw new SSLException("Unsupported result status : "+result.getStatus());
				}


				/*
				 * From java doc: The SSLEngine has just finished handshaking.
				 */
			case FINISHED: 

				switch(result.getStatus()) {

				case OK: 
					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					return (networkBuffer.position() > 0 ? SEND : 0) |
							/* there are actually APP bytes to wrap and send (and networkBuffer is empty) */
							(applicationBuffer.position() > 0 ? WRAP : 0) |
							/* in any case wake-up inbound side to notify FINISHED */
							UNWRAP;
					

				case BUFFER_UNDERFLOW: 
					handleApplicationBufferUnderflow();
					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					return (networkBuffer.position() > 0 ? SEND : 0) |
							/* there are actually APP bytes to wrap and send (and networkBuffer is empty) */
							(applicationBuffer.position() > 0 ? WRAP : 0) |
							/* in any case wake-up inbound side to notify FINISHED */
							UNWRAP;

				case BUFFER_OVERFLOW: 
					boolean isSendingRequired = handleNetworkBufferOverflow();
					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					return ((networkBuffer.position() > 0 | isSendingRequired) ? SEND : 0) |
							/* there are actually APP bytes to wrap and send (and networkBuffer is empty) */
							(applicationBuffer.position() > 0 ? WRAP : 0) |
							/* in any case wake-up inbound side to notify FINISHED */
							UNWRAP;

				case CLOSED: close(); return STOP; /* stop */

				default: throw new SSLException("Unsupported result status : "+result.getStatus());
				}	


			case NOT_HANDSHAKING:
				switch(result.getStatus()) {

				case OK: 
					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					if(networkBuffer.position() > 0) { return STOP | SEND; } /* STOP_AND_SEND */
					/* there are actually APP bytes to wrap and send (and networkBuffer is empty) */
					else if(applicationBuffer.position() > 0) { return WRAP; } /* STOP_AND_SEND */
					/* everything has been consumed (APP) and transmitted (NET), so try to relaunch receive */
					else { return  STOP | RECEIVE; }

				case BUFFER_UNDERFLOW: 
					handleApplicationBufferUnderflow();
					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					if(networkBuffer.position() > 0) { return STOP | SEND; } /* STOP_AND_SEND */
					/* there are actually APP bytes to wrap and send (and networkBuffer is empty) */
					else if(applicationBuffer.position() > 0) { return WRAP; } /* STOP_AND_SEND */
					/* everything has been consumed (APP) and transmitted (NET), so try to relaunch receive */
					else { return  STOP | RECEIVE; }

				case BUFFER_OVERFLOW: 
					boolean isSendingRequired = handleNetworkBufferOverflow();
					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					if(isSendingRequired || networkBuffer.position() > 0) { return STOP | SEND; } /* STOP_AND_SEND */
					/* there are actually APP bytes to wrap and send (and networkBuffer is empty) */
					else if(applicationBuffer.position() > 0) { return WRAP; } /* STOP_AND_SEND */
					/* everything has been consumed (APP) and transmitted (NET), so try to relaunch receive */
					else { return  STOP | RECEIVE; }

				case CLOSED: close(); return STOP; /* stop */

				default: throw new SSLException("Unsupported result status : "+result.getStatus());
				}	

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
			return STOP;
		}
	}

	/* </main> */

	
	/* <handles> */




	/**
	 * Synchronous to make code easier (stability > performance)
	 */
	private void runDelegatedTasks() {
		Runnable runnable;
		while((runnable = engine.getDelegatedTask()) != null) {
			runnable.run();
		};
	}

	/**
	 * From Javadoc: 
	 * For example, unwrap() will return a SSLEngineResult.Status.BUFFER_OVERFLOW result if the engine 
	 * determines that there is not enough destination buffer space available. Applications should 
	 * call SSLSession.getApplicationBufferSize() and compare that value with the space available in 
	 * the destination buffer, enlarging the buffer if necessary
	 */
	private boolean handleNetworkBufferOverflow() {

		/**
		 * Could attempt to drain the destination (application) buffer of any already obtained data, 
		 * but we'll just increase it to the size needed.
		 */


		if(networkBuffer.capacity() < engine.getSession().getPacketBufferSize()) {

			/* new capacity first guess */
			int nc = 2 * networkBuffer.capacity();

			/* required capacity */
			int sc = engine.getSession().getPacketBufferSize() + networkBuffer.remaining();

			while(nc < sc) { nc*=2; }

			increaseNetworkBufferCapacity(nc);

			return false; /* no sending required */
		}
		/* application buffer is likely to be filled, so drain */
		else if(networkBuffer.position() > networkBuffer.capacity()) {

			return true; /* require sending */
		}
		/* ... try to increase application buffer capacity, because no other apparent reasons */
		else {
			/* new capacity first guess */
			int nc = 2 * applicationBuffer.capacity();

			increaseApplicationBufferCapacity(nc);

			return false; /* no sending required */
		}
	}


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

			
			getConnection().ssl_close();


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


	/* </handles> */

	
	/* <utilities> */


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
			//extendedBuffer.put(applicationBuffer);

			/* replace (application buffer is in WRITE mode) */
			applicationBuffer = extendedBuffer;
		}
	}


	/* </utilities> */

}
