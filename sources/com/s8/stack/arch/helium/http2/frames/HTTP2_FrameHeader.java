package com.s8.stack.arch.helium.http2.frames;

import com.s8.stack.arch.helium.http2.utilities.BytesBlock;

/** 
 * <h1>Frame Format</h1>
 * 
 * <p>
 * All frames begin with a fixed 9-octet header followed by a variable- length
 * payload.
 * 
 * <pre>
	    +-----------------------------------------------+
	    |                 Length (24)                   |
	    +---------------+---------------+---------------+
	    |   Type (8)    |   Flags (8)   |
	    +-+-------------+---------------+-------------------------------+
	    |R|                 Stream Identifier (31)                      |
	    +=+=============================================================+
	    |                   Frame Payload (0...)                      ...
	    +---------------------------------------------------------------+

	                          Figure 1: Frame Layout
 * 
 * </pre>
 * 
 * The fields of the frame header are defined as:
 * <ul>
 * <li><b>Length</b>: The length of the frame payload expressed as an unsigned 24-bit
 * integer. Values greater than 2^14 (16,384) MUST NOT be sent unless the
 * receiver has set a larger value for SETTINGS_MAX_FRAME_SIZE. The 9 octets 
 * of the frame header are not included in this value.
 * </li>
 * <li><b>Type</b>: The 8-bit type of the frame. The frame type determines the format and
 * semantics of the frame. Implementations MUST ignore and discard any frame
 * that has a type that is unknown.
 * </li>
 * <li><b>Flags</b>: An 8-bit field reserved for boolean flags specific to the frame type. 
 * Flags are assigned semantics specific to the indicated frame type. Flags that
 * have no defined semantics for a particular frame type MUST be ignored and
 * MUST be left unset (0x0) when sending.
 * </li>
 * <li><b>R</b>: A reserved 1-bit field. The semantics of this bit are undefined, and the
 * bit MUST remain unset (0x0) when sending and MUST be ignored when receiving.
 * </li>
 * <li><b>Stream Identifier</b>: A stream identifier (see Section 5.1.1) expressed as an
 * unsigned 31-bit integer. The value 0x0 is reserved for frames that are
 * associated with the connection as a whole as opposed to an individual stream.
 * </li>
 * </p>
 * @author pc
 * 
 */
public class HTTP2_FrameHeader {


	/**
	 * (from RFC 7540 / 4.1. Frame Format) All frames begin with a fixed 9-octet 
	 * header followed by a variable-length payload.
	 */
	public final static int FRAME_HEADER_LENGTH = 9;



	/** 
	 * [RFC-7540/4.1] from frame header parsing
	 */
	public int length;


	/**
	 * 
	 */
	public int type;


	/** 
	 * [RFC-7540/4.1] from frame header parsing 
	 */
	public byte flags;


	/** 
	 * [RFC-7540/4.1] from frame header parsing 
	 */
	public int streamIdentifier;

	
	public HTTP2_FrameHeader() {
		super();
	}

	public HTTP2_FrameHeader(int length, int type, byte flags, int streamIdentifier) {
		super();
		this.length = length;
		this.type = type;
		this.flags = flags;
		this.streamIdentifier = streamIdentifier;
	}


	/**
	 * 
	 * @param bytes
	 */
	public void parse(BytesBlock bytes) {

		// <length>
		/*
		 * The length of the frame payload expressed as an unsigned
		 * 24-bit integer. The 9 octets of the frame header are not included in this value.
		 */
		length = bytes.getUInt24(0);


		// </length>

		// <type>
		/*
		 *  Type: The 8-bit type of the frame. The frame type determines the
		 *  format and semantics of the frame. Implementations MUST ignore
		 *  and discard any frame that has a type that is unknown.
		 */
		type = bytes.getUInt8(3);
		// </type>

		// <flags>
		/*
		 * An 8-bit field reserved for boolean flags specific to the frame type.
		 * Flags are assigned semantics specific to the indicated frame type.
		 * Flags that have no defined semantics for a particular frame type
		 * MUST be ignored and MUST be left unset (0x0) when sending.
		 * -> interpreted later on
		 */
		flags = bytes.getByte(4);
		// </flags>

		// <r>
		/*
		 * The semantics of this bit are undefined,
		 * and the bit MUST remain unset (0x0) when sending and MUST be
		 * ignored when receiving.
		 */
		//boolean r = (bytes[4] & 0x80)==0x80;
		// </r>

		// <stream-identifier>
		/*
		 * A stream identifier (see Section 5.1.1) expressed
		 * as an unsigned 31-bit integer.
		 */
		streamIdentifier = bytes.getUInt31(5);
		// </stream-identifier>

	}


	public BytesBlock compose() {
		BytesBlock bytes = new BytesBlock(FRAME_HEADER_LENGTH);

		// <length>
		bytes.setUInt24(0, length);
		// </length>

		// <type>
		bytes.setUInt8(3, type);
		// </type>

		// <flags>
		bytes.setByte(4, flags);
		// </flags>

		// <r>
		// nothing to do 
		// </r>

		// <stream-identifier>
		bytes.setUInt31(5, streamIdentifier);
		// </stream-identifier>

		return bytes;
	}

	
	
	public HTTP2_Frame createFrame() {

		switch(type) {

		case HTTP2_DATA_Frame.CODE: 
			return new HTTP2_DATA_Frame();

		case HTTP2_HEADERS_Frame.CODE:
			return  new HTTP2_HEADERS_Frame();

		case HTTP2_PRIORITY_Frame.CODE:
			return new HTTP2_PRIORITY_Frame();

		case HTTP2_RESET_Frame.CODE:
			return new HTTP2_RESET_Frame();

		case HTTP2_SETTINGS_Frame.CODE:
			return new HTTP2_SETTINGS_Frame();

		case HTTP2_PUSH_PROMISE_Frame.CODE:
			return new HTTP2_PUSH_PROMISE_Frame();

		case HTTP2_PING_Frame.CODE:
			return new HTTP2_PING_Frame();

		case HTTP2_GOAWAY_Frame.CODE:
			return new HTTP2_GOAWAY_Frame();

		case HTTP2_WINDOW_UPDATE_Frame.CODE:
			return new HTTP2_WINDOW_UPDATE_Frame();

		case HTTP2_CONTINUATION_Frame.CODE:
			return new HTTP2_CONTINUATION_Frame();

			// cannot match frame type, so stop here
		default : return null; 

		}
	}
}
