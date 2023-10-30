package com.s8.stack.arch.tests.web.rx;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.s8.core.web.helium.rx.RxConnection;
import com.s8.core.web.helium.rx.RxEndpoint;
import com.s8.core.web.helium.rx.RxInbound;
import com.s8.core.web.helium.rx.RxOutbound;

public class RxConnection_Impl01 extends RxConnection {
	
	private RxInbound inbound;
	
	private RxOutbound outbound;

	private RxEndpoint endpoint;
	
	
	public RxConnection_Impl01(
			RxEndpoint endpoint,
			SocketChannel socketChannel,
			RxInbound_Impl01 inbound,
			RxOutbound_Impl01 outbound) throws IOException {
		super(socketChannel);
		
		this.endpoint = endpoint;
		this.inbound = inbound;
		inbound.connection = this;
		
		this.outbound = outbound;
		outbound.connection = this;
		
		Rx_initialize(endpoint.getWebConfiguration());
		
	}
	
	
	@Override
	public RxEndpoint getEndpoint() {
		return endpoint;
	}
	

	@Override
	public RxInbound getInbound() {
		return inbound;
	}

	@Override
	public RxOutbound getOutbound() {
		return outbound;
	}
}
