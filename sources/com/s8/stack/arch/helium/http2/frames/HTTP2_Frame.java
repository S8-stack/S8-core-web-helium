package com.s8.stack.arch.helium.http2.frames;

import com.s8.stack.arch.helium.http2.HTTP2_Connection;
import com.s8.stack.arch.helium.http2.HTTP2_Error;
import com.s8.stack.arch.helium.http2.utilities.BytesBlock;

/**
 * HTTP2_Frame is a frame payload. Frame header has been decoded by the parser and relevant arguments
 * have been added to the HTTP2_Frame object.
 */
public abstract class HTTP2_Frame {

	/**
	 * Length: The length of the frame payload expressed as an unsigned
	 * 24-bit integer. Values greater than 2^14 (16,384) MUST NOT be
	 * sent unless the receiver has set a larger value for
	 * SETTINGS_MAX_FRAME_SIZE.
	 */
	public final static int DEFAULT_FRAME_LENGTH = 16384;



	public HTTP2_Frame() {
		super();
	}


	
	public abstract int getStreamIdentifier();
	
	public abstract HTTP2_FrameType getType();

	/**
	 * Set fields from header parsing
	 * 
	 * @param payloadLength
	 * @param flags
	 * @param streamIdentifier
	 * @return isSuccessful (error flag)
	 */
	public abstract HTTP2_Error setHeader(HTTP2_FrameHeader header);


	/**
	 * prepare header fields
	 */
	public abstract HTTP2_FrameHeader getHeader();


	/**
	 * This method is responsible for handling appropriately payload as it 
	 * is decoded (intermediate storage).
	 * 
	 * @param bytes
	 * @return
	 */
	public abstract HTTP2_Error parsePayload(BytesBlock payload);



	public abstract BytesBlock composePayload();


	/**
	 * Test the presence of END_STREAM flag, and return 
	 * @return END_STREAM flag value if defined, false otherwise
	 */
	public abstract boolean isEndOfStream();
	
	/**
	 * This method is finally called when frame has been received
	 * 
	 * @param endpoint
	 * @return
	 */
	public abstract HTTP2_Error onReceived(HTTP2_Connection endpoint);
}
