package com.s8.stack.arch.tests.web.rx;

import com.s8.stack.arch.helium.rx.RxConnection;
import com.s8.stack.arch.helium.rx.RxOutbound;
import com.s8.stack.arch.helium.rx.RxWebConfiguration;

public abstract class RxOutbound_Impl01 extends RxOutbound {

	RxConnection connection;
	
	public RxOutbound_Impl01(int capacity, RxWebConfiguration configuration) {
		super(capacity, configuration);
	}


	@Override
	public RxConnection getConnection() {
		return connection;
	}

}
