package com.s8.stack.arch.helium.http2.frames;

import com.s8.stack.arch.helium.http2.HTTP2_Connection;
import com.s8.stack.arch.helium.http2.HTTP2_Error;
import com.s8.stack.arch.helium.http2.utilities.BytesBlock;


/**
 * 
 * <h1>6.3. PRIORITY</h1>
 * 
 * <p>
 * The PRIORITY frame (type=0x2) specifies the sender-advised priority of a
 * stream (Section 5.3). It can be sent in any stream state, including idle or
 * closed streams.
 * </p>
 * 
 * <pre>

    +-+-------------------------------------------------------------+
    |E|                  Stream Dependency (31)                     |
    +-+-------------+-----------------------------------------------+
    |   Weight (8)  |
    +-+-------------+

                     Figure 8: PRIORITY Frame Payload
 * 
 * </pre>
 * <p>
 * 
 * The payload of a PRIORITY frame contains the following fields:
 * 
 * E: A single-bit flag indicating that the stream dependency is exclusive (see
 * Section 5.3).
 * 
 * Stream Dependency: A 31-bit stream identifier for the stream that this stream
 * depends on (see Section 5.3).
 * 
 * Weight: An unsigned 8-bit integer representing a priority weight for the
 * stream (see Section 5.3). Add one to the value to obtain a weight between 1
 * and 256.
 * 
 * The PRIORITY frame does not define any flags.
 * 
 * The PRIORITY frame always identifies a stream. If a PRIORITY frame is
 * received with a stream identifier of 0x0, the recipient MUST respond with a
 * connection error (Section 5.4.1) of type PROTOCOL_ERROR.
 * 
 * The PRIORITY frame can be sent on a stream in any state, though it cannot be
 * sent between consecutive frames that comprise a single header block (Section
 * 4.3). Note that this frame could arrive after processing or frame sending has
 * completed, which would cause it to have no effect on the identified stream.
 * For a stream that is in the "half-closed (remote)" or "closed" state, this
 * frame can only affect processing of the identified stream and its dependent
 * streams; it does not affect frame transmission on that stream.
 * 
 * The PRIORITY frame can be sent for a stream in the "idle" or "closed" state.
 * This allows for the reprioritization of a group of dependent streams by
 * altering the priority of an unused or closed parent stream.
 * 
 * A PRIORITY frame with a length other than 5 octets MUST be treated as a
 * stream error (Section 5.4.2) of type FRAME_SIZE_ERROR.
 * </p>
 * 
 * @author pc
 *
 */
public class HTTP2_PRIORITY_Frame extends HTTP2_Frame {

	public final static byte CODE = 0x2; 

	public final static int PAYLOAD_LENGTH = 5; 


	/**
	 * indicating that the stream dependency is exclusive (see Section 5.3).
	 */
	public boolean isStreamDependencyExclusive;


	/**
	 * identifier for the stream that this stream depends on (see Section 5.3).
	 */
	public int dependencyStreamIdentifier;

	/**
	 * Representing a priority weight for the stream (see Section 5.3). 
	 * .
	 */
	public int weight;

	
	private int streamIdentifier;


	public HTTP2_PRIORITY_Frame() {
		super();
	}

	@Override
	public HTTP2_Error setHeader(HTTP2_FrameHeader header) {

		/*
		 *  The PRIORITY frame does not define any flags.
		 */
		// no flags

		/*
		 *  The PRIORITY frame always identifies a stream. If a PRIORITY frame
		 *  is received with a stream identifier of 0x0, the recipient MUST
		 *  respond with a connection error (Section 5.4.1) of type
		 *  PROTOCOL_ERROR.
		 */
		streamIdentifier = header.streamIdentifier;
		if(streamIdentifier==0x00) {
			return HTTP2_Error.PROTOCOL_ERROR;
		}


		/*
		 * A PRIORITY frame with a length other than 5 octets MUST be treated as
		 * a stream error (Section 5.4.2) of type FRAME_SIZE_ERROR.
		 */
		if(header.length!=PAYLOAD_LENGTH) {
			return HTTP2_Error.FRAME_SIZE_ERROR;
		}

		return HTTP2_Error.NO_ERROR;
	}




	@Override
	public HTTP2_FrameType getType() {
		return HTTP2_FrameType.PRIORITY;
	}
	
	@Override
	public int getStreamIdentifier() {
		return streamIdentifier;
	}

	@Override
	public HTTP2_Error parsePayload(BytesBlock payload) {

		// The payload of a PRIORITY frame contains the following fields:

		/*
		 * E: A single-bit flag 
		 */
		isStreamDependencyExclusive = (payload.getByte(0) & 0x80)==0x80;

		/*
		 * Stream Dependency: A 31-bit stream 
		 */
		dependencyStreamIdentifier = payload.getUInt31(0);

		/*
		 *  weight: An unsigned 8-bit integer (Add one to the value to 
		 *  obtain a weight between 1 and 256)
		 */
		weight = payload.getUInt8(4)+1;
		return HTTP2_Error.NO_ERROR;
	}

	@Override
	public boolean isEndOfStream() {
		return false; // not defined
	}

	@Override
	public HTTP2_FrameHeader getHeader() {
		return new HTTP2_FrameHeader(PAYLOAD_LENGTH, CODE, (byte) 0x00, streamIdentifier);
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
