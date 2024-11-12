package com.s8.core.web.helium.ssl.v1.inbound;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;

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

	String name;

	SSLEngine engine;

	ByteBuffer applicationBuffer;

	SSL_Outbound outbound;

	boolean SSL_isVerbose;

	
	Flow flow;
	
	Mode callback;


	/**
	 * 
	 * @param channel
	 */
	public SSL_Inbound(SSL_WebConfiguration configuration) {
		super(NETWORK_INPUT_STARTING_CAPACITY, configuration);

		this.SSL_isVerbose = configuration.isSSLVerbose();
		
		/* <buffers> */

		applicationBuffer = ByteBuffer.allocate(APPLICATION_INPUT_STARTING_CAPACITY);
		// left in write mode

		/* </buffer> */
	}

	@Override
	public abstract SSL_Connection getConnection();

	@Override
	public void onRxReceived() throws IOException {
		Mode startMode = callback!=null?callback:new Unwrapping();
		callback = null; // reset callback
		
		/* on fresh bytes, create new flow */
		flow = new Flow(startMode);
		flow.start();
		
		/**
		 * if flow is entirely consumed, set to null so cannot be resumed
		 */
		if(flow!=null && flow.isExhausted()) {
			flow = null;
		}
	}

	@Override
	public void onRxRemotelyClosed() throws IOException {
		getConnection().isClosed = true;
		new Flow(new Closing()).start();
	}
	

	@Override
	public void onRxReceptionFailed(IOException exception) throws IOException {
		getConnection().isClosed = true;
		new Flow(new Closing()).start();
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
		name = connection.getName()+".inbound";
	}




	/*
	public void unwrap() {
		new Process(new Unwrapping()).launch();
	}

*/



	public void unwrap() {
		
		if(SSL_isVerbose) {
			System.out.println(name+": Unwrapping required");
		}

		if(flow!=null) {
			flow.then(new Unwrapping());
			flow.start();
			
			if(flow!=null && flow.isExhausted()) {
				flow = null;
			}
		}
		else {
			// no flow so pull more bytes
			
			// clear callback so we'll start from fresh with a new unwrapping
			callback = null;

			// trigger receive
			receive();	
		}	
	}





	class Flow {


		private ByteBuffer networkBuffer;

		// Next mode to be played
		private Mode mode;

		//Must be reset after use
		private boolean isRunning = false;

		public Flow(Mode mode) {
			super();
			this.networkBuffer = SSL_Inbound.this.networkBuffer;
			this.mode = mode;
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
			}

			if(SSL_isVerbose) {
				System.out.println(name+" is exiting process...");
			}
		}



		/**
		 * ALWAYS drain to supply the upper layer with app data
		 * as EARLY as possible
		 */
		public void drain() {

			// flip buffer to prepare reading (see SSL_EndPoint.onReceived contract).
			/* application input buffer -> WRITE */
			applicationBuffer.flip();

			// apply
			// we ignore the fact that receiver can potentially read more bytes
			SSL_onReceived(applicationBuffer);

			// since endPoint.onReceived read ALL data, nothing left, so clear
			/* application input buffer -> READ */
			applicationBuffer.clear();	

		}

		

		public boolean isExhausted() {
			return !networkBuffer.hasRemaining();
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




		public ByteBuffer resizeApplicationBuffer(int increasedCapacity) {
			return (applicationBuffer = ByteBuffer.allocate(increasedCapacity));
		}

		public String getName() { 
			return name; 
		}

		public SSLEngine getEngine() { 
			return engine; 
		}

		public SSL_Connection getConnection() { 
			return SSL_Inbound.this.getConnection();
		}

		public boolean isVerbose() { 
			return SSL_isVerbose;
		}


		public void again() {

		}

		public void then(Mode mode) {
			this.mode = mode;
			isRunning = true;
		}


		public void stop() {
			isRunning = false;
		}

		/**
		 * trigger another reception
		 * @param mode the callback mode
		 * @throws IOException 
		 */
		public void pull(Mode mode) {
			callback = mode;
			receive();
		}

		
		public void wrap() {
			if(SSL_isVerbose) {
				System.out.println("\t--->"+name+" is requesting wrap...");	
			}

			// trigger wrapping
			outbound.wrap();
		}

		public ByteBuffer getApplicationBuffer() {
			return applicationBuffer;
		}
		

		public boolean isNetworkInputHalfFilled() {
			return networkBuffer.position()>networkBuffer.capacity()/2;
		}

		
		/**
		 * 
		 * @throws SSLException
		 */
		public void doubleNetworkInputCapacity() throws SSLException {

			int increasedCapacity = 2 * networkBuffer.capacity();
			if (SSL_isVerbose) {
				System.out.println("[SSL_NetworkInput] " + name + 
						" -> Network input buffer capacity increased to " 
						+ increasedCapacity);
			}
			if (increasedCapacity > 4 * engine.getSession().getPacketBufferSize()) {
				throw new SSLException(
						"[SSL_Inbound] networkInput capacity is now 4x getPacketBufferSize. " +
						"Seen as excessive");
			}

			increaseNetwordBufferCapacity(increasedCapacity);
		}


		public SSLEngineResult unwrap() throws SSLException {
			return engine.unwrap(networkBuffer, applicationBuffer);
		}
	}

}
