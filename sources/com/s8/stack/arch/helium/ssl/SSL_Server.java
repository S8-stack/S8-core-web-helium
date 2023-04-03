package com.s8.stack.arch.helium.ssl;

import javax.net.ssl.SSLContext;

import com.s8.stack.arch.helium.rx.RxServer;

public abstract class SSL_Server extends RxServer implements SSL_Endpoint {

	private SSLContext SSL_context;

	public SSL_Server() {
		super();
	}

	

	
	@Override
	public void start() throws Exception {
		
		startSSL();
		
		// start (RX) lower level
		startRxLayer();
		
	}

	public void startSSL() throws Exception {
		
		// start SSL level
		SSL_WebConfiguration configuration = getWebConfiguration();
		SSL_context = SSL_Module.createContext(configuration);

	}


	@Override
	public SSLContext ssl_getContext() {
		return SSL_context;
	}


	/**
	 * Up-casting definition method
	 * @return
	 */
	@Override
	public abstract SSL_WebConfiguration getWebConfiguration();
}
