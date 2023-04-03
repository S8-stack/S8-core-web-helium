package com.s8.stack.arch.helium.ssl.inbound;

import javax.net.ssl.SSLException;

import com.s8.stack.arch.helium.ssl.inbound.SSL_Inbound.Flow;

class Closing extends Mode {


	public Closing() {
		super();
	}



	@Override
	public String advertise() {
		return "is closing";
	}


	@Override
	public void run(Flow sequence) {

		try {
			sequence.getEngine().closeInbound();
		} 
		catch (SSLException e) {
			//e.printStackTrace();
			// --> javax.net.ssl.SSLException: closing inbound before receiving peer's close_notify
			// We don't care...
		}
		sequence.getEngine().closeOutbound();

		sequence.getConnection().close();
		sequence.stop();
	}

}
