package com.s8.stack.arch.helium.ssl.inbound;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;

import com.s8.stack.arch.helium.ssl.inbound.SSL_Inbound.Flow;


/**
 * 
 * @author pierreconvert
 *
 */
class Unwrapping extends Mode {

	
	public Unwrapping() {
		super();
	}
	
	
	@Override
	public String advertise() {
		return "is unwrapping...";
	}




	@Override
	public void run(Flow flow) {

		try {

			// if nothing to read in network buffer, go back to pulling
			/*
				if(!networkBuffer.hasRemaining()) {
					return pulling.new Task();
				}
			 */

			SSLEngineResult	result = flow.unwrap();

			if(flow.isVerbose()) {
				System.out.println(flow.getName()+": "+result);
			}

			// drain as soon as bytes available
			if(result.bytesProduced()>0) {
				flow.drain();
			}

			switch(result.getHandshakeStatus()) {

			/*
			 * From javadoc -> The SSLEngine needs to receive data from the remote side
			 * before handshaking can continue.
			 */
			case NEED_UNWRAP: 

				switch(result.getStatus()) {

				case BUFFER_UNDERFLOW: handleBufferUnderflow(flow); break;

				case BUFFER_OVERFLOW: handleBufferOverflow(flow); break;

				// this side has been closed, so initiate closing
				case CLOSED: 
					flow.then(new Closing());
					break;

					// everything is fine, so process normally
				case OK: 
					flow.again(); 
					break;
				}

				break; // </NEED_WRAP>

				/*
				 * From java doc: The SSLEngine needs to unwrap before handshaking can continue.
				 */
			case NEED_UNWRAP_AGAIN: 

				switch(result.getStatus()) {

				case BUFFER_UNDERFLOW: handleBufferUnderflow(flow); break;

				case BUFFER_OVERFLOW: handleBufferOverflow(flow); break;

				// this side has been closed, so initiate closing
				case CLOSED: 
					flow.then(new Closing());
					break;

					// then(this); keep on unwrapping
				case OK: 
					flow.again();
					break;
				}
				break; // </NEED_UNWRAP_AGAIN>

				/*
				 * (From java doc): The SSLEngine must send data to the remote side before
				 * handshaking can continue, so SSLEngine.wrap() should be called.
				 */
			case NEED_WRAP: 

				switch(result.getStatus()) {

				// this side has been closed, so initiate closing
				case CLOSED: 
					flow.then(new Closing());
					break;

				case BUFFER_UNDERFLOW: // ignored since WRAP is required

				case BUFFER_OVERFLOW: // ignored since WRAP is required

				case OK: // everything is fine, so process normally
					
					flow.wrap(); // trigger wrap
					flow.stop();
					break;

				}
				break; // </NEED_WRAP>

				/*
				 * (From java doc): The SSLEngine needs the results of one (or more) delegated
				 * tasks before handshaking can continue.
				 */
			case NEED_TASK: 

				switch(result.getStatus()) {

				case BUFFER_UNDERFLOW: handleBufferUnderflow(flow); break;

				case BUFFER_OVERFLOW: handleBufferOverflow(flow); break; 

				// this side has been closed, so initiate closing
				case CLOSED:
					flow.then(new Closing());
					break;

				case OK: 
					flow.then(new RunningDelegates(this)); 
					break;
				}
				break; // </NEED_TASK>

				/*
				 * From java doc: The SSLEngine has just finished handshaking.
				 */
			case FINISHED:

				/*
				 * End of handshaking, start independent working of inbound/outbound. Since
				 * Inbound MIGHT have been left in idle mode, wake it up to ensure it is active
				 */
				flow.wrap();


				// -> continue to next case

			case NOT_HANDSHAKING: 

				switch(result.getStatus()) {

				case BUFFER_UNDERFLOW: handleBufferUnderflow(flow); break;

				case BUFFER_OVERFLOW: handleBufferOverflow(flow); break;

				// this side has been closed, so initiate closing
				case CLOSED: 
					flow.then(new Closing());
					break;

				// everything is fine, so process normally
				case OK: 
					flow.then(this); // no effect, for clarity purposes
					break;
				}
				break; // </NOT_HANDSHAKING>
			}
		}
		catch (SSLException exception) {
			
			// analyze exception as much as possible
			analyzeException(flow, exception);
			
			// close
			flow.then(new Closing());
		}
	}


	/**
	 * <p>
	 * (From javadoc): The SSLEngine was not able to unwrap the incoming data
	 * because there were not enough source bytes available to make a complete
	 * packet.
	 * </p>
	 * <p>
	 * Two reasons are possible:
	 * </p>
	 * <ul>
	 * <li>Not enough bytes pulled from the network -> need to pull</li>
	 * <li>Not enough space in the network incoming buffer -> need to increase size
	 * and retry to pull to fill</li>
	 * </ul>
	 * @throws IOException 
	 */
	private void handleBufferUnderflow(Flow sequence) throws SSLException {

		/* 
		 * networkInput seems to be sufficiently filled, so must be under-sized 
		 */
		if(sequence.isNetworkInputHalfFilled()) {
			sequence.doubleNetworkInputCapacity();
		}

		/*
		 * In any case, just need to pull more bytes from network 
		 * (/!\ without flashing current ones) and come back to unwrap
		 */
		// asynchronous, so stop unwrapping and resume on AIO completion

		// need more data, so pull and then come back to this mode
		sequence.pull(this);
		
		// stop process
		sequence.stop();
	}


	/**
	 * <p>(from javadoc) SSLEngine was not able to process the operation because there 
	 * are not enough bytes available in the destination buffer 
	 * (ApplicationInput) to hold the result.
	 * </p>
	 * <p>
	 * </p>
	 * @throws SSLException 
	 */
	public void handleBufferOverflow(Flow inbound) throws SSLException {

		// so double destination buffer ...
		doubleApplicationInputBufferCapacity(inbound);

	}



	


	private void doubleApplicationInputBufferCapacity(Flow flow) throws SSLException {
		ByteBuffer applicationBuffer = flow.getApplicationBuffer();
		int increasedSize = 2 * applicationBuffer.capacity();
		if (flow.isVerbose()) {
			System.out
			.println("[SSL/" + flow.getName() + "] " + "Application input buffer capacity increased to " + increasedSize);
		}

		if (increasedSize > 4 * flow.getEngine().getSession().getApplicationBufferSize()) {
			throw new SSLException(
					"[SSL_Inbound] Application buffer capacity is now "
							+ "4x getApplicationBufferSize. " + "Seen as excessive");
		}



		ByteBuffer extendedBuffer = flow.resizeApplicationBuffer(increasedSize);
		applicationBuffer.flip();
		extendedBuffer.put(applicationBuffer);
	}
	
	
	private void analyzeException(Flow flow, SSLException exception) {
		exception.printStackTrace();
		if(flow.isVerbose()) {
			ByteBuffer networkBuffer = flow.getNetworkBuffer();
			int nBytes = networkBuffer.limit();
			networkBuffer.position(0);
			byte[] bytes = new byte[nBytes];
			networkBuffer.get(bytes);
			
			if(flow.isVerbose()) {
				System.out.println("Try casting to plain text: "+new String(bytes));	
			}
		}
	}
}
