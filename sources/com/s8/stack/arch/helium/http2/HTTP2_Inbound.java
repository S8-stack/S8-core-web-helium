package com.s8.stack.arch.helium.http2;

import java.nio.ByteBuffer;

import com.s8.stack.arch.helium.http2.settings.HTTP2_Settings;
import com.s8.stack.arch.helium.http2.utilities.ReceivingPreface;
import com.s8.stack.arch.helium.ssl.inbound.SSL_Inbound;

public class HTTP2_Inbound extends SSL_Inbound {

	private HTTP2_Connection connection;
	
	private HTTP2_IOReactive state;
	
	private boolean HTTP2_isVerbose;

	
	public HTTP2_Inbound(HTTP2_Connection connection, HTTP2_WebConfiguration configuration) {
		super(configuration);
		this.connection = connection;
		this.HTTP2_isVerbose = configuration.isHTTP2Verbose;
	}
	
	@Override
	public HTTP2_Connection getConnection() {
		return connection;
	}
	
	
	/**
	 * 
	 * @param connection
	 */
	public void bind(HTTP2_Connection connection) {

		// Rx
		Rx_bind(connection);
		
		// SSL
		SSL_bind(connection);
		
		// HTTP2
		HTTP2_bind(connection);
	}
	
	
	protected void HTTP2_bind(HTTP2_Connection connection) {
		if(connection.isServerSide()) {
			state = new ReceivingPreface(this);
		}
	}
	

	public void setState(HTTP2_IOReactive state) {
		this.state = state;
	}
	
	public final static int MAX_NB_FAILED_BUFFER_READING_ATTEMPTS = 1024;

	@Override
	public void SSL_onReceived(ByteBuffer buffer) {
		try {
			int count = 0;
			int newPosition, lastPosition = 0;
			boolean isReceiving = true;
			while(isReceiving && state!=null && buffer.hasRemaining()) {
				
				lastPosition = buffer.position();
				
				/* read from buffer */
				HTTP2_Error error = state.on(buffer);
				if(error!=HTTP2_Error.NO_ERROR) {
					connection.close();
				}
				
				/* check that Inbound is ACTUALLY reading buffer, if not increment counter */
				newPosition = buffer.position();
				if(newPosition == lastPosition) {
					count++;
					if(count > MAX_NB_FAILED_BUFFER_READING_ATTEMPTS) {
						isReceiving = false;
						connection.close();
						
						//boolean isVerbose = connection.getInbound().HTTP2_isVerbose;
						//if(isVerbose) {
							System.err.println("[HTTP2_Inbound] Force closing of connection: "
									+ "continuously failed to read data");
						//}
					}
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
