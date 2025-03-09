package com.s8.stack.arch.tests.web.ssl;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.s8.core.web.helium.ssl.SSL_Connection;
import com.s8.core.web.helium.ssl.SSL_Endpoint;
import com.s8.core.web.helium.ssl.SSL_Inbound;
import com.s8.core.web.helium.ssl.SSL_Outbound;

public class SSL_Connection_Impl02 extends SSL_Connection {

	
	
	private SSL_Endpoint endpoint;
	private SSL_Inbound inbound;
	
	
	private SSL_Outbound outbound;
	
	
	public SSL_Connection_Impl02(SSL_Endpoint endpoint, SelectionKey key, SocketChannel channel, 
			SSL_Inbound_Impl02 inbound,
			SSL_Outbound_Impl02 outbound) throws IOException {
		super(key, channel);
		
		this.endpoint = endpoint;
		
		this.inbound = inbound;
		inbound.connection = this;
		
		this.outbound = outbound;
		outbound.connection = this;
		
		ssl_initialize(endpoint.getWebConfiguration());
		
	}


	@Override
	public SSL_Inbound getInbound() {
		return inbound;
	}


	@Override
	public SSL_Outbound getOutbound() {
		return outbound;
	}


	@Override
	public SSL_Endpoint getEndpoint() {
		return endpoint;
	}


	@Override
	public void close() {
		ssl_close();
	}

}
