package com.s8.stack.arch.helium.ssl;

import javax.net.ssl.SSLContext;

import com.s8.stack.arch.helium.rx.RxEndpoint;

public interface SSL_Endpoint extends RxEndpoint {
	
	
	@Override
	public SSL_WebConfiguration getWebConfiguration();

	
	public SSLContext ssl_getContext();

}
