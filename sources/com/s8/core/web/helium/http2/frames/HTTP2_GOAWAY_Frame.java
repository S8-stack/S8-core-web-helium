package com.s8.core.web.helium.http2.frames;

import com.s8.core.web.helium.http2.HTTP2_Connection;
import com.s8.core.web.helium.http2.HTTP2_Error;
import com.s8.core.web.helium.http2.utilities.BytesBlock;


/**
 * <h1>GOAWAY</h1>
 * <p>
 * The GOAWAY frame (type=0x7) is used to initiate shutdown of a connection or
 * to signal serious error conditions. GOAWAY allows an endpoint to gracefully
 * stop accepting new streams while still finishing processing of previously
 * established streams. This enables administrative actions, like server
 * maintenance.
 * </p>
 * <p>
 * There is an inherent race condition between an endpoint starting new streams
 * and the remote sending a GOAWAY frame. To deal with this case, the GOAWAY
 * contains the stream identifier of the last peer- initiated stream that was or
 * might be processed on the sending endpoint in this connection. For instance,
 * if the server sends a GOAWAY frame, the identified stream is the
 * highest-numbered stream initiated by the client.
 * </p>
 * <p>
 * Once sent, the sender will ignore frames sent on streams initiated by the
 * receiver if the stream has an identifier higher than the included last stream
 * identifier. Receivers of a GOAWAY frame MUST NOT open additional streams on
 * the connection, although a new connection can be established for new streams.
 * </p>
 * <p>
 * If the receiver of the GOAWAY has sent data on streams with a higher stream
 * identifier than what is indicated in the GOAWAY frame, those streams are not
 * or will not be processed. The receiver of the GOAWAY frame can treat the
 * streams as though they had never been created at all, thereby allowing those
 * streams to be retried later on a new connection.
 * </p>
 * <p>
 * Endpoints SHOULD always send a GOAWAY frame before closing a connection so
 * that the remote peer can know whether a stream has been partially processed
 * or not. For example, if an HTTP client sends a POST at the same time that a
 * server closes a connection, the client cannot know if the server started to
 * process that POST request if the server does not send a GOAWAY frame to
 * indicate what streams it might have acted on.
 * </p>
 * <p>
 * An endpoint might choose to close a connection without sending a GOAWAY for
 * misbehaving peers.
 * </p>
 * <p>
 * A GOAWAY frame might not immediately precede closing of the connection; a
 * receiver of a GOAWAY that has no more use for the connection SHOULD still
 * send a GOAWAY frame before terminating the connection.
 * </p>
 * 
 * <pre>

  +-+-------------------------------------------------------------+
  |R|                  Last-Stream-ID (31)                        |
  +-+-------------------------------------------------------------+
  |                      Error Code (32)                          |
  +---------------------------------------------------------------+
  |                  Additional Debug Data (*)                    |
  +---------------------------------------------------------------+

                   Figure 13: GOAWAY Payload Format
 * 
 * </pre>
 * <p>
 * The GOAWAY frame does not define any flags.
 * </p>
 * <p>
 * The GOAWAY frame applies to the connection, not a specific stream. An
 * endpoint MUST treat a GOAWAY frame with a stream identifier other than 0x0 as
 * a connection error (Section 5.4.1) of type PROTOCOL_ERROR.
 * </p>
 * <p>
 * The last stream identifier in the GOAWAY frame contains the highest- numbered
 * stream identifier for which the sender of the GOAWAY frame might have taken
 * some action on or might yet take action on. All streams up to and including
 * the identified stream might have been processed in some way. The last stream
 * identifier can be set to 0 if no streams were processed.
 * </p>
 * <p>
 * Note: In this context, "processed" means that some data from the stream was
 * passed to some higher layer of software that might have taken some action as
 * a result.
 * </p>
 * <p>
 * If a connection terminates without a GOAWAY frame, the last stream identifier
 * is effectively the highest possible stream identifier.
 * </p>
 * <p>
 * On streams with lower- or equal-numbered identifiers that were not closed
 * completely prior to the connection being closed, reattempting requests,
 * transactions, or any protocol activity is not possible, with the exception of
 * idempotent actions like HTTP GET, PUT, or DELETE. Any protocol activity that
 * uses higher-numbered streams can be safely retried using a new connection.
 * </p>
 * <p>
 * Activity on streams numbered lower or equal to the last stream identifier
 * might still complete successfully. The sender of a GOAWAY frame might
 * gracefully shut down a connection by sending a GOAWAY frame, maintaining the
 * connection in an "open" state until all in- progress streams complete.
 * </p>
 * <p>
 * An endpoint MAY send multiple GOAWAY frames if circumstances change. For
 * instance, an endpoint that sends GOAWAY with NO_ERROR during graceful
 * shutdown could subsequently encounter a condition that requires immediate
 * termination of the connection. The last stream identifier from the last
 * GOAWAY frame received indicates which streams could have been acted upon.
 * Endpoints MUST NOT increase the value they send in the last stream
 * identifier, since the peers might already have retried unprocessed requests
 * on another connection.
 * </p>
 * <p>
 * A client that is unable to retry requests loses all requests that are in
 * flight when the server closes the connection. This is especially true for
 * intermediaries that might not be serving clients using HTTP/2. A server that
 * is attempting to gracefully shut down a connection SHOULD send an initial
 * GOAWAY frame with the last stream identifier set to 2^31-1 and a NO_ERROR
 * code. This signals to the client that a shutdown is imminent and that
 * initiating further requests is prohibited. After allowing time for any
 * in-flight stream creation (at least one round-trip time), the server can send
 * another GOAWAY frame with an updated last stream identifier. This ensures
 * that a connection can be cleanly shut down without losing requests.
 * </p>
 * <p>
 * After sending a GOAWAY frame, the sender can discard frames for streams
 * initiated by the receiver with identifiers higher than the identified last
 * stream. However, any frames that alter connection state cannot be completely
 * ignored. For instance, HEADERS, PUSH_PROMISE, and CONTINUATION frames MUST be
 * minimally processed to ensure the state maintained for header compression is
 * consistent (see Section 4.3); similarly, DATA frames MUST be counted toward
 * the connection flow-control window. Failure to process these frames can cause
 * flow control or header compression state to become unsynchronized.
 * </p>
 * <p>
 * The GOAWAY frame also contains a 32-bit error code (Section 7) that contains
 * the reason for closing the connection.
 * </p>
 * <p>
 * Endpoints MAY append opaque data to the payload of any GOAWAY frame.
 * Additional debug data is intended for diagnostic purposes only and carries no
 * semantic value. Debug information could contain security- or
 * privacy-sensitive data. Logged or otherwise persistently stored debug data
 * MUST have adequate safeguards to prevent unauthorized access.
 * </p>
 * 
 * @author pc
 *
 */
