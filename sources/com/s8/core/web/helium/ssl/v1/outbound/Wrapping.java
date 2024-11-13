package com.s8.core.web.helium.ssl.v1.outbound;

import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;

import com.s8.core.web.helium.ssl.v1.SSL_Outbound;
import com.s8.core.web.helium.ssl.v1.SSL_Outbound.Flow;

/**
 * 
 * @author pc
 *
 */
class Wrapping extends Mode {


	//public final static Wrapping MODE = new Wrapping();

	public Wrapping() {
		super();
	}

	@Override
	public String declare() {
		return "is wrapping...";
	}


	@Override
	public void run(SSL_Outbound.Flow flow) {

		
	}


	private void handleBufferOverflow(Flow process) throws SSLException {
		/* Network output is not even half-filled, so assume that it is 
		 * under-sized 
		 */
		if(!process.isNetworkBufferHalfFilled()) {
			process.doubleNetworkBufferCapacity();
			//outbound.then(this); -> retry wrapping
		}
		/*
		 * Network output is almost filled, so best solution is to send. But since we
		 * are in handshaking phase, must return to unsafeWrap once networkBuffer bytes have
		 * been written out
		 */
		else {

			/* 
			 *  NEED_WRAP, so ask to retry wrapping after pushing
			 *  process is terminated here, another process will be restarted
			 */
			process.push();
		}
	}



	

}