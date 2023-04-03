package com.s8.stack.arch.helium.http2.frames;


import com.s8.io.bytes.linked.LinkedBytes;
import com.s8.stack.arch.helium.http2.HTTP2_Connection;
import com.s8.stack.arch.helium.http2.HTTP2_Error;
import com.s8.stack.arch.helium.http2.streams.HTTP2_Stream;
import com.s8.stack.arch.helium.http2.utilities.BytesBlock;

/**
 * <h1>6.1. DATA</h1>
 * <p>
 * DATA frames (type=0x0) convey arbitrary, variable-length sequences of octets
 * associated with a stream. One or more DATA frames are used, for instance, to
 * carry HTTP request or response payloads.
 * 
 * DATA frames MAY also contain padding. Padding can be added to DATA frames to
 * obscure the size of messages. Padding is a security feature; see Section
 * 10.7.
 * </p>
 * <pre>

  +---------------+
  |Pad Length? (8)|
  +---------------+-----------------------------------------------+
  |                            Data (*)                         ...
  +---------------------------------------------------------------+
  |                           Padding (*)                       ...
  +---------------------------------------------------------------+

                     Figure 6: DATA Frame Payload
 * 
 * </pre>
 * <p>
 * The DATA frame contains the following fields:
 * <ul>
 * <li>Pad Length: An 8-bit field containing the length of the frame padding in
 * units of octets. This field is conditional (as signified by a "?" in the
 * diagram) and is only present if the PADDED flag is set.
 * </li>
 * <li>Data: Application data. The amount of data is the remainder of the frame
 * payload after subtracting the length of the other fields that are present.
 * </li>
 * <li>Padding: Padding octets that contain no application semantic value.
 * Padding octets MUST be set to zero when sending. A receiver is not obligated
 * to verify padding but MAY treat non-zero padding as a connection error
 * (Section 5.4.1) of type PROTOCOL_ERROR.
 * </li>
 * </ul>
 * </p>
 * <p>
 * The DATA frame defines the following flags:
 * 
 * END_STREAM (0x1): When set, bit 0 indicates that this frame is the last that
 * the endpoint will send for the identified stream. Setting this flag causes
 * the stream to enter one of the "half- closed" states or the "closed" state
 * (Section 5.1).
 * 
 * PADDED (0x8): When set, bit 3 indicates that the Pad Length field and any
 * padding that it describes are present.
 * </p>
 * <p>
 * DATA frames MUST be associated with a stream. If a DATA frame is received
 * whose stream identifier field is 0x0, the recipient MUST respond with a
 * connection error (Section 5.4.1) of type PROTOCOL_ERROR.
 * 
 * DATA frames are subject to flow control and can only be sent when a stream is
 * in the "open" or "half-closed (remote)" state. The entire DATA frame payload
 * is included in flow control, including the Pad Length and Padding fields if
 * present. If a DATA frame is received whose stream is not in "open" or
 * "half-closed (local)" state, the recipient MUST respond with a stream error
 * (Section 5.4.2) of type STREAM_CLOSED.
 * 
 * The total number of padding octets is determined by the value of the Pad
 * Length field. If the length of the padding is the length of the frame payload
 * or greater, the recipient MUST treat this as a connection error (Section
 * 5.4.1) of type PROTOCOL_ERROR.
 * 
 * Note: A frame can be increased in size by one octet by including a Pad Length
 * field with a value of zero.
 * 
 * </p>
 *
 */
public class HTTP2_DATA_Frame extends HTTP2_Frame {

	public final static int CODE = 0x00;


	private boolean isPadded;

	private int paddingLength;



	/**
	 * When set, bit 0 indicates that this frame is the
	 * last that the endpoint will send for the identified stream.
	 * Setting this flag causes the stream to enter one of the "half-closed" 
	 * states or the "closed" state (Section 5.1).
	 */
	public boolean isEndOfStream;


	public int payloadLength;

	public int streamIdentifier;

	public LinkedBytes fragment;


	public HTTP2_DATA_Frame() {
		super();
	}

	@Override
	public HTTP2_Error setHeader(HTTP2_FrameHeader header) {

		streamIdentifier = header.streamIdentifier;
		if(streamIdentifier==0x00f) {
			/* The value 0x0 is reserved for frames that are associated with the 
			 * connection as a whole as opposed to an individual stream.
			 */
			return HTTP2_Error.PROTOCOL_ERROR;
		}

		payloadLength = header.length;

		// <flags>
		byte flags = header.flags;

		isEndOfStream = (flags & 0x01)==0x01;

		/*
		 * When set, bit 3 indicates that the Pad Length field 
		 * and any padding that it describes are present.
		 */
		isPadded = (flags & 0x08)==0x08;

		// </flags>

		return HTTP2_Error.NO_ERROR; // OK
	}


	@Override
	public HTTP2_FrameType getType() {
		return HTTP2_FrameType.DATA;
	}

	@Override
	public int getStreamIdentifier() {
		return streamIdentifier;
	}

	@Override
	public HTTP2_Error parsePayload(BytesBlock payload) {

		int dataLength = payloadLength;
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

			dataLength -= 1+paddingLength;
			offset+=1;
		}

		fragment = new LinkedBytes(payload.getBytes(), offset, dataLength);
		/*
		HTTP2_Stream stream = endpoint.getStream(streamIdentifier);
		stream.pushData(fragment);
		 */
		return HTTP2_Error.NO_ERROR;
	}

	@Override
	public boolean isEndOfStream() {
		return isEndOfStream;
	}

	@Override
	public HTTP2_FrameHeader getHeader() {

		payloadLength = fragment.length;
		
		/* <flags> */
		byte flags = 0x00;

		if(isEndOfStream) {
			flags |= 0x01;
		}

		/*
		 * When set, bit 3 indicates that the Pad Length field 
		 * and any padding that it describes are present.
		 */
		if(isPadded) {
			flags |= 0x08;
			payloadLength+=paddingLength+1;
		}
		/* </flags> */
		return new HTTP2_FrameHeader(payloadLength, CODE, flags, streamIdentifier);
	}

	@Override
	public BytesBlock composePayload() {
		BytesBlock payload = new BytesBlock(payloadLength);
		int index=0;
		if(isPadded) {
			payload.setUInt8(index, paddingLength);
			index+=1;
		}
		payload.setBytes(index, fragment.bytes, fragment.offset, fragment.length);
		return payload;
	}

	@Override
	public HTTP2_Error onReceived(HTTP2_Connection endpoint) {
		HTTP2_Stream stream = endpoint.getStream(streamIdentifier);

		// check state
		switch(stream.getState()) {
		case OPEN: break; // no problem
		default: return HTTP2_Error.PROTOCOL_ERROR;
		}

		stream.pushData(fragment);

		if(isEndOfStream) {
			stream.remoteClose();
		}
		return HTTP2_Error.NO_ERROR;
	}


}