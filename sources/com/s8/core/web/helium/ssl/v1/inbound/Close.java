package com.s8.core.web.helium.ssl.v1.inbound;

import javax.net.ssl.SSLException;


/**
 * 
 */
class Close implements SSL_Inbound.Operation {

	@Override
	public void operate(SSL_Inbound in) {
		try {
			in.engine.closeInbound();
		} 
		catch (SSLException e) {
			//e.printStackTrace();
			// --> javax.net.ssl.SSLException: closing inbound before receiving peer's close_notify
			// We don't care...
		}


		in.engine.closeOutbound();

		in.getConnection().close();
	}

}
