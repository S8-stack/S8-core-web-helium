package com.s8.stack.arch.helium.http2.frames;

import com.s8.stack.arch.helium.http2.HTTP2_Connection;
import com.s8.stack.arch.helium.http2.HTTP2_Error;
import com.s8.stack.arch.helium.http2.hpack.HPACK_Data;
import com.s8.stack.arch.helium.http2.streams.HTTP2_Stream;
import com.s8.stack.arch.helium.http2.utilities.BytesBlock;

/**
 * <h1>CONTINUATION</h1>
 * <p>
 * The CONTINUATION frame (type=0x9) is used to continue a sequence of
 *  header block fragments (Section 4.3). Any number of CONTINUATION frames 
 *  can be sent, as long as the preceding frame is on the same stream 
 *  and is a HEADERS, PUSH_PROMISE, or CONTINUATION frame without
 *  the END_HEADERS flag set.
 * </p>
 * <pre>
 *  +---------------------------------------------------------------+
 *  | Header Block Fragment (*) ...
 *  +---------------------------------------------------------------+
 *  Figure 15: CONTINUATION Frame Payload
 * </pre>
 * 
 * @author pc
 *
 */
public class HTTP2_CONTINUATION_Frame extends HTTP2_Frame {


	public final static int CODE = 0x09;


	/**
	 * <h1>END_HEADERS</h1>
	 * When set, bit 2 indicates that this frame ends a  header block (Section 4.3). 
	 * If the END_HEADERS bit is not set, this frame MUST be followed by another 
	 * CONTINUATION frame. A receiver MUST treat the receipt of any other type of 
	 * frame or a frame on a different stream as a connection error (Section 5.4.1) 
	 * of type PROTOCOL_ERROR.
	 */
	public boolean isEndOfHeaders;

	public int streamIdentifier;

	public HPACK_Data headersFragment;


	public int payloadLength; 

	public HTTP2_CONTINUATION_Frame() {
		super();
	}


	@Override
	public int getStreamIdentifier() {
		return streamIdentifier;
	}
	
	
	@Override
	public HTTP2_Error setHeader(HTTP2_FrameHeader header) {

		streamIdentifier = header.streamIdentifier;

		payloadLength = header.length;

		/* <flags> */
		byte flags = header.flags;

		//END_HEADERS (0x4): 
		isEndOfHeaders = (flags & 0x4) == 0x4;

		/* </flags> */

		return HTTP2_Error.NO_ERROR;
	}



	@Override
	public HTTP2_FrameType getType() {
		return HTTP2_FrameType.CONTINUATION;
	}

	@Override
	public HTTP2_Error parsePayload(BytesBlock payload) {
		headersFragment = new HPACK_Data(payload.getBytes(), 0, payloadLength);
		return HTTP2_Error.NO_ERROR;
	}

	@Override
	public boolean isEndOfStream() {
		// END_STREAM not defined for this frame
		return false;
	}

	@Override
	public HTTP2_FrameHeader getHeader() {
		// compile here if necessary

		/* <flags> */
		byte flags = 0x00;

		//END_HEADERS (0x4): 
		if(isEndOfHeaders) {
			flags |= 0x4;
		}
		/* </flags> */

		return new HTTP2_FrameHeader(payloadLength, CODE, flags, streamIdentifier);
	}

	@Override
	public BytesBlock composePayload() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HTTP2_Error onReceived(HTTP2_Connection endpoint) {
		HTTP2_Stream stream = endpoint.getStream(streamIdentifier);

		// check state
		switch(stream.getState()) {
		case OPEN: break; // no problem
		default: return HTTP2_Error.PROTOCOL_ERROR;
		}

		stream.pushHeaders(headersFragment, isEndOfHeaders);

		if(isEndOfHeaders) {
			stream.remoteClose();
		}
		return HTTP2_Error.NO_ERROR;
	}

	
}
