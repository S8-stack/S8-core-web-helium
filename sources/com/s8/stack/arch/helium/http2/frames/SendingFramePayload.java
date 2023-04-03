package com.s8.stack.arch.helium.http2.frames;

import java.nio.ByteBuffer;

import com.s8.stack.arch.helium.http2.HTTP2_Error;
import com.s8.stack.arch.helium.http2.HTTP2_IOReactive;
import com.s8.stack.arch.helium.http2.HTTP2_Outbound;
import com.s8.stack.arch.helium.http2.utilities.BytesBlock;

public class SendingFramePayload implements HTTP2_IOReactive {

	private HTTP2_Outbound outbound;
	
	private HTTP2_Frame frame;
	
	private BytesBlock payload;
	
	public SendingFramePayload(HTTP2_Outbound outbound, HTTP2_Frame frame) {
		super();
		this.outbound = outbound;
		this.frame = frame;
	}

	@Override
	public HTTP2_Error on(ByteBuffer buffer) {
		if(payload==null) {
			payload = frame.composePayload();
		}
		
		if(payload.pushToBuffer(buffer)) {
			outbound.setState(new SendingFrameHeader(outbound));	
		}
		// else { } -> keep on pushing...
		
		//outbound.setAlive(false); // TODO : to be removed in prod (for debugging only)
		return HTTP2_Error.NO_ERROR;
	}

}