public class HTTP2_GOAWAY_Frame extends HTTP2_Frame {

	public final static byte CODE = 0x07;

	public int lastStreamIdentifier;

	public int errorCode;

	/**
	 * Endpoints MAY append opaque data to the payload of any GOAWAY frame.
	 * Additional debug data is intended for diagnostic purposes only and 
	 * carries no semantic value. Debug information could contain security 
	 * or privacy-sensitive data. Logged or otherwise persistently stored 
	 * debug data MUST have adequate safeguards to prevent unauthorized 
	 * access.
	 */
	public byte[] debugData;

	public int payloadLength;

	public HTTP2_GOAWAY_Frame() {
		super();		
	}

	@Override
	public HTTP2_Error setHeader(HTTP2_FrameHeader header) {

		/*
		 *  The GOAWAY frame applies to the connection, not a specific stream. 
		 *  An endpoint MUST treat a GOAWAY frame with a stream identifier other 
		 *  than 0x0 as a connection error (Section 5.4.1) of type PROTOCOL_ERROR.
		 */
		if(header.streamIdentifier!=0x0) {
			return HTTP2_Error.PROTOCOL_ERROR;
		}

		/* <flags> */

		// The GOAWAY frame does not define any flags.

		/* </flags> */
		payloadLength = header.length;

		return HTTP2_Error.NO_ERROR; // terminates normally
	}


	@Override
	public HTTP2_Error parsePayload(BytesBlock payload) {
		int offset = 0;
		lastStreamIdentifier = payload.getUInt31(offset);
		offset+=4;
		errorCode = payload.getUInt31(offset);
		offset+=4;

		debugData = payload.trim(offset);

		return HTTP2_Error.NO_ERROR;
	}	

	@Override
	public HTTP2_FrameType getType() {
		return HTTP2_FrameType.GOAWAY;
	}

	@Override
	public int getStreamIdentifier() {
		return 0x0;
	}

	@Override
	public boolean isEndOfStream() {
		// no flag defined
		return false;
	}

	@Override
	public HTTP2_FrameHeader getHeader() {
		// The GOAWAY frame does not define any flags.
		return new HTTP2_FrameHeader(payloadLength, CODE, (byte) 0x00, 0x00);
	}

	@Override
	public BytesBlock composePayload() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HTTP2_Error onReceived(HTTP2_Connection endpoint) {
		if(endpoint.HTTP2_isVerbose()) {
			
			HTTP2_Error receivedError = HTTP2_Error.get(errorCode);
			String debugInfo = debugData.length>0?new String(debugData):"no-info";
			
			System.out.println("[HTTP2_GOAWAY_Frame] : "+
					"last stream-id = "+lastStreamIdentifier+
					", error = "+receivedError+
					", debug-info = "+debugInfo);
		}
		endpoint.close();
		return HTTP2_Error.NO_ERROR;
	}

}
