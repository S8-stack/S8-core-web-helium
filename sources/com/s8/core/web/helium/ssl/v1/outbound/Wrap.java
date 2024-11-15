package com.s8.core.web.helium.ssl.v1.outbound;

import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;

import com.s8.core.web.helium.utilities.HeUtilities;

class Wrap implements Operation {

	@Override
	public Mode operate(SSL_Outbound out) {

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
				System.out.println("\n"); 
			}

			/*
			if(!hasProducedBytes && result.bytesProduced() > 0) {
				hasProducedBytes = true;
			}
			 */

			out.applicationBuffer.compact();

			if(result.bytesConsumed() > 0) {
				System.out.println("[SSL_Outbound] " + out.name + " : begin transfer"); 
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

				case OK: 
					out.pushOp(new Wrap());
					return Mode.CONTINUE; // continue

				case BUFFER_UNDERFLOW: 
					out.pushOp(new HandleApplicationBufferUnderflow()); 
					out.pushOp(new Wrap()); 
					return Mode.CONTINUE; // continue

				case BUFFER_OVERFLOW: 
					out.pushOp(new HandleNetworkBufferOverflow()); // will trigger send if necessary
					return Mode.CONTINUE; // continue

				case CLOSED: 
					out.pushOp(new Close());
					return Mode.CONTINUE; // continue

				default: return Mode.CONTINUE; // continue
				}


			case NEED_UNWRAP:
			case NEED_UNWRAP_AGAIN: 

				switch(result.getStatus()) {

				/* before switching to unwrap, release what has been written */
				case OK: 
					out.inbound.ssl_unwrap();
					out.pushOp(new Wrap()); 

					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					if(out.networkBuffer.position() > 0) {  return Mode.STOP_AND_SEND; }
					else { return Mode.CONTINUE; /* continue */  }


				case BUFFER_UNDERFLOW: 
					out.inbound.ssl_unwrap();
					out.pushOp(new HandleApplicationBufferUnderflow()); 

					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					if(out.networkBuffer.position() > 0) {  return Mode.STOP_AND_SEND; }
					else { return Mode.CONTINUE; /* continue */ }

				case BUFFER_OVERFLOW: 
					out.inbound.ssl_unwrap();
					out.pushOp(new HandleNetworkBufferOverflow());

					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					if(out.networkBuffer.position() > 0) {  return Mode.STOP_AND_SEND; }
					else { return Mode.CONTINUE; /* continue */  }

				case CLOSED: 
					out.pushOp(new Close()); 
					return Mode.STOP; // continue

				default: return Mode.CONTINUE; // continue
				}


				/*
				 * (From java doc): The SSLEngine needs the results of one (or more) delegated
				 * tasks before handshaking can continue.
				 */
			case NEED_TASK:

				/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
				if(out.networkBuffer.position() > 0) { out.send(); }


				switch(result.getStatus()) {

				case OK: 
					out.pushOp(new RunDelegatedTask());
					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					if(out.networkBuffer.position() > 0) {  return Mode.STOP_AND_SEND; }
					else { return Mode.CONTINUE; /* continue */  }

				case BUFFER_UNDERFLOW: 
					out.pushOp(new HandleApplicationBufferUnderflow()); 
					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					if(out.networkBuffer.position() > 0) {  return Mode.STOP_AND_SEND; }
					else { return Mode.CONTINUE; /* continue */  }

				case BUFFER_OVERFLOW: 
					out.pushOp(new HandleNetworkBufferOverflow());
					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					if(out.networkBuffer.position() > 0) {  return Mode.STOP_AND_SEND; }
					else { return Mode.CONTINUE; /* continue */  }

				case CLOSED: 
					out.pushOp(new Close());
					return Mode.STOP; // continue

				default: return Mode.CONTINUE; // continue
				}


				/*
				 * From java doc: The SSLEngine has just finished handshaking.
				 */
			case FINISHED: 

				switch(result.getStatus()) {

				case OK: 
					out.pushOp(new Wrap());
					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					if(out.networkBuffer.position() > 0) {  return Mode.STOP_AND_SEND; }
					else { return Mode.CONTINUE; /* continue */  }

				case BUFFER_UNDERFLOW: 
					out.pushOp(new HandleApplicationBufferUnderflow());
					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					if(out.networkBuffer.position() > 0) {  return Mode.STOP_AND_SEND; }
					else { return Mode.CONTINUE; /* continue */  }

				case BUFFER_OVERFLOW: 
					out.pushOp(new HandleNetworkBufferOverflow());
					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					if(out.networkBuffer.position() > 0) {  return Mode.STOP_AND_SEND; }
					else { return Mode.CONTINUE; /* continue */  }

				case CLOSED: 
					out.pushOp(new Close());
					return Mode.STOP; // continue

				default: return Mode.CONTINUE; // continue
				}	

				// -> continue to next case

			case NOT_HANDSHAKING:

				switch(result.getStatus()) {

				case OK: 
					if(out.applicationBuffer.position() > 0) { 
						out.pushOp(new Wrap());	
					}
					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					if(out.networkBuffer.position() > 0) {  return Mode.STOP_AND_SEND; }
					else { return Mode.CONTINUE; /* continue */  }

				case BUFFER_UNDERFLOW: 
					out.pushOp(new HandleApplicationBufferUnderflow());
					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					if(out.networkBuffer.position() > 0) {  return Mode.STOP_AND_SEND; }
					else { return Mode.CONTINUE; /* continue */  }

				case BUFFER_OVERFLOW: 
					out.pushOp(new HandleNetworkBufferOverflow());
					/* for any other task than WRAP (accumulating bytes to be sent) send immediately what's possible */
					if(out.networkBuffer.position() > 0) {  return Mode.STOP_AND_SEND; }
					else { return Mode.CONTINUE; /* continue */  }

				case CLOSED: 
					out.pushOp(new Close());
					return Mode.STOP; // continue

				default: return Mode.CONTINUE; // continue
				}	


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
			return Mode.CONTINUE;
		}
	}

}
