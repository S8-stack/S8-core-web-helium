package com.s8.stack.arch.helium.http1;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.s8.stack.arch.helium.http1.HTTP1_IOReactive.Result;
import com.s8.stack.arch.helium.http1.messages.HTTP1_Response;
import com.s8.stack.arch.helium.rx.NetworkBufferResizer;
import com.s8.stack.arch.helium.rx.RxConnection;
import com.s8.stack.arch.helium.rx.RxOutbound;

public class HTTP1_Outbound extends RxOutbound {


	/**
	 * Typical required NETWORK_OUTPUT_STARTING_CAPACITY is 16709. Instead, we add 
	 * security margin up to: 2^14+2^10 = 17408
	 */
	public final static int NETWORK_OUTPUT_STARTING_CAPACITY = 17408;

	private HTTP1_Connection connection;
	
	private Queue<HTTP1_Response> queue;
	
	private HTTP1_IOReactive composing;
	
	public HTTP1_Outbound(HTTP1_Connection connection, HTTP1_WebConfiguration configuration) {
		super(NETWORK_OUTPUT_STARTING_CAPACITY, configuration);
		this.connection = connection;
		queue = new ConcurrentLinkedDeque<>();
	}
	
	/**
	 * 
	 * @param response
	 */
	public void push(HTTP1_Response response) {
		queue.add(response);
		
		// notify that there is data to be sent and start asynchronously
		connection.resume();
	}

	@Override
	public RxConnection getConnection() {
		return connection;
	}
	
	private void pull() {
		if(composing==null) {
			HTTP1_Response response = queue.poll();
			if(response!=null) {
				composing = response.compose();
			}
		}
	}

	@Override
	public void onRxSending(ByteBuffer networkBuffer, NetworkBufferResizer resizer) {
		pull();
		
		boolean isSending = composing!=null;
		while(isSending) {
			Result result = composing.onBytes(networkBuffer);
			switch(result) {
			case OK:
				composing = null;
				pull();
				isSending = composing!=null;
				break;
				
			case ERROR:
				isSending = false;
				connection.close();
				break;
				
			case NEED_MORE_BYTES:
				isSending = false;
				break;
			}
		}
	}

	


	@Override
	public void onRxRemotelyClosed(ByteBuffer networkBuffer) {
		connection.close();
	}

	@Override
	public void onRxFailed(ByteBuffer networkBuffer, IOException exception) {
		connection.close();	
	}

}
