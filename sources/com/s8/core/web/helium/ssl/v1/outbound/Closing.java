package com.s8.core.web.helium.ssl.v1.outbound;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.Status;

import com.s8.core.web.helium.ssl.v1.SSL_Outbound;

import javax.net.ssl.SSLException;

/**
 * Just flush bytes, but do not require re-launching 
 * unsafeWrap after that
 * 
 * 
 */
class Closing extends Mode {

	public Closing() {
		super();
	}

	
	@Override
	public String declare() {
		return "is closing...";
	}
	
	
	@Override
	public void run(SSL_Outbound.Flow flow) {

		
	}
}
