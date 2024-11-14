package com.s8.core.web.helium.ssl.v1.inbound;


/**
 * 
 */
public class Drain implements SSL_Inbound.Operation {

	@Override
	public void operate(SSL_Inbound in) {
		/**
		 * ALWAYS drain to supply the upper layer with app data
		 * as EARLY as possible
		 */

			/* Trigger SSL_onReceived
				we ignore the fact that receiver can potentially read more bytes */
			in.SSL_onReceived(in.applicationBuffer);

			/* /!\ since endPoint.onReceived read ALL data, nothing left, so clear
			application input buffer -> READ */
			in.applicationBuffer.clear();	
		
	}

}
