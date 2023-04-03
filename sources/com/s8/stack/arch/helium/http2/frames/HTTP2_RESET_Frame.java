package com.s8.stack.arch.helium.http2.frames;

import com.s8.stack.arch.helium.http2.HTTP2_Connection;
import com.s8.stack.arch.helium.http2.HTTP2_Error;
import com.s8.stack.arch.helium.http2.streams.HTTP2_Stream;
import com.s8.stack.arch.helium.http2.streams.HTTP2_StreamState;
import com.s8.stack.arch.helium.http2.utilities.BytesBlock;

/**
 * <h1>RST_STREAM</h1>
 * <p>
 * [from RFC-7540 / 6.4]
 * </p>
 * <p>
 * The RST_STREAM frame (type=0x3) allows for immediate termination of a stream.
 * RST_STREAM is sent to request cancellation of a stream or to indicate that an
 * error condition has occurred.
 * </p>
 * <pre>
  +---------------------------------------------------------------+
  |                        Error Code (32)                        |
  +---------------------------------------------------------------+

                  Figure 9: RST_STREAM Frame Payload
 * </pre>
 * <p>
 * The RST_STREAM frame contains a single unsigned, 32-bit integer identifying
 * the error code (Section 7). The error code indicates why the stream is being
 * terminated.
 * 
 * The RST_STREAM frame does not define any flags.
 * 
 * The RST_STREAM frame fully terminates the referenced stream and causes it to
 * enter the "closed" state. After receiving a RST_STREAM on a stream, the
 * receiver MUST NOT send additional frames for that stream, with the exception
 * of PRIORITY. However, after sending the RST_STREAM, the sending endpoint MUST
 * be prepared to receive and process additional frames sent on the stream that
 * might have been sent by the peer prior to the arrival of the RST_STREAM.
 * 
 * RST_STREAM frames MUST be associated with a stream. If a RST_STREAM frame is
 * received with a stream identifier of 0x0, the recipient MUST treat this as a
 * connection error (Section 5.4.1) of type PROTOCOL_ERROR.
 * 
 * RST_STREAM frames MUST NOT be sent for a stream in the "idle" state. If a
 * RST_STREAM frame identifying an idle stream is received, the recipient MUST
 * treat this as a connection error (Section 5.4.1) of type PROTOCOL_ERROR.
 * 
 * A RST_STREAM frame with a length other than 4 octets MUST be treated as a
 * connection error (Section 5.4.1) of type FRAME_SIZE_ERROR.
 * </p>
 * 
 * @author pc
 *
 */
public class HTTP2_RESET_Frame extends HTTP2_Frame {

	public final static byte CODE = 0x03;

	public final static int PAYLOAD_LENGTH = 4;


	public HTTP2_Error error;

	private int streamIdentifier;


	/**
	 * 
	 * @param connection
	 * @param payloadLength
	 * @param flags
	 * @param streamIdentifier
	 */
	public HTTP2_RESET_Frame() {
		super();
	}



	@Override
	public HTTP2_Error setHeader(HTTP2_FrameHeader header) {

		/*
		 * RST_STREAM frames MUST be associated with a stream. If a RST_STREAM
		 * frame is received with a stream identifier of 0x0, the recipient MUST
		 * treat this as a connection error (Section 5.4.1) of type PROTOCOL_ERROR.
		 */
		streamIdentifier = header.streamIdentifier;
		if(streamIdentifier==0x00) {
			return HTTP2_Error.PROTOCOL_ERROR;
		}

		/* 
		 * A RST_STREAM frame with a length other than 4 octets MUST be treated
		 * as a connection error (Section 5.4.1) of type FRAME_SIZE_ERROR.
		 */
		if(header.length!=PAYLOAD_LENGTH) {
			return HTTP2_Error.FRAME_SIZE_ERROR;
		}

		// The RST_STREAM frame does not define any flags.

		return HTTP2_Error.NO_ERROR; // OK
	}








	@Override
	public HTTP2_FrameType getType() {
		return HTTP2_FrameType.RESET;
	}

	@Override
	public int getStreamIdentifier() {
		return streamIdentifier;
	}


	@Override
	public HTTP2_Error parsePayload(BytesBlock payload) {
		int code = payload.getUInt31(0);
		error = HTTP2_Error.get(code);


		return HTTP2_Error.NO_ERROR;
	}


	@Override
	public boolean isEndOfStream() {
		return false; // no flags defined
	}



	@Override
	public HTTP2_FrameHeader getHeader() {
		// The RST_STREAM frame does not define any flags.
		return new HTTP2_FrameHeader(PAYLOAD_LENGTH, CODE, (byte) 0x00, streamIdentifier);
	}



	@Override
	public BytesBlock composePayload() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public HTTP2_Error onReceived(HTTP2_Connection endpoint) {
		if(endpoint.HTTP2_isVerbose()) {
			System.out.println("[HTTP2_RESET_Frame]: streamIdentifier="+streamIdentifier);
			System.out.println("[HTTP2_RESET_Frame]: error:"+error);
		}

		HTTP2_Stream stream = endpoint.getStream(streamIdentifier);
		if(stream!=null) {
			stream.setState(HTTP2_StreamState.CLOSED);
			return HTTP2_Error.NO_ERROR;		
		}
		else {
			return HTTP2_Error.PROTOCOL_ERROR;
		}
	}
}
