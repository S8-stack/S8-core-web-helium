package com.s8.stack.arch.helium.http2.frames;

import com.s8.stack.arch.helium.http2.HTTP2_Connection;
import com.s8.stack.arch.helium.http2.HTTP2_Error;
import com.s8.stack.arch.helium.http2.hpack.HPACK_Data;
import com.s8.stack.arch.helium.http2.utilities.BytesBlock;

/**
 * <h1>PUSH_PROMISE</h1>
 * <p>
 * [RFC-7540 / 6.6] The PUSH_PROMISE frame (type=0x5) is used to notify the peer
 * endpoint in advance of streams the sender intends to initiate. The
 * PUSH_PROMISE frame includes the unsigned 31-bit identifier of the stream the
 * endpoint plans to create along with a set of headers that provide additional
 * context for the stream. Section 8.2 contains a thorough description of the
 * use of PUSH_PROMISE frames.
 * </p>
 * 
 * <pre>

    +---------------+
    |Pad Length? (8)|
    +-+-------------+-----------------------------------------------+
    |R|                  Promised Stream ID (31)                    |
    +-+-----------------------------+-------------------------------+
    |                   Header Block Fragment (*)                 ...
    +---------------------------------------------------------------+
    |                           Padding (*)                       ...
    +---------------------------------------------------------------+

                  Figure 11: PUSH_PROMISE Payload Format
 * 
 * </pre>
 * <p>
 * The PUSH_PROMISE frame payload has the following fields:
 * 
 * Pad Length: An 8-bit field containing the length of the frame padding in
 * units of octets. This field is only present if the PADDED flag is set.
 * 
 * R: A single reserved bit.
 * 
 * Promised Stream ID: An unsigned 31-bit integer that identifies the stream
 * that is reserved by the PUSH_PROMISE. The promised stream identifier MUST be
 * a valid choice for the next stream sent by the sender (see "new stream
 * identifier" in Section 5.1.1).
 * 
 * Header Block Fragment: A header block fragment (Section 4.3) containing
 * request header fields.
 * 
 * Padding: Padding octets.
 * </p>
 * 
 * @author pc
 *
 */
public class HTTP2_PUSH_PROMISE_Frame extends HTTP2_Frame {

	public final static byte CODE = 0x05;

	/**
	 * When set, indicates that this frame contains an entire header block 
	 * (Section 4.3) and is not followed by any CONTINUATION frames. 
	 * A PUSH_PROMISE frame without the END_HEADERS flag set MUST be 
	 * followed by a CONTINUATION frame for the same stream. A receiver MUST 
	 * treat the receipt of any other type of frame or a frame on a different 
	 * stream as a connection error (Section 5.4.1) of type PROTOCOL_ERROR.
	 */
	public boolean isEndOfHeaders;


	/**
	 * When set, bit 3 indicates that the Pad Length field and any padding 
	 * that it describes are present.
	 */
	public boolean isPadded;


	/**
	 * An 8-bit field containing the length of the frame padding in units of octets. 
	 * This field is only present if the PADDED flag is set.
	 */
	public int paddingLength;


	/**
	 * An unsigned 31-bit integer that identifies the stream that is reserved by the 
	 * PUSH_PROMISE. The promised stream identifier MUST be a valid choice for the 
	 * next stream sent by the sender (see "new stream identifier" in Section 5.1.1).
	 */
	public int promisedStreamIdentifier;



	/**
	 * header data
	 */
	public HPACK_Data data;


	public int payloadLength;


	public HTTP2_PUSH_PROMISE_Frame() {
		super();
	}

	@Override
	public HTTP2_Error setHeader(HTTP2_FrameHeader header) {


		paddingLength = header.length;

		/*
		 * The PUSH_PROMISE frame defines the following flags:
		 */

		byte flags = header.flags;
		// END_HEADERS (0x4)
		isEndOfHeaders = (flags & 0x04)==0x04;

		// PADDED (0x8): 
		isPadded = (flags & 0x08)==0x08;


		return HTTP2_Error.NO_ERROR; // no error
	}



	@Override
	public HTTP2_FrameType getType() {
		return HTTP2_FrameType.PUSH_PROMISE;
	}
	
	@Override
	public int getStreamIdentifier() {
		return 0x0; // TODO need to further check doc
	}

	@Override
	public HTTP2_Error parsePayload(BytesBlock payload) {

		int offset = 0;
		if(isPadded) {
			paddingLength = payload.getUInt8(offset);
			offset+=1;
		}

		promisedStreamIdentifier = payload.getUInt31(offset);
		offset+=4;

		int fragmentLength = payloadLength-offset-paddingLength;
		if(fragmentLength>0) {
			data = new HPACK_Data(payload.getBytes(), offset, fragmentLength);
		}

		return HTTP2_Error.NO_ERROR;
	}

	@Override
	public boolean isEndOfStream() {
		// flag END_STREAM not defined for this type of frame
		return false;
	}

	@Override
	public HTTP2_FrameHeader getHeader() {
		
		/*
		 * The PUSH_PROMISE frame defines the following flags:
		 */

		byte flags = 0x00;
		
		// END_HEADERS (0x4)
		if(isEndOfHeaders) {
			flags |= 0x04;
		}

		// PADDED (0x8):
		if(isPadded) {
			flags |= 0x08;
		}
		return new HTTP2_FrameHeader(payloadLength, CODE, flags, 0x00);
	}

	@Override
	public BytesBlock composePayload() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HTTP2_Error onReceived(HTTP2_Connection endpoint) {
		return HTTP2_Error.NO_ERROR;
	}

}
