package com.s8.stack.arch.helium.http2.frames;

import com.s8.stack.arch.helium.http2.HTTP2_Connection;
import com.s8.stack.arch.helium.http2.HTTP2_Error;
import com.s8.stack.arch.helium.http2.utilities.BytesBlock;

/**
 * <p>
 * The PING frame (type=0x6) is a mechanism for measuring a minimal
 * round-trip time from the sender, as well as determining whether an 
 * idle connection is still functional. PING frames can be sent from 
 * any endpoint.</p>
 * </p>
 * <pre>
 * +---------------------------------------------------------------+
 * | Opaque Data (64) 											   |
 * +---------------------------------------------------------------+
 * Figure 12: PING Payload Format
 * </pre>
 * <p>
 * In addition to the frame header, PING frames MUST contain 8 octets of
 * opaque data in the payload. A sender can include any value it
 * chooses and use those octets in any fashion.
 * </p>
 * @author pc
 *
 */
public class HTTP2_PING_Frame extends HTTP2_Frame {

	public final static byte CODE = 0x06;

	public final static int PAYLOAD_LENGTH = 8;

	/**
	 * When set, flag indicates that this PING frame is a PING response
	 */
	private boolean isAcknoledging;

	public byte[] data;


	public HTTP2_PING_Frame() {
		super();
	}


	@Override
	public HTTP2_Error setHeader(HTTP2_FrameHeader header) {


		/*
		 * 	Receipt of a PING frame with a length field value other than 8 MUST
		 * be treated as a connection error (Section 5.4.1) of type FRAME_SIZE_ERROR.
		 */
		if(header.length!=PAYLOAD_LENGTH) {
			return HTTP2_Error.FRAME_SIZE_ERROR;
		}


		/* <flags> */
		byte flags = header.flags;
		//The PING frame defines the following flags:

		/*
		 * ACK (0x1): When set, bit 0 indicates that this PING frame is a PING
		 */
		isAcknoledging = (flags & 0x1) == 0x1;


		/* </flags> */

		/*
		 * PING frames are not associated with any individual stream. If a PING
		 * frame is received with a stream identifier field value other than
		 * 0x0, the recipient MUST respond with a connection error 
		 * (Section 5.4.1) of type PROTOCOL_ERROR.
		 */
		if(header.streamIdentifier!=0x0) {
			return HTTP2_Error.PROTOCOL_ERROR;
		}

		return HTTP2_Error.NO_ERROR; // terminates normally
	}


	@Override
	public HTTP2_FrameType getType() {
		return HTTP2_FrameType.PING;
	}

	@Override
	public int getStreamIdentifier() {
		/*
		 * PING frames are not associated with any individual stream.
		 */
		return 0x0;
	}


	@Override
	public HTTP2_Error parsePayload(BytesBlock payload) {

		data = payload.getBytes();

		/*
		if(!isAcknoledging) {
			System.out.println("[HTTP2_PING] payload: "+new String(payload.getBytes()));
		}
		else {
			System.out.println("[HTTP2_PING] payload: none (acknoledging)");
		}
		*/
		return HTTP2_Error.NO_ERROR;
	}


	@Override
	public boolean isEndOfStream() {
		return false; // flag not defined
	}


	@Override
	public HTTP2_FrameHeader getHeader() {
		/* <flags> */
		byte flags = 0x00;
		//The PING frame defines the following flags:

		/*
		 * ACK (0x1): When set, bit 0 indicates that this PING frame is a PING
		 */
		if(isAcknoledging) {
			flags |= 0x1;
		}

		/* </flags> */
		return new HTTP2_FrameHeader(PAYLOAD_LENGTH, CODE, flags, 0x00);
	}


	@Override
	public BytesBlock composePayload() {
		return new BytesBlock(data);
	}


	@Override
	public HTTP2_Error onReceived(HTTP2_Connection endpoint) {
		if(endpoint.HTTP2_isVerbose()) {
			System.out.println("[HTTP2_PING_Frame] :"+new String(data));
		}
		/*
		 * PONG 
		 * -> Receivers of a PING frame that does not include an ACK flag MUST send a
		 * PING frame with the ACK flag set in response, with an identical payload. PING
		 * responses SHOULD be given higher priority than any other frame.
		 */
		if(!isAcknoledging) {
			HTTP2_PING_Frame pong = new HTTP2_PING_Frame();
			pong.isAcknoledging = true;
			pong.data = data;
			endpoint.send(pong);
		}

		return HTTP2_Error.NO_ERROR;
	}

}
