package com.s8.core.web.helium.ssl.v1.outbound;

import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;

import com.s8.core.web.helium.utilities.HeUtilities;

class Wrap implements SSL_Outbound.Operation {

	@Override
	public void operate(SSL_Outbound out) {
		
		/* <retrieve> */

		/*
		 * Wrapping is greedy, i.e. it retrieves as much application data as possible
		 * before retrying to push to the network
		 */
		out.pump();

		/* </retrieve> */




		try {

			/* Actually wrapping... */
			out.applicationBuffer.flip();

			if(out.SSL_isVerbose) { 
				System.out.println("\t\t * application buffer: " + HeUtilities.printInfo(out.applicationBuffer)); 
				System.out.println("\t\t * network buffer: " + HeUtilities.printInfo(out.networkBuffer)); 
			}
			
			
			SSLEngineResult result = out.engine.wrap(out.applicationBuffer, out.networkBuffer);

			if(out.SSL_isVerbose) { 
				System.out.println("[SSL_Outbound] " + out.name + " :"); 
				System.out.println("\tunwrap result: " + result); 
				System.out.println("\tnetwork buffer: " + HeUtilities.printInfo(out.networkBuffer)); 
			}

			/*
			if(!hasProducedBytes && result.bytesProduced() > 0) {
				hasProducedBytes = true;
			}
			*/

			out.applicationBuffer.compact();


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

				case OK: out.pushOp(new Wrap()); break;

				case BUFFER_UNDERFLOW: 
					out.pushOp(new HandleApplicationBufferUnderflow()); 
					out.pushOp(new Wrap()); 
					break;

				case BUFFER_OVERFLOW: out.pushOp(new HandleNetworkBufferOverflow()); break;

				case CLOSED: out.pushOp(new Close()); break;
				}
				break;


			case NEED_UNWRAP:
			case NEED_UNWRAP_AGAIN: 

				/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
				if(out.networkBuffer.position() > 0) { out.send(); }

				switch(result.getStatus()) {

				/* before switching to unwrap, release what has been written */
				case OK: out.inbound.ssl_unwrap(); break;

				case BUFFER_UNDERFLOW: 
					out.pushOp(new HandleApplicationBufferUnderflow()); 
					out.inbound.ssl_unwrap(); 
					break;

				case BUFFER_OVERFLOW: 
					out.pushOp(new HandleNetworkBufferOverflow()); 
					out.inbound.ssl_unwrap(); 
					break;

				case CLOSED: 
					out.pushOp(new Close()); 
					break;
				}
				break;


				/*
				 * (From java doc): The SSLEngine needs the results of one (or more) delegated
				 * tasks before handshaking can continue.
				 */
			case NEED_TASK:

				/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
				if(out.networkBuffer.position() > 0) { out.send(); }


				switch(result.getStatus()) {

				case OK: 
					if(result.bytesConsumed() > 0 || result.bytesProduced() > 0) {
						out.pushOp(new RunDelegatedTask()); 	
					}
					break;

				case BUFFER_UNDERFLOW: 
					out.pushOp(new HandleApplicationBufferUnderflow()); 
					out.pushOp(new RunDelegatedTask());
					break;

				case BUFFER_OVERFLOW: 
					out.pushOp(new HandleNetworkBufferOverflow());
					out.pushOp(new RunDelegatedTask()); break;

				case CLOSED: out.pushOp(new Close()); break;
				
				}
				break;


				/*
				 * From java doc: The SSLEngine has just finished handshaking.
				 */
			case FINISHED: 

				/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
				if(out.networkBuffer.position() > 0) { out.send(); }

				switch(result.getStatus()) {

				case OK: out.pushOp(new Wrap()); break;

				case BUFFER_UNDERFLOW: out.pushOp(new HandleApplicationBufferUnderflow()); break;

				case BUFFER_OVERFLOW: out.pushOp(new HandleNetworkBufferOverflow()); break;

				case CLOSED: out.pushOp(new Close()); break;
				}
				break;


				// -> continue to next case

			case NOT_HANDSHAKING:

				/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
				if(out.networkBuffer.position() > 0) { out.send(); }

				switch(result.getStatus()) {

				case OK: out.pump(); break;

				case BUFFER_UNDERFLOW: out.pushOp(new HandleApplicationBufferUnderflow()); out.pump(); break;

				case BUFFER_OVERFLOW: out.pushOp(new HandleNetworkBufferOverflow()); out.pump(); break;

				case CLOSED: out.pushOp(new Close()); break;
				}
				break;

			default : throw new SSLException("Unsupported case : "+result.getHandshakeStatus());

			}




		}
		catch (SSLException exception) {	
			if(out.SSL_isVerbose) {
				System.out.println("[Wrapping]: SSL_Exception causes endpoint to close.");
				exception.printStackTrace();
			}
			// Everything went wrong, so try launching the closing procedure
			out.pushOp(new Close());
		}
	}

}
