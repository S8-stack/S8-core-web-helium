package com.s8.stack.arch.helium.http2.frames;

import com.s8.stack.arch.helium.http2.HTTP2_Connection;
import com.s8.stack.arch.helium.http2.HTTP2_Error;
import com.s8.stack.arch.helium.http2.utilities.BytesBlock;

/**
 * 
 * <h1>WINDOW_UPDATE</h1>
 * <p>
 * The WINDOW_UPDATE frame (type=0x8) is used to implement flow control; 
 * see Section 5.2 for an overview.
 * </p>
 * <p>
 * Flow control operates at two levels: on each individual stream and on 
 * the entire connection. Both types of flow control are hop by hop, that is, 
 * only between the two endpoints. Intermediaries do not forward WINDOW_UPDATE 
 * frames between dependent connections. However, throttling of data transfer 
 * by any receiver can indirectly cause the propagation of flow-control 
 * information toward the original sender. 
 * </p>
 * <p>
 * Flow control only applies to frames that are identified as being subject 
 * to flow control. Of the 
 * frame types defined in this document, this includes only DATA frames. 
 * Frames that are exempt from flow control MUST be accepted and processed, 
 * unless the receiver is unable to assign resources to handling the frame. 
 * A receiver MAY respond with a stream error (Section 5.4.2) or connection 
 * error (Section 5.4.1) of type FLOW_CONTROL_ERROR if it is unable to accept
 * a frame.
 * </p>
 * <pre>
 * +-+-------------------------------------------------------------+
 * |R| Window Size Increment (31)                                  |
 * +-+-------------------------------------------------------------+
 * Figure 14: WINDOW_UPDATE Payload Format
 * </pre>
 * @author pc
 *
 */
public class HTTP2_WINDOW_UPDATE_Frame extends HTTP2_Frame {

	public final static int CODE = 0x08;
	
	public final static int PAYLOAD_LENGTH = 4;

	public int windowSizeIncrement;
	
	public int streamIdentifier;

	public HTTP2_WINDOW_UPDATE_Frame() {
		super();
	}


	@Override
	public HTTP2_Error setHeader(HTTP2_FrameHeader header) {

		/* <flags> */
		// The WINDOW_UPDATE frame does not define any flags.
		/* </flags> */

		/*
		 * The WINDOW_UPDATE frame can be specific to a stream or to the entire
		 * connection.
		 */
		streamIdentifier = header.streamIdentifier;

		/*
		 * A WINDOW_UPDATE frame with a length other than 4 octets MUST be 
		 * treated as a connection error (Section 5.4.1) of type FRAME_SIZE_ERROR.
		 */
		if(header.length!=PAYLOAD_LENGTH) {
			return HTTP2_Error.FRAME_SIZE_ERROR;
		}

		

		return HTTP2_Error.NO_ERROR; // OK
	}
	
	
	@Override
	public HTTP2_FrameType getType() {
		return HTTP2_FrameType.WINDOW_UPDATE;
	}
	
	@Override
	public int getStreamIdentifier() {
		return streamIdentifier;
	}


	@Override
	public HTTP2_Error parsePayload(BytesBlock payload) {
		
		windowSizeIncrement = payload.getUInt31(0);
		
		/*
		 * The legal range for the increment to the flow-control window is 1 to
		 * 2^31-1 (2,147,483,647) octets.
		 */
		if(windowSizeIncrement>2147483647) {
			return HTTP2_Error.FLOW_CONTROL_ERROR;
		}

		/*
		 * 	A receiver MUST treat the receipt of a WINDOW_UPDATE frame with an
		 *  flow-control window increment of 0 as a stream error (Section 5.4.2)
		 *  of type PROTOCOL_ERROR; errors on the connection flow-control window
		 *  MUST be treated as a connection error (Section 5.4.1). 
		 */
		if(windowSizeIncrement==0) {
			return HTTP2_Error.PROTOCOL_ERROR;
		}

		/*
		 * The WINDOW_UPDATE frame can be specific to a stream or to the entire
		 * connection. In the former case, the frame’s stream identifier indicates 
		 * the affected stream; in the latter, the value "0" indicates that the 
		 * entire connection is the subject of the frame.
		 */


		/*
		 * A sender MUST NOT allow a flow-control window to exceed 2^31-1
		 * octets. If a sender receives a WINDOW_UPDATE that causes a 
		 * flow-control window to exceed this maximum, it MUST terminate 
		 * either the stream or the connection, as appropriate. 
		 * For streams, the sender sends a RST_STREAM with an error code 
		 * of FLOW_CONTROL_ERROR; for the connection, a GOAWAY frame with 
		 * an error code of FLOW_CONTROL_ERROR is sent.
		 */
		return HTTP2_Error.NO_ERROR;
	}


	@Override
	public boolean isEndOfStream() {
		/* The WINDOW_UPDATE frame does not define any flags */
		return false;
	}


	@Override
	public HTTP2_FrameHeader getHeader() {
		return new HTTP2_FrameHeader(PAYLOAD_LENGTH, CODE, (byte) 0x00, streamIdentifier) ;
	}


	@Override
	public BytesBlock composePayload() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public HTTP2_Error onReceived(HTTP2_Connection endpoint) {
		if(endpoint.HTTP2_isVerbose()) {
			System.out.println("[HTTP2_WINDOW_UPDATE_Frame] windowSizeIncrement: "+windowSizeIncrement);
		}
		return HTTP2_Error.NO_ERROR;
	}

}
