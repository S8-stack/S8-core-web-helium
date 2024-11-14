package com.s8.core.web.helium.ssl.v1.outbound;

class Flush implements SSL_Outbound.Operation {

	@Override
	public void operate(SSL_Outbound out) {


		// if there is actually new bytes, send them
		if(out.networkBuffer.position()>0) {


			/*
			 *  stop this process here (trigger sending)
			 * setup callback as this to continue on this mode asynchronously
			 */
			out.send();
		}
	}

}
