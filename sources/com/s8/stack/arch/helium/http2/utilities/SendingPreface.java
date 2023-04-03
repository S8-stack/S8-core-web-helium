package com.s8.stack.arch.helium.http2.utilities;

import java.nio.ByteBuffer;

import com.s8.stack.arch.helium.http2.HTTP2_Error;
import com.s8.stack.arch.helium.http2.HTTP2_IOReactive;
import com.s8.stack.arch.helium.http2.HTTP2_Outbound;
import com.s8.stack.arch.helium.http2.frames.SendingFrameHeader;

/**
 * 
 * The client connection preface starts with a sequence of 24 octets, which in
 * hex notation is:
 * <pre>
 * 0x505249202a20485454502f322e300d0a0d0a534d0d0a0d0a
 * </pre>
 * That is, the connection preface starts with the string "PRI *
 * HTTP/2.0\r\n\r\nSM\r\n\r\n"."
 * 
 * @author pc
 *
 */
public class SendingPreface implements HTTP2_IOReactive {
	
	private int index;
	
	private HTTP2_Outbound outbound;

	public SendingPreface(HTTP2_Outbound outbound) {
		super();
		index = 0;
		this.outbound = outbound;
	}

	@Override
	public HTTP2_Error on(ByteBuffer buffer) {
		while(buffer.hasRemaining()) {
			buffer.put(ReceivingPreface.PREFACE[index]);
			index++;
			if(index==ReceivingPreface.PREFACE_LENGTH) {
				outbound.setState(new SendingFrameHeader(outbound));
				return HTTP2_Error.NO_ERROR;
			}
		}
		return HTTP2_Error.NO_ERROR;
	}

}
