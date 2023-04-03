package com.s8.stack.arch.helium.http1;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.s8.stack.arch.helium.http1.messages.HTTP1_Request;
import com.s8.stack.arch.helium.rx.RxConnection;
import com.s8.stack.arch.helium.rx.RxEndpoint;

/**
 * 
 * @author pierreconvert
 *
 */
public abstract class HTTP1_Connection extends RxConnection {

	private HTTP1_Inbound inbound;
	
	private HTTP1_Outbound outbound;
	
	private HTTP1_Endpoint endpoint;
	
	public HTTP1_Connection(SocketChannel socketChannel, HTTP1_Endpoint endpoint) throws IOException {
		super(socketChannel);
		this.endpoint = endpoint;
		inbound = new HTTP1_Inbound(this, endpoint.getWebConfiguration());
		outbound = new HTTP1_Outbound(this, endpoint.getWebConfiguration());
	}

	@Override
	public HTTP1_Inbound getInbound() {
		return inbound;
	}

	@Override
	public HTTP1_Outbound getOutbound() {
		return outbound;
	}

	@Override
	public RxEndpoint getEndpoint() {
		return endpoint;
	}
	
	
	public abstract void onReceivedRequest(HTTP1_Request request);

	
	public void resume() {
		receive();	
		send();
	}
	
}
