package com.s8.stack.arch.helium.http2.frames;

import com.s8.stack.arch.helium.http2.HTTP2_Connection;
import com.s8.stack.arch.helium.http2.HTTP2_Error;
import com.s8.stack.arch.helium.http2.hpack.HPACK_Data;
import com.s8.stack.arch.helium.http2.streams.HTTP2_Stream;
import com.s8.stack.arch.helium.http2.streams.HTTP2_StreamState;
import com.s8.stack.arch.helium.http2.utilities.BytesBlock;


/**
 * <h1>[RFC-7540/6.2] HEADERS</h1>
 * <p>
 * The HEADERS frame (type=0x1) is used to open a stream (Section 5.1), and
 * additionally carries a header block fragment. HEADERS frames can be sent on a
 * stream in the "idle", "reserved (local)", "open", or "half-closed (remote)"
 * state.
 * </p>
 * <pre>
 * +---------------+
 * |Pad Length? (8)|
 * +-+-------------+-----------------------------------------------+
 * |E|                 Stream Dependency? (31)                     |
 * +-+-------------+-----------------------------------------------+
 * |  Weight? (8)  |
 * +-+-------------+-----------------------------------------------+
 * |                   Header Block Fragment (*)                 ...
 * +---------------------------------------------------------------+
 * |                           Padding (*)                       ...
 * +---------------------------------------------------------------+
 * 		Figure 7: HEADERS Frame Payload
 * </pre>
 * <p>
 * 
 * The HEADERS frame changes the connection state as described in Section 4.3.
 * 
 * The HEADERS frame can include padding. Padding fields and flags are identical
 * to those defined for DATA frames (Section 6.1). Padding that exceeds the size
 * remaining for the header block fragment MUST be treated as a PROTOCOL_ERROR.
 * 
 * Prioritization information in a HEADERS frame is logically equivalent to a
 * separate PRIORITY frame, but inclusion in HEADERS avoids the potential for
 * churn in stream prioritization when new streams are created. Prioritization
 * fields in HEADERS frames subsequent to the first on a stream reprioritize the
 * stream (Section 5.3.3).
 * 
 * @author pc
 *
 */
public class HTTP2_HEADERS_Frame extends HTTP2_Frame {

	public final static int CODE = 0x01;

	@Override
	public HTTP2_FrameType getType() {
		return HTTP2_FrameType.HEADERS;
	}


	public int streamIdentifier;

	@Override
	public int getStreamIdentifier() {
		return streamIdentifier;
	}



	/**
	 * <h1>END_STREAM</h1>
	 * <p>
	 * When set, bit 0 indicates that the header block (Section 4.3) is the last
	 * that the endpoint will send for the identified stream.
	 * A HEADERS frame carries the END_STREAM flag that signals the end of a stream.
	 * However, a HEADERS frame with the END_STREAM flag set can be followed by
	 * CONTINUATION frames on the same stream. Logically, the CONTINUATION frames
	 * are part of the HEADERS frame.
	 * </p>
	 */
	public boolean isEndOfStream;

	/**
	 * <h1>END_HEADERS</h1>
	 * <p>
	 * When set, bit 2 indicates that this frame contains an entire header block
	 * (Section 4.3) and is not followed by any CONTINUATION frames.
	 * 
	 * A HEADERS frame without the END_HEADERS flag set MUST be followed by a
	 * CONTINUATION frame for the same stream. A receiver MUST treat the receipt of
	 * any other type of frame or a frame on a different stream as a connection
	 * error (Section 5.4.1) of type PROTOCOL_ERROR.
	 * </p>
	 */
	public boolean isEndOfHeaders;
	

	/**
	 * <h1>PADDED</h1>
	 * <p>
	 * When set, bit 3 indicates that the Pad Length field 
	 * and any padding that it describes are present.
	 * </p>
	 */
	public boolean isPadded;


	/**
	 * <h1>PRIORITY</h1>
	 * <p>
	 * When set, bit 5 indicates that the Exclusive Flag (E), Stream Dependency, and
	 * Weight fields are present; see Section 5.3.
	 * </p>
	 */
	public boolean isPriorityDefined;

	/**
	 * Pad Length: An 8-bit field containing the length of the frame padding in
	 * units of octets. This field is only present if the PADDED flag is set
	 */
	public int paddingLength;
	
	/**
	 * E: A single-bit flag indicating that the stream dependency is exclusive (see
	 * Section 5.3). This field is only present if the PRIORITY flag is set.
	 */
	public boolean isExclusive;



	/**
	 * Stream Dependency: A 31-bit stream identifier for the stream that this stream
	 * depends on (see Section 5.3). This field is only present if the PRIORITY flag
	 * is set.
	 */
	public int streamDependency;

	/**
	 * Weight: An unsigned 8-bit integer representing a priority weight for the
	 * stream (see Section 5.3). Add one to the value to obtain a weight between 1
	 * and 256. This field is only present if the PRIORITY flag is set.
	 */
	public int weight;

	private int payloadLength;


	public HPACK_Data headersFragment;


	/**
	 * Constructor for <code>HTTP2_Inbound</code>
	 */
	public HTTP2_HEADERS_Frame() {
		super();
	}


