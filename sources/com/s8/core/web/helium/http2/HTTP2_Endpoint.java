package com.s8.core.web.helium.http2;

import com.s8.core.web.helium.http2.hpack.HPACK_Context;
import com.s8.core.web.helium.ssl.SSL_Endpoint;

public interface HTTP2_Endpoint extends SSL_Endpoint {

	@Override
	public HTTP2_WebConfiguration getWebConfiguration();

	
	public HPACK_Context HPACK_getContext();
	
	
}
