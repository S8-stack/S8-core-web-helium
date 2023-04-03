package com.s8.stack.arch.helium.http1;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.s8.stack.arch.helium.http1.HTTP1_IOReactive.Result;
import com.s8.stack.arch.helium.http1.messages.HTTP1_Request;
import com.s8.stack.arch.helium.rx.NetworkBufferResizer;
import com.s8.stack.arch.helium.rx.RxConnection;
import com.s8.stack.arch.helium.rx.RxInbound;

public class HTTP1_Inbound extends RxInbound {


	/**
	 * Typical required NETWORK_INPUT_STARTING_CAPACITY is 16709. Instead, we add 
	 * security margin up to: 2^14+2^10 = 17408
	 */
	public final static int NETWORK_INPUT_STARTING_CAPACITY = 17408;


	private HTTP1_Connection connection;


	/**
	 * incoming request
	 */
	private HTTP1_Request request;

	private HTTP1_IOReactive parsing;

	public HTTP1_Inbound(HTTP1_Connection connection, HTTP1_WebConfiguration configuration) {
		super(NETWORK_INPUT_STARTING_CAPACITY, configuration);
		this.connection = connection;

		this.request = new HTTP1_Request();
		this.parsing = request.parse(); 
	}


	@Override
	public void onRxReceived(ByteBuffer networkBuffer, NetworkBufferResizer resizer) {
		boolean isReceiving = true;
		while(isReceiving) {
			
			Result result = parsing.onBytes(networkBuffer);
			
			switch(result) {

			case OK:
				connection.onReceivedRequest(request);

				// initiate new request parsing
				request = new HTTP1_Request();
				parsing = request.parse(); 	
				break;

			case ERROR:
				isReceiving = false;
				connection.close();
				break;
				
			case NEED_MORE_BYTES:
				isReceiving = false;
				break;
			}
		}
	}

	@Override
	public RxConnection getConnection() {
		return connection;
	}


	@Override
	public void onRxRemotelyClosed(ByteBuffer networkBuffer) {
		connection.close();
	}


	@Override
	public void onRxReceptionFailed(ByteBuffer networkBuffer, IOException exception) {
		connection.close();
	}

}
