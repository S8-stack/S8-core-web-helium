package com.s8.stack.arch.helium.http2.frames;

import java.nio.ByteBuffer;

import com.s8.stack.arch.helium.http2.HTTP2_Error;
import com.s8.stack.arch.helium.http2.HTTP2_IOReactive;
import com.s8.stack.arch.helium.http2.HTTP2_Inbound;
import com.s8.stack.arch.helium.http2.utilities.BytesBlock;

public class ReceivingFrameHeader implements HTTP2_IOReactive {

	private BytesBlock inputBuffer;

	private HTTP2_Inbound inbound;

	private HTTP2_FrameType assertedType;
	
	public ReceivingFrameHeader(HTTP2_Inbound inbound) {
		super();
		this.inbound = inbound;
		inputBuffer = new BytesBlock(HTTP2_FrameHeader.FRAME_HEADER_LENGTH);
	}
	
	public void assertFrameType(HTTP2_FrameType assertedType) {
		this.assertedType = assertedType;
	}

	
	@Override
	public HTTP2_Error on(ByteBuffer buffer) {

		inputBuffer.pullFromBuffer(buffer);

		if(inputBuffer.isFilled()) {

			HTTP2_FrameHeader header = new HTTP2_FrameHeader();
			header.parse(inputBuffer);

			if(header.length>inbound.getEndpointSettings().maxFrameSize) {

				/*
				 * An endpoint MUST send an error code of FRAME_SIZE_ERROR if a frame
				 * exceeds the size defined in SETTINGS_MAX_FRAME_SIZE, exceeds any
				 * limit defined for the frame type, or is too small to contain
				 * mandatory frame data. A frame size error in a frame that could alter
				 * the state of the entire connection MUST be treated as a connection
				 * error (Section 5.4.1);
				 */
				// send connection error
				return HTTP2_Error.FRAME_SIZE_ERROR;
			}

			HTTP2_Frame frame = header.createFrame();
			
			// cannot match frame type, so stop here
			if(frame==null) { 
				return HTTP2_Error.PROTOCOL_ERROR; 
			}

			// type checking if asserted
			if(assertedType!=null && frame.getType()!=assertedType) {
				return HTTP2_Error.PROTOCOL_ERROR;
			}
			
			/* set data */
			HTTP2_Error error = frame.setHeader(header);
			if(error!= HTTP2_Error.NO_ERROR) {
				return error;
			}

			// now, start to receive body
			inbound.setState(new ReceivingFramePayload(inbound, frame, header.length));
			return HTTP2_Error.NO_ERROR; // go for next reception

		}
		else {
			/*
			 * We need to have a completely transferred block to start parsing
			 * (note: recopy has been preferred to full asynchronous code (with -for instance- states for each of the parsing steps), 
			 * for the following reasons:
			 * <ul>
			 * <li> sake of code readability and simplicity</li>
			 * <li> by block recopy overhead is small</li>
			 * </ul>
			 */
			 // OK but not enough bytes, continue acquiring	
			return HTTP2_Error.NO_ERROR;
		}
	}

}
