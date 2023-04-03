package com.s8.stack.arch.helium.ssl.outbound;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

import com.s8.stack.arch.helium.rx.NetworkBufferResizer;
import com.s8.stack.arch.helium.rx.RxOutbound;
import com.s8.stack.arch.helium.ssl.SSL_Connection;
import com.s8.stack.arch.helium.ssl.SSL_WebConfiguration;
import com.s8.stack.arch.helium.ssl.inbound.SSL_Inbound;


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

	private String name;

	private SSLEngine engine;

	private ByteBuffer applicationBuffer;

	private SSL_Inbound inbound;



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


	private boolean SSL_isVerbose;
	
	
	
	private Flow flow;

	private Mode callback;

	/**
	 * 
	 * @param channel
	 */
	public SSL_Outbound(SSL_WebConfiguration configuration) {
		super(NETWORK_OUTPUT_STARTING_CAPACITY, configuration);


		this.SSL_isVerbose = configuration.SSL_isVerbose;
		
		
		/* <buffers> */

		/* 
		 * Left in read mode outside retrieve state. So initialize with nothing to read
		 */
		applicationBuffer = ByteBuffer.allocate(APPLICATION_OUTPUT_STARTING_CAPACITY);
		applicationBuffer.position(0);
		applicationBuffer.limit(0);

		/* </buffer> */
	}

	@Override
	public abstract SSL_Connection getConnection();

	@Override
	public void onRxSending(ByteBuffer networkBuffer, NetworkBufferResizer resizer) throws IOException {
		Mode startMode = callback!=null?callback:new Wrapping();
		callback = null; // reset callback
		flow = new Flow(networkBuffer, resizer, startMode);
		flow.start();
		
		if(flow!=null && flow.isExhausted()) {
			flow = null;
		}
	}

	@Override
	public void onRxRemotelyClosed(ByteBuffer networkBuffer) {
		getConnection().isClosed = true;
		new Flow(networkBuffer, null, new Closing()).start();
	}

	@Override
	public void onRxFailed(ByteBuffer networkBuffer, IOException exception) {
		if(SSL_isVerbose) {
			exception.printStackTrace();
		}
		callback = null; // reset callback
		getConnection().isClosed = true;
		new Flow(networkBuffer, null, new ShuttingDown()).start();
	}


	public void SSL_bind(SSL_Connection connection) {
		
		this.engine = connection.ssl_getEngine();

		
		this.inbound = connection.getInbound();
		name = connection.getName()+".outbound";
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
	 * @throws IOException 
	 * 
	 */
	public void wrap() {
		
		if(SSL_isVerbose) {
			System.out.println(name+" wrapping required");
		}
		
		if(flow!=null) {
			flow.then(new Wrapping());
			flow.start();
			if(flow !=null && flow.isExhausted()) {
				flow = null;
			}
		}
		else {

			// clear callback, so we'll start from fresh with a Wrapping
			callback = null;
			
			// trigger sending
			send();	
		}
		
	}


	class Flow {

		
		private ByteBuffer networkBuffer;
		
		private NetworkBufferResizer networkBufferResizer;
		
		private boolean isNextStepDefined;

		// Next mode to be played
		private Mode mode;

		//Must be reset after use
		private boolean isRunning = false;

		public Flow(ByteBuffer networkBuffer, NetworkBufferResizer networkBufferResizer, Mode mode) {
			super();
			this.networkBuffer = networkBuffer;
			this.networkBufferResizer = networkBufferResizer;
			this.mode = mode;
		}

		public boolean isExhausted() {
			return !networkBuffer.hasRemaining();
		}

		public void start() {

			// reset pushing flag
			isRunning = true;

			/*
			 * Note: Even if nothing has been written, we'll add so new bytes before retrying
			 */
			while(isRunning) {

				mode.advertise(this);

				mode.run(this);

				if(!isNextStepDefined) {
					throw new RuntimeException("No next step defined");
				}
			}

			if(SSL_isVerbose) {
				System.out.println(name+": is exiting process...");
			}
		}



		public void pump() {
			/* Application buffer is left in read mode (to be able to perform wrap). */
			applicationBuffer.compact();

			/* peform the "pumping" operation */
			SSL_onSending(applicationBuffer);

			/* return to read mode */
			applicationBuffer.flip();
		}


		public void notifyHandshakingCompleted() {
			ssl_onHandshakingCompleted();
		}


		
		public void unwrap() {
			if(SSL_isVerbose) {
				System.out.println("\t--->"+name+" is requesting unwrap...");	
			}

			// trigger unwrapping
			inbound.unwrap();
		}
		


		
		/**
		 * <p>
		 * <b>Important notice</b>: ByteBuffer buffer (as retrieved by
		 * <code>getNetworkBuffer()</code> method) is passed in write mode state.
		 * </p>
		 * 
		 * @return the network buffer
		 */
		public ByteBuffer getNetworkBuffer() {
			return networkBuffer;
		}

		/**
		 * Resize 
		 * @param capacity
		 * @return
		 * @throws SSLException 
		 */
		public ByteBuffer resizeNetworkBuffer(int capacity) throws SSLException {
			
			if(networkBufferResizer==null) {
				throw new SSLException("Network byte buffer resizer is not availbale for this flow.");
			}
			
			return networkBufferResizer.resizeNetworkBuffer(capacity);
		}

		public ByteBuffer getApplicationBuffer() { 
			return applicationBuffer;
		}

		public String getName() { 
			return name; 
		}

		public SSLEngine getEngine() { 
			return engine; 
		}

		public SSL_Connection getConnection() { 
			return SSL_Outbound.this.getConnection();
		}

		public boolean isVerbose() { 
			return SSL_isVerbose;
		}


		public void stop() {
			isNextStepDefined = true;
			isRunning = false;
		}

		public void again() {
			isNextStepDefined = true;
		}

		public void then(Mode mode) {
			isNextStepDefined = true;
			this.mode = mode;
		}

		/**
		 * Push and stop
		 * @throws IOException 
		 */
		public void push() {
			send();
			isNextStepDefined = true;
			isRunning = false;
		}


		/**
		 * Push and restart asynchronously with the mode passed as argument
		 * @param mode
		 * @throws IOException 
		 */
		public void push(Mode mode) {
			send();
			isNextStepDefined = true;
			isRunning = false;
			callback = mode;
		}
		

		public boolean isNetworkBufferHalfFilled() {
			return networkBuffer.position()>networkBuffer.capacity()/2;
		}

		
		/**
		 * Double buffer capacity
		 * 
		 * @param engine
		 * @throws SSLException
		 */
		public void doubleNetworkBufferCapacity() throws SSLException {


			int increasedCapacity = 2 * networkBuffer.capacity();
			if (SSL_isVerbose) {
				System.out.println("[SSL] "+name
				+ " : Network output buffer capacity increased to " + increasedCapacity);
			}
			if (increasedCapacity > 4 * engine.getSession().getPacketBufferSize()) {
				throw new SSLException(
						"[SSL_Inbound] networ output capacity is now 4x getPacketBufferSize. " + "Seen as excessive");
			}

			// publish new network buffer
			ByteBuffer extendedBuffer = networkBufferResizer.resizeNetworkBuffer(increasedCapacity);

			networkBuffer.flip();
			extendedBuffer.put(networkBuffer);
			
			networkBuffer = extendedBuffer;
			//extendedBuffer.compact(); // left in write mode
			again();
		}
	}



}
