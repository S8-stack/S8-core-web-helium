package com.s8.core.web.helium.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

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

	private SSLEngine engine;


	/**
	 * Mostly left in READ mode 
	 */
	private ByteBuffer applicationBuffer;

	private SSL_Outbound outbound;

	boolean SSL_isVerbose = false;


	private final Object lock = new Object();

	private final static int UNWRAP = 0x01;

	private final static int STOP = 0x02;
	private final static int WRAP = 0x04;
	private final static int RECEIVE = 0x08;
	//private final static int SEND = 0x08;




	/**
	 * 
	 * @param channel
	 */
	public SSL_Inbound(String name, SSL_WebConfiguration configuration) {
		super(name, configuration);

		this.SSL_isVerbose = configuration.isSSLVerbose();

		/* <buffers> */



		//operations = new LinkedList<>();
		//operations.add(new Unwrap());
	}

	@Override
	public abstract SSL_Connection getConnection();



	@Override
	public void rx_onReceived() throws IOException {
		ssl_launchUnwrap();
	}

	@Override
	public void rx_onRemotelyClosed() throws IOException {
		getConnection().isClosed = true;
		close();
	}


	@Override
	public void rx_onReceptionFailed(IOException exception) throws IOException {
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

		rxInitializeNetworkBuffer(engine.getSession().getPacketBufferSize());
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
		//applicationBuffer.clear();	
	}


	/**
	 * 
	 */
	public void ssl_launchUnwrap() {
		
		int extOps = UNWRAP;
		
		
		synchronized (lock) {
			
			int ops = UNWRAP;

			while((ops & UNWRAP) == UNWRAP) { 
				
				/* unwrap */
				ops = unwrap();
				
				/* accumulate externale operations */
				extOps |= ops;
			}

			if(SSL_isVerbose) {
				System.out.println("[SSL_Inbound] "+name+" Exiting run...");
			}
		}

		/* avoid dead lock by performaing following up operations outside critical section */
		if((extOps & RECEIVE) == RECEIVE) { receive(); }

		if((extOps & WRAP) == WRAP) { outbound.ssl_launchWrap(); }
	}



	/* <main> */


	private int unwrap() {

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
				System.out.println("\t"+name+" network buffer: " + HeUtilities.printInfo(networkBuffer)); 
				System.out.println("\t"+name+" application buffer: " + HeUtilities.printInfo(applicationBuffer)); 
				System.out.println("\n");
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
				case OK: return UNWRAP;

				/* stop the flow, because need to pump more data */
				case BUFFER_UNDERFLOW: 
					boolean isReceivedRequired = handleNetworkBufferUnderflow();
					if(isReceivedRequired) { return STOP | RECEIVE; }
					else { return UNWRAP; }

					/* continue wrapping, because no additional I/O call involved */
				case BUFFER_OVERFLOW: 
					handleApplicationBufferOverflow();
					return UNWRAP;

					/* this side has been closed, so initiate closing */
				case CLOSED: return STOP;

				default: throw new SSLException("Unsupported SSLResult status");
				}

			case NEED_WRAP: 



				switch(result.getStatus()) {

				/* not need to continue */
				/* launch outbound side, do not add a loop */
				case OK: return STOP | WRAP;

				/* stop the flow, because need to pump more data */
				case BUFFER_UNDERFLOW: 
					boolean isReceivedRequired = handleNetworkBufferUnderflow();
					if(isReceivedRequired) { return STOP | WRAP | RECEIVE; }
					else { return STOP | WRAP; }

					/* continue wrapping, because no additional I/O call involved */
				case BUFFER_OVERFLOW: handleApplicationBufferOverflow(); return STOP | WRAP;

				/* this side has been closed, so initiate closing */
				case CLOSED: close(); return STOP;

				default: throw new SSLException("Unsupported SSLResult status");
				}


				/*
				 * (From java doc): The SSLEngine needs the results of one (or more) delegated
				 * tasks before handshaking can continue.
				 */
			case NEED_TASK: 
				switch(result.getStatus()) {

				/* handle delegated task */
				case OK: runDelegatedTasks(); return UNWRAP;

				/* stop the flow, because need to pump more data */
				case BUFFER_UNDERFLOW: 
					runDelegatedTasks();
					boolean isReceivedRequired = handleNetworkBufferUnderflow();
					if(isReceivedRequired) {
						return STOP | RECEIVE; /* stop */
					}
					else {
						return UNWRAP; /* continue */
					}

					/* continue wrapping, because no additional I/O call involved */
				case BUFFER_OVERFLOW: 
					runDelegatedTasks();
					handleApplicationBufferOverflow();
					return UNWRAP;

					/* this side has been closed, so initiate closing */
				case CLOSED: 
					close();
					return STOP; /* stop */

				default : throw new SSLException("Unsupported SSLResult status");
				}


				/*
				 * From java doc: The SSLEngine has just finished handshaking.
				 */
			case FINISHED: 

				switch(result.getStatus()) {

				case OK: 
					return networkBuffer.hasRemaining() ? UNWRAP | WRAP : STOP | RECEIVE | WRAP; /* continue is something is left to read from network*/

					/* stop the flow, because need to pump more data */
				case BUFFER_UNDERFLOW: 
					boolean isReceivingRequired = handleNetworkBufferUnderflow();
					if(isReceivingRequired) { 
						return STOP | RECEIVE; /* stop to wait for entword data */ 
					}
					else {
						return networkBuffer.hasRemaining() ? UNWRAP | WRAP : STOP | RECEIVE | WRAP; /* continue is something is left to read from network*/
					}


					/* continue wrapping, because no additional I/O call involved */
				case BUFFER_OVERFLOW: 
					handleApplicationBufferOverflow();
					return networkBuffer.hasRemaining() ? UNWRAP | WRAP : STOP | RECEIVE | WRAP; /* continue is something is left to read from network*/

					/* this side has been closed, so initiate closing */
				case CLOSED: 
					close();
					return STOP; /* terminated */

				default : throw new SSLException("Unsupported SSLResult status");
				}


			case NOT_HANDSHAKING: 

				switch(result.getStatus()) {

				case OK: 
					return networkBuffer.hasRemaining() ? UNWRAP : STOP | RECEIVE; /* continue is something is left to read from network*/

					/* stop the flow, because need to pump more data */
				case BUFFER_UNDERFLOW: 
					boolean isReceivingRequired = handleNetworkBufferUnderflow();
					if(isReceivingRequired) { 
						return STOP | RECEIVE; /* stop to wait for entword data */ 
					}
					else {
						return networkBuffer.hasRemaining() ? UNWRAP : STOP | RECEIVE; /* continue is something is left to read from network*/
					}


					/* continue wrapping, because no additional I/O call involved */
				case BUFFER_OVERFLOW: 
					handleApplicationBufferOverflow();
					return networkBuffer.hasRemaining() ? UNWRAP : STOP | RECEIVE; /* continue is something is left to read from network*/

					/* this side has been closed, so initiate closing */
				case CLOSED: 
					close();
					return STOP; /* terminated */

				default : throw new SSLException("Unsupported SSLResult status");
				}

			default : throw new SSLException("Unsupported case : "+result.getHandshakeStatus());

			}


		}
		catch (SSLException exception) {

			// analyze exception as much as possible
			exception.printStackTrace();
			if(SSL_isVerbose) {
				int nBytes = networkBuffer.limit();
				networkBuffer.position(0);
				byte[] bytes = new byte[nBytes];
				networkBuffer.get(bytes);

				System.out.println("Try casting to plain text: "+new String(bytes));	

			}

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
	 * 
	 * @param in
	 */
	private void handleApplicationBufferOverflow() {

		/**
		 * Could attempt to drain the destination (application) buffer of any already obtained data, 
		 * but we'll just increase it to the size needed.
		 */

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
		else if(applicationBuffer.position() > applicationBuffer.capacity() / 2) {
			if(SSL_isVerbose) { System.out.print("[SSL_Inbound] skip application buffer capacity increase: too empty"); }
			/* should be drained by next unwrap call */
		}
		/* ... try to increase application buffer capacity, because no other apparent reasons */
		else {
			/* new capacity first guess */
			int nc = 2 * applicationBuffer.capacity();

			increaseApplicationBufferCapacity(nc);
		}
	}




	/**
	 * 
	 * @param in
	 * @return true if receive is required
	 */
	private boolean handleNetworkBufferUnderflow() {

		/**
		 *
		 */
		if(networkBuffer.capacity() < engine.getSession().getPacketBufferSize()) {
			/* new capacity first guess */
			int nc = 2 * networkBuffer.capacity();

			/* required capacity */
			int sc = engine.getSession().getPacketBufferSize();

			while(nc < sc) { nc*=2; }

			rxIncreaseNetworkBufferCapacity(nc);

			return false;
		}
		/* network buffer is likely to need more inbound data */
		else if(networkBuffer.remaining() < networkBuffer.capacity() / 2) {
			/* nothing to do */

			/* in any case, need to load more inbound data in networkBuffer */
			return true;
		}
		else {
			/* new capacity first guess */
			int nc = 2 * networkBuffer.capacity();
			rxIncreaseNetworkBufferCapacity(nc);

			return false;
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

		getConnection().ssl_close();
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
	void increaseApplicationBufferCapacity(int capacity) {
		if(capacity > applicationBuffer.capacity()) {

			/* allocate new buffer */
			ByteBuffer extendedBuffer = ByteBuffer.allocate(capacity);

			/* copy remaining content */
			//extendedBuffer.put(applicationBuffer);
			// TODO
			
			/* replace */
			applicationBuffer = extendedBuffer;

			/* buffer is now in READ mode */
			applicationBuffer.flip();		
		}
	}


	/* </utilities> */
}
