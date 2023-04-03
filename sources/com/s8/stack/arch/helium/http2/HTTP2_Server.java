package com.s8.stack.arch.helium.http2;

import com.s8.stack.arch.helium.http2.hpack.HPACK_Context;
import com.s8.stack.arch.helium.ssl.SSL_Server;


/**
 * 
 * @author pc
 *
 */
public abstract class HTTP2_Server extends SSL_Server implements HTTP2_Endpoint {
	
	/**
	 * HPACK context
	 */
	private HPACK_Context HPACK_context;
	
	
	public HTTP2_Server() throws Exception {
		super();
		
	}
	

	@Override
	public void start() throws Exception {
		startHTTP2();
		startSSL();
		startRxLayer();
	}
	
	public void startHTTP2() throws Exception {
		
		// initialize
		HPACK_context = new HPACK_Context(getWebConfiguration().isHPACKVerbose);
		
	}
	
	@Override
	public HPACK_Context HPACK_getContext() {
		return HPACK_context;
	}

	@Override
	public abstract HTTP2_WebConfiguration getWebConfiguration();
	
}
