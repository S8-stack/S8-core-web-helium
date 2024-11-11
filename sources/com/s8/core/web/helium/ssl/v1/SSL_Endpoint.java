package com.s8.core.web.helium.ssl.v1;

import javax.net.ssl.SSLContext;

import com.s8.core.web.helium.rx.RxEndpoint;

public interface SSL_Endpoint extends RxEndpoint {
	
	
	@Override
	public SSL_WebConfiguration getWebConfiguration();

	
	public SSLContext ssl_getContext();

}
