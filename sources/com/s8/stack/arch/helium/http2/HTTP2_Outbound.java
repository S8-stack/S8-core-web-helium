package com.s8.stack.arch.helium.http2;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.s8.stack.arch.helium.http2.frames.HTTP2_Frame;
import com.s8.stack.arch.helium.http2.frames.SendingFrameHeader;
import com.s8.stack.arch.helium.http2.utilities.SendingPreface;
import com.s8.stack.arch.helium.ssl.outbound.SSL_Outbound;

public class HTTP2_Outbound extends SSL_Outbound {
	
	private boolean isVerbose;

	private HTTP2_IOReactive state;
	
	private Queue<HTTP2_Frame> queue;
	
	private boolean isStateAlive;
	
	private HTTP2_Connection connection;
	
	public HTTP2_Outbound(HTTP2_Connection connection, HTTP2_WebConfiguration configuration) {
		super(configuration);
		this.connection = connection;
		/* 
		 * Lock free queue implementing Atomic control new algos
		 */
		queue = new ConcurrentLinkedQueue<>();
		
	}


	@Override
	public HTTP2_Connection getConnection() {
		return connection;
	}
	
	
	
	public void HTTP2_bind(HTTP2_Connection connection) {
		this.isVerbose = connection.getEndpoint().getWebConfiguration().isHTTP2Verbose;
		
		if(connection.isServerSide()) {
			setState(new SendingFrameHeader(this));
		}
		else {
			setState(new SendingPreface(this));
		}
	}
	
	
	public void setState(HTTP2_IOReactive state) {
		this.state = state;
		
		// state is alive by default when just set
		isStateAlive = true;
	}
	
	public HTTP2_Frame next() {
		return queue.poll();
	}
	
	public void setAlive(boolean stateFlag) {
		isStateAlive = stateFlag;
	}
	
	public boolean isVerbose() {
		return isVerbose;
	}
	
	@Override
	public void SSL_onSending(ByteBuffer buffer) {
		try {
			// fresh new start!!
			isStateAlive = true;
			boolean isRunning = true;
			//boolean DEBUG_bundled = true;
			while(isRunning && buffer.hasRemaining() && isStateAlive /*&& DEBUG_bundled*/) {
				HTTP2_Error error = state.on(buffer);
				if(error!=HTTP2_Error.NO_ERROR) {
					connection.close();
					isRunning = false;
				}
				//DEBUG_bundled = false;
			}
			if(isVerbose) {
				System.out.println("[HTTP2_Outbound] flush......");
			}
		
		}
		catch(Throwable throwable) {
			if(isVerbose) {
				throwable.printStackTrace();	
			}
			connection.close();
		}
	}
	
	public void push(HTTP2_Frame frame) {
		queue.add(frame);
		
		// notify that there is data to be sent and start asynchronously
		connection.resumeSending();
	}


	@Override
	public void ssl_onHandshakingCompleted() {
		// nothing to do
	}

}
