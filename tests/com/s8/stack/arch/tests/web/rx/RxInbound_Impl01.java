package com.s8.stack.arch.tests.web.rx;

import com.s8.core.web.helium.rx.RxConnection;
import com.s8.core.web.helium.rx.RxInbound;
import com.s8.core.web.helium.rx.RxWebConfiguration;

public abstract class RxInbound_Impl01 extends RxInbound {

	RxConnection connection;
	
	public RxInbound_Impl01(int capacity, RxWebConfiguration configuration) {
		super(capacity, configuration);
	}


	@Override
	public RxConnection getConnection() {
		return connection;
	}

}
