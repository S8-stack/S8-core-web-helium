package com.s8.core.web.helium.ssl.v1.inbound;

import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;

import com.s8.core.web.helium.utilities.HeUtilities;

/**
 * 
 */
class Unwrap implements Operation {

	@Override
	public boolean operate(SSL_Inbound in) {

		try {


			/* switch application buffer into write mode */
			try {
				in.applicationBuffer.compact();	
			}
			catch(IllegalArgumentException e) {
				e.printStackTrace();
			}

			SSLEngineResult	result = in.engine.unwrap(in.networkBuffer, in.applicationBuffer);


			/* switch back application buffer into read mode */
			in.applicationBuffer.flip();

			if(in.SSL_isVerbose) { 
				System.out.println("[SSL_Inbound] " + in.name + " :"); 
				System.out.println("\tunwrap result: " + result); 
				System.out.println("\tnetwork buffer: " + HeUtilities.printInfo(in.networkBuffer)); 
			}

			// drain as soon as bytes available

			if(result.bytesProduced() > 0) {
				in.drain();
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
				case OK: in.pushOp(new Unwrap()); return true;

				/* stop the flow, because need to pump more data */
				case BUFFER_UNDERFLOW: in.pushOp(new HandleNetworkBufferUnderflow()); return true;

				/* continue wrapping, because no additional I/O call involved */
				case BUFFER_OVERFLOW: in.pushOp(new HandleApplicationBufferOverflow()); return true;

				/* this side has been closed, so initiate closing */
				case CLOSED: in.pushOp(new Close()); return true;

				default: return true;
				}

			case NEED_WRAP: 

				switch(result.getStatus()) {

				/* launch outbound side, do not add a loop */
				case OK: in.outbound.ssl_wrap(); return true;

				/* stop the flow, because need to pump more data */
				case BUFFER_UNDERFLOW: in.pushOp(new HandleNetworkBufferUnderflow()); in.outbound.ssl_wrap(); return true;

				/* continue wrapping, because no additional I/O call involved */
				case BUFFER_OVERFLOW: in.pushOp(new HandleApplicationBufferOverflow()); in.outbound.ssl_wrap(); return true;

				/* this side has been closed, so initiate closing */
				case CLOSED: in.pushOp(new Close()); return true;

				default : return true;
				}


				/*
				 * (From java doc): The SSLEngine needs the results of one (or more) delegated
				 * tasks before handshaking can continue.
				 */
			case NEED_TASK: 
				switch(result.getStatus()) {

				/* handle delegated task */
				case OK: 
					in.pushOp(new RunDelegatedTask()); return true;

					/* stop the flow, because need to pump more data */
				case BUFFER_UNDERFLOW: 
					in.pushOp(new RunDelegatedTask()); 
					in.pushOp(new HandleNetworkBufferUnderflow()); return true;

					/* continue wrapping, because no additional I/O call involved */
				case BUFFER_OVERFLOW: 
					in.pushOp(new RunDelegatedTask()); 
					in.pushOp(new HandleApplicationBufferOverflow()); return true;

					/* this side has been closed, so initiate closing */
				case CLOSED: 
					in.pushOp(new Close()); return true;

				default : return true;
				}


				/*
				 * From java doc: The SSLEngine has just finished handshaking.
				 */
			case FINISHED: 

				switch(result.getStatus()) {

				/* handle delegated task */
				case OK: in.pushOp(new Unwrap()); return true;

				/* stop the flow, because need to pump more data */
				case BUFFER_UNDERFLOW: 
					in.pushOp(new HandleNetworkBufferUnderflow());
					return true;

					/* continue wrapping, because no additional I/O call involved */
				case BUFFER_OVERFLOW: 
					in.pushOp(new HandleApplicationBufferOverflow());
					return true;

					/* this side has been closed, so initiate closing */
				case CLOSED: 
					in.pushOp(new Close());
					return true;

				default : return true;
				}

				// -> continue to next case

			case NOT_HANDSHAKING: 

				switch(result.getStatus()) {

				/* handle delegated task */
				case OK: 
					in.pushOp(new Unwrap());
					return true;

					/* stop the flow, because need to pump more data */
				case BUFFER_UNDERFLOW: 
					in.pushOp(new HandleNetworkBufferUnderflow()); 
					return true;

					/* continue wrapping, because no additional I/O call involved */
				case BUFFER_OVERFLOW: 
					in.pushOp(new HandleApplicationBufferOverflow()); 
					return true;

					/* this side has been closed, so initiate closing */
				case CLOSED: 
					in.pushOp(new Close());
					return true;

				default : return true;
				}

			default : throw new SSLException("Unsupported case : "+result.getHandshakeStatus());

			}


		}
		catch (SSLException exception) {

			// analyze exception as much as possible
			exception.printStackTrace();
			if(in.SSL_isVerbose) {
				int nBytes = in.networkBuffer.limit();
				in.networkBuffer.position(0);
				byte[] bytes = new byte[nBytes];
				in.networkBuffer.get(bytes);

				System.out.println("Try casting to plain text: "+new String(bytes));	

			}

			return true;
		}
	}

}
