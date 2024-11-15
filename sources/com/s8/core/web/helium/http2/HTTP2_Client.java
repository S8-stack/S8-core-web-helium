package com.s8.core.web.helium.http2;

import com.s8.core.web.helium.http2.hpack.HPACK_Context;
import com.s8.core.web.helium.ssl.v1.SSL_Client;

public abstract class HTTP2_Client extends SSL_Client implements HTTP2_Endpoint {
	
	/**
	 * HPACK context
	 */
	private HPACK_Context HPACK_context;
	

	public HTTP2_Client() throws Exception {
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
