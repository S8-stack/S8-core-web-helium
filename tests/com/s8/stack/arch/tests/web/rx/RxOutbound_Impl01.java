package com.s8.stack.arch.tests.web.rx;

import com.s8.core.web.helium.rx.RxConnection;
import com.s8.core.web.helium.rx.RxOutbound;
import com.s8.core.web.helium.rx.RxWebConfiguration;

public abstract class RxOutbound_Impl01 extends RxOutbound {

	RxConnection connection;
	
	public RxOutbound_Impl01(String name, int capacity, RxWebConfiguration configuration) {
		super(name, capacity, configuration);
	}


	@Override
	public RxConnection getConnection() {
		return connection;
	}

}
