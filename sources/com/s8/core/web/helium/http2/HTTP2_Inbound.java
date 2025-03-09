package com.s8.core.web.helium.http2;

import java.nio.ByteBuffer;

import com.s8.core.web.helium.http2.settings.HTTP2_Settings;
import com.s8.core.web.helium.http2.utilities.ReceivingPreface;
import com.s8.core.web.helium.ssl.SSL_Inbound;

public class HTTP2_Inbound extends SSL_Inbound {

	private final HTTP2_Connection connection;
	
	/** direct cache of the othe side of the connection (managed by connection) */
	HTTP2_Outbound outbound;
	
	
	private HTTP2_IOReactive state;
	
	private boolean HTTP2_isVerbose;


	
	public HTTP2_Inbound(String name, HTTP2_Connection connection, HTTP2_WebConfiguration configuration) {
		super(name, configuration);
		this.connection = connection;
		this.HTTP2_isVerbose = configuration.isHTTP2Verbose;
	}
	
	@Override
	public HTTP2_Connection getConnection() {
		return connection;
	}
	
	@Override
	public HTTP2_Outbound getOutbound() {
		return outbound;
	}
	
	
	/**
	 * 
	 * @param connection
	 */
	/*
	public void bind(HTTP2_Connection connection) {

		// Rx
		rxBind(connection);
		
		// SSL
		//ssl_bind(connection);
		
		// HTTP2
		HTTP2_bind(connection);
	}
	*/
	
	
	protected void http2_initialize() {
		if(connection.isServerSide()) {
			state = new ReceivingPreface(this);
		}
	}
	

	public void setState(HTTP2_IOReactive state) {
		this.state = state;
	}


	@Override
	public void ssl_onReinitializing() {
		http2_initialize();
	}

	
	@Override
	public void ssl_onReceived(ByteBuffer buffer) {
		try {
			while(state!=null && buffer.hasRemaining()) {
				HTTP2_Error error = state.on(buffer);
				if(error!=HTTP2_Error.NO_ERROR) {
					connection.close();
				}
			}
		}
		catch(Throwable throwable) {
			if(HTTP2_isVerbose) {
				throwable.printStackTrace();	
			}
			connection.close();
		}
	}


	public boolean isVerbose() {
		return HTTP2_isVerbose;
	}
	
	public HTTP2_Settings getEndpointSettings() {
		return connection.settings;
	}

	public HTTP2_Connection getEndpoint() {
		return connection;
	}

	public void close() {
		connection.close();
	}


}
