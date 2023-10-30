package com.s8.core.web.helium.http1;

import com.s8.core.web.helium.rx.RxEndpoint;

public interface HTTP1_Endpoint extends RxEndpoint {

	@Override
	public HTTP1_WebConfiguration getWebConfiguration();
	
}