	/**
	 * (outbound)
	 * @param paddingLength
	 */
	public void setPadding(int paddingLength) {
		this.isPadded = true;
		this.paddingLength = paddingLength;
	}


	/**
	 * (outbound)
	 * @param isExclusive
	 * @param weight
	 */
	public void setPriority(boolean isExclusive, int streamDependency, int weight) {
		this.isPriorityDefined = true;
		this.streamDependency = streamDependency;
		this.isExclusive = true;
		this.weight = weight;
	}


	@Override
	public HTTP2_Error setHeader(HTTP2_FrameHeader header) {

		/*
		 * HEADERS frames MUST be associated with a stream. If a HEADERS frame is
		 * received whose stream identifier field is 0x0, the recipient MUST respond
		 * with a connection error (Section 5.4.1) of type PROTOCOL_ERROR.
		 */
		streamIdentifier = header.streamIdentifier;
		if(streamIdentifier==0x00) {
			return HTTP2_Error.PROTOCOL_ERROR;
		}


		/* <flags> */
		byte flags = header.flags;
		//The HEADERS frame defines the following flags:

		/* END_STREAM (0x1) */
		isEndOfStream = (flags & 0x01) == 0x01;

		/* END_HEADERS (0x4) */
		isEndOfHeaders = (flags & 0x04) == 0x04;

		/* PADDED (0x8) */
		isPadded = (flags & 0x08) == 0x08;

		/* PRIORITY (0x20) */
		isPriorityDefined = (flags & 0x20) == 0x20;

		/* </flags> */


		payloadLength = header.length;

		return HTTP2_Error.NO_ERROR;
	}



	@Override
	public HTTP2_Error parsePayload(BytesBlock payload) {

		int fragmentLength = payloadLength;
		int offset = 0;
		if(isPadded) {
			paddingLength = payload.getUInt8(0);

			/* 
			 * Padding that exceeds the size remaining for the 
			 * header block fragment MUST be treated as a PROTOCOL_ERROR.
			 */
			if(paddingLength>payloadLength) {
				return HTTP2_Error.PROTOCOL_ERROR;
			}

			fragmentLength -= 1+paddingLength;
			offset+=1;
		}

		if(isPriorityDefined) {
			isExclusive = (payload.getByte(offset+0) & 0x80) == 0x80;
			streamDependency = payload.getUInt31(offset+0);
			weight = payload.getUInt8(offset+4);
			fragmentLength -= 5;
			offset+=5;
		}

		// Header Block Fragment
		if(fragmentLength<0) {
			return HTTP2_Error.PROTOCOL_ERROR;
		}

		headersFragment = new HPACK_Data(payload.getBytes(), offset, fragmentLength);

		return HTTP2_Error.NO_ERROR;
	}


	

	@Override
	public HTTP2_FrameHeader getHeader() {

		// recompute payload length before shipping
		int size = headersFragment.getIndex();
		if(isPadded) {
			size += 1+paddingLength;
		}
		if(isPriorityDefined) {
			size += 5;
		}
		payloadLength = size;
		
		/* <flags> */
		byte flags = 0x00;
		//The HEADERS frame defines the following flags:

		/* END_STREAM (0x1) */
		if(isEndOfStream) {
			flags |= 0x01;
		}

		/* END_HEADERS (0x4) */
		if(isEndOfHeaders) {
			flags |= 0x04;
		}

		/* PADDED (0x8) */
		if(isPadded) {
			flags |= 0x08;
		}

		/* PRIORITY (0x20) */
		if(isPriorityDefined) {
			flags |= 0x20;
		}

		/* </flags> */

		return new HTTP2_FrameHeader(payloadLength, CODE, flags, streamIdentifier);
	}


	@Override
	public BytesBlock composePayload() {

		BytesBlock payload = new BytesBlock(payloadLength);
		int offset = 0;
		
		// padding
		if(isPadded) {
			payload.setUInt8(offset, paddingLength);
			offset+=1;
		}

		// priority
		if(isPriorityDefined) {
			payload.setUInt31(offset, streamDependency);
			if(isExclusive) {
				payload.setByte(offset, (byte) (payload.getByte(offset) & 0x80));
			}
			payload.setUInt8(offset+4, weight);
			offset+=5;
		}

		payload.setBytes(offset, headersFragment.getBytes(), 0, headersFragment.getIndex());
		return payload;
	}
	
	
	@Override
	public HTTP2_Error onReceived(HTTP2_Connection connection) {
		HTTP2_Stream stream = connection.getStream(streamIdentifier);

		// check state
		switch(stream.getState()) {
		
		case IDLE: 
			stream.setState(HTTP2_StreamState.OPEN);
			break; // no problem
			
		case OPEN: 
			break; // no problem
		
		default: 
			return HTTP2_Error.PROTOCOL_ERROR; // problem
		}

		if(isPriorityDefined) {
			stream.setPriority(isExclusive, streamDependency, weight);
		}
		stream.pushHeaders(headersFragment, isEndOfHeaders);

		if(isEndOfStream) {
			stream.remoteClose();
		}
		return HTTP2_Error.NO_ERROR;
	}


	@Override
	public boolean isEndOfStream() {
		return isEndOfStream;
	}
}
