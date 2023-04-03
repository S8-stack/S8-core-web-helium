package com.s8.stack.arch.helium.http1;

import com.s8.stack.arch.helium.rx.RxEndpoint;

public interface HTTP1_Endpoint extends RxEndpoint {

	@Override
	public HTTP1_WebConfiguration getWebConfiguration();
	
}
