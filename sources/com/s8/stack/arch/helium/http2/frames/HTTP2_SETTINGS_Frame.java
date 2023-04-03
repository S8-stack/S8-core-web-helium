package com.s8.stack.arch.helium.http2.frames;

import com.s8.stack.arch.helium.http2.HTTP2_Connection;
import com.s8.stack.arch.helium.http2.HTTP2_Error;
import com.s8.stack.arch.helium.http2.settings.HTTP2_Settings;
import com.s8.stack.arch.helium.http2.utilities.BytesBlock;

/**
 * <h1>SETTINGS</h1>
 * <p>
 * [RFC 7540 / 6.5]
 * </p>
 * <p>
 * The SETTINGS frame (type=0x4) conveys configuration parameters that affect
 * how endpoints communicate, such as preferences and constraints on peer
 * behavior. The SETTINGS frame is also used to acknowledge the receipt of those
 * parameters. Individually, a SETTINGS parameter can also be referred to as a
 * "setting".
 * </p>
 * <p>
 * 6.5.1. SETTINGS Format The payload of a SETTINGS frame consists of zero or
 * more parameters, each consisting of an unsigned 16-bit setting identifier and
 * an unsigned 32-bit value.
 * </p>
 * <pre>

    +-------------------------------+
    |       Identifier (16)         |
    +-------------------------------+-------------------------------+
    |                        Value (32)                             |
    +---------------------------------------------------------------+

                         Figure 10: Setting Format
 * </pre>
 * <p>
 * SETTINGS parameters are not negotiated; they describe characteristics of the
 * sending peer, which are used by the receiving peer. Different values for the
 * same parameter can be advertised by each peer. For example, a client might
 * set a high initial flow-control window, whereas a server might set a lower
 * value to conserve resources.
 * </p>
 * <p>
 * A SETTINGS frame MUST be sent by both endpoints at the start of a connection
 * and MAY be sent at any other time by either endpoint over the lifetime of the
 * connection. Implementations MUST support all of the parameters defined by
 * this specification.
 * </p>
 * <p>
 * Each parameter in a SETTINGS frame replaces any existing value for that
 * parameter. Parameters are processed in the order in which they appear, and a
 * receiver of a SETTINGS frame does not need to maintain any state other than
 * the current value of its parameters. Therefore, the value of a SETTINGS
 * parameter is the last value that is seen by a receiver.
 * </p>
 * 
 * @author pc
 *
 */
public class HTTP2_SETTINGS_Frame extends HTTP2_Frame {

	public final static byte CODE = 0x04;

	public final static int PARAMETER_LENGTH = 6;

	private boolean isAcknoledging;

	public int payloadLength;

	public HTTP2_Settings.Entry[] entries ;
	
	public HTTP2_SETTINGS_Frame() {
		super();
	}
	
	public HTTP2_SETTINGS_Frame(HTTP2_Settings.Entry[] entries) {
		super();
		this.entries = entries;
		this.isAcknoledging = false;
	}

	@Override
	public HTTP2_Error setHeader(HTTP2_FrameHeader header) {

		/*
		 * SETTINGS frames always apply to a connection, never a single stream. The stream 
		 * identifier for a SETTINGS frame MUST be zero (0x0). If an endpoint receives a SETTINGS 
		 * frame whose stream identifier field is anything other than 0x0, the endpoint MUST 
		 * respond with a connection error (Section 5.4.1) of type PROTOCOL_ERROR.
		 */
		if(header.streamIdentifier!=0x00) {
			return HTTP2_Error.PROTOCOL_ERROR;
		}

		/* SETTINGS parameters are acknowledged by the receiving peer. To
		 enable this, the SETTINGS frame defines the following flag:
		 ACK (0x1): When set, bit 0 indicates that this frame acknowledges
		 receipt and application of the peer’s SETTINGS frame.
		 */
		byte flags = header.flags;
		isAcknoledging = (flags & 0x01)==0x01;


		payloadLength = header.length;
		if(isAcknoledging) {
			/*
			 * When this bit is set, the payload of the SETTINGS frame MUST be empty.
			 * Receipt of a SETTINGS frame with the ACK flag set and a length
			 * field value other than 0 MUST be treated as a connection error
			 * (Section 5.4.1) of type FRAME_SIZE_ERROR. For more information,
			 * see Section 6.5.3 ("Settings Synchronization").
			 */
			if(payloadLength!=0) {
				return HTTP2_Error.FRAME_SIZE_ERROR;
			}
		}
		else {
			/*
			 * A SETTINGS frame with a length other than a multiple of 6 octets MUST be treated 
			 * as a connection error (Section 5.4.1) of type FRAME_SIZE_ERROR.
			 */
			if(payloadLength%PARAMETER_LENGTH!=0) {
				return HTTP2_Error.FRAME_SIZE_ERROR;
			}
		}	

		/*
		 * The SETTINGS frame affects connection state. A badly formed or
		 * incomplete SETTINGS frame MUST be treated as a connection error
		 * (Section 5.4.1) of type PROTOCOL_ERROR.
		 */
		// TODO : Don't know yet how to implement "badly formed" detection...

		return HTTP2_Error.NO_ERROR; // not exceptions raised
	}


