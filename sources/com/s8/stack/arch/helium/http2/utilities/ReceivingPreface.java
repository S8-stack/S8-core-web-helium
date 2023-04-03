package com.s8.stack.arch.helium.http2.utilities;

import java.nio.ByteBuffer;

import com.s8.stack.arch.helium.http2.HTTP2_Error;
import com.s8.stack.arch.helium.http2.HTTP2_IOReactive;
import com.s8.stack.arch.helium.http2.HTTP2_Inbound;
import com.s8.stack.arch.helium.http2.frames.HTTP2_FrameType;
import com.s8.stack.arch.helium.http2.frames.ReceivingFrameHeader;

/**
 * 
 * The client connection preface starts with a sequence of 24 octets, which in
 * hex notation is:
 * 
 * 0x505249202a20485454502f322e300d0a0d0a534d0d0a0d0a
 * 
 * That is, the connection preface starts with the string "PRI *
 * HTTP/2.0\r\n\r\nSM\r\n\r\n"."
 * 
 * @author pc
 *
 */
public class ReceivingPreface implements HTTP2_IOReactive {

	public final static int PREFACE_LENGTH = 24;
	
	public final static byte[] PREFACE = new byte[] {
			(byte) 0x50, // 00
			(byte) 0x52, // 01
			(byte) 0x49, // 02
			(byte) 0x20, // 03
			(byte) 0x2a, // 04
			(byte) 0x20, // 05
			(byte) 0x48, // 06
			(byte) 0x54, // 07
			(byte) 0x54, // 08
			(byte) 0x50, // 09
			(byte) 0x2f, // 10
			(byte) 0x32, // 11
			(byte) 0x2e, // 12
			(byte) 0x30, // 13
			(byte) 0x0d, // 14
			(byte) 0x0a, // 15
			(byte) 0x0d, // 16
			(byte) 0x0a, // 17
			(byte) 0x53, // 18
			(byte) 0x4d, // 19
			(byte) 0x0d, // 20
			(byte) 0x0a, // 21
			(byte) 0x0d, // 22
			(byte) 0x0a  // 23
	};
	
	private HTTP2_Inbound inbound;
	
	private int index;

	public ReceivingPreface(HTTP2_Inbound inbound) {
		super();
		this.inbound = inbound;
		index = 0;
	}


	@Override
	public HTTP2_Error on(ByteBuffer buffer) {
		while(buffer.hasRemaining()) {
			byte b = buffer.get();
			if(b!=PREFACE[index]) {
				// exit connection
				inbound.close();
				
				/*
				 * Clients and servers MUST treat an invalid connection preface as a connection
				 * error (Section 5.4.1) of type PROTOCOL_ERROR. A GOAWAY frame (Section 6.8)
				 * MAY be omitted in this case, since an invalid preface indicates that the peer
				 * is not using HTTP/2.
				 */
				return HTTP2_Error.PROTOCOL_ERROR;
			}
			index++;
			if(index==PREFACE_LENGTH) {
				
				if(inbound.isVerbose()) {
					System.out.println("[HTTP2] Preface received!!");
				}
				
				// reach end
				ReceivingFrameHeader state = new ReceivingFrameHeader(inbound);
				
				/*
				 * [RFC 7540] This sequence MUST be followed by a SETTINGS frame (Section 6.5),
				 * which MAY be empty
				 */
				state.assertFrameType(HTTP2_FrameType.SETTINGS);
				
				inbound.setState(state);
				return HTTP2_Error.NO_ERROR;
			}
		}
		return HTTP2_Error.NO_ERROR;
	}

}
