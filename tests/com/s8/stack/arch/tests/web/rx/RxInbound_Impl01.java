package com.s8.stack.arch.tests.web.rx;

import java.io.IOException;

import com.s8.core.web.helium.rx.RxConnection;
import com.s8.core.web.helium.rx.RxInbound;
import com.s8.core.web.helium.rx.RxWebConfiguration;

public abstract class RxInbound_Impl01 extends RxInbound {
	

	/**
	 * Typical required NETWORK_INPUT_STARTING_CAPACITY is 16709. Instead, we add 
	 * security margin up to: 2^14+2^10 = 17408
	 */
	public final static int NETWORK_INPUT_STARTING_CAPACITY = 17408;

	RxConnection connection;
	
	public RxInbound_Impl01(String name, RxWebConfiguration configuration) {
		super(name, configuration);
		
		initializeNetworkBuffer(NETWORK_INPUT_STARTING_CAPACITY);
	}


	@Override
	public RxConnection getConnection() {
		return connection;
	}

	@Override
	public void onRxRemotelyClosed() {
	}

	@Override
	public void onRxReceptionFailed(IOException exception) {
	}
}