	@Override
	public HTTP2_FrameType getType() {
		return HTTP2_FrameType.SETTINGS;
	}
	
	@Override
	public int getStreamIdentifier() {
		// SETTINGS frames always apply to a connection, never a single stream
		return 0x0;
	}

	@Override
	public HTTP2_Error parsePayload(BytesBlock payload) {
		if(!isAcknoledging) {
			/*
			 * Most values in SETTINGS benefit from or require an understanding of when
			 * the peer has received and applied the changed parameter values. In order to
			 * provide such synchronization timepoints, the recipient of a SETTINGS frame in
			 * which the ACK flag is not set MUST apply the updated parameters as soon as
			 * possible upon receipt. The values in the SETTINGS frame MUST be processed in
			 * the order they appear, with no other frame processing between values.
			 * Unsupported parameters MUST be ignored. Once all values have been processed,
			 * the recipient MUST immediately emit a SETTINGS frame with the ACK flag set.
			 * Upon receiving a SETTINGS frame with the ACK flag set, the sender of the
			 * altered parameters can rely on the setting having been applied.
			 * 
			 * If the sender of a SETTINGS frame does not receive an acknowledgement within
			 * a reasonable amount of time, it MAY issue a connection error (Section 5.4.1)
			 * of type SETTINGS_TIMEOUT.
			 * 
			 */
			
			int nbSettings = payloadLength/PARAMETER_LENGTH;
			entries = new HTTP2_Settings.Entry[nbSettings];
			int offset=0;
			for(int index=0; index<nbSettings; index++) {
				HTTP2_Settings.Entry entry = new HTTP2_Settings.Entry();
				entry.parse(payload, offset);
				entries[index] = entry;
				offset+=PARAMETER_LENGTH;
			}
		}
		return HTTP2_Error.NO_ERROR;
	}

	@Override
	public boolean isEndOfStream() {
		return false;
	}

	@Override
	public HTTP2_FrameHeader getHeader() {
		
		payloadLength = 0;
		if(entries!=null) {
			payloadLength += entries.length*PARAMETER_LENGTH;			
		}

		/* SETTINGS parameters are acknowledged by the receiving peer. To
		 enable this, the SETTINGS frame defines the following flag:
		 ACK (0x1): When set, bit 0 indicates that this frame acknowledges
		 receipt and application of the peer’s SETTINGS frame.
		 */
		byte flags = 0x00;
		if(isAcknoledging) {
			flags |= 0x01;	
		}
		
		return new HTTP2_FrameHeader(payloadLength, CODE, flags, 0x00);
	}

	@Override
	public BytesBlock composePayload() {
		if(entries!=null) {
			int nbSettings = entries.length;
			BytesBlock payload = new BytesBlock(nbSettings*PARAMETER_LENGTH);
			int offset=0;
			for(int index=0; index<nbSettings; index++) {
				entries[index].compose(payload, offset);
				offset+=PARAMETER_LENGTH;
			}
			return payload;
		}
		else {
			return new BytesBlock(0);
		}
	}

	@Override
	public HTTP2_Error onReceived(HTTP2_Connection endpoint) {
		HTTP2_Error error;
		if(!isAcknoledging) {
			HTTP2_Settings settings = endpoint.getSettings();
			for(HTTP2_Settings.Entry setting : entries) {
				error = settings.set(setting, endpoint.HTTP2_isVerbose());
				if(error!=HTTP2_Error.NO_ERROR) {
					return error;
				}
			}
			
			// acknowledge
			HTTP2_SETTINGS_Frame acknowledgingFrame = new HTTP2_SETTINGS_Frame();
			acknowledgingFrame.isAcknoledging = true;
			endpoint.send(acknowledgingFrame);
		}
		return HTTP2_Error.NO_ERROR;
	}
}
