package com.s8.stack.arch.helium.http2.frames;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.s8.stack.arch.helium.http2.HTTP2_Error;
import com.s8.stack.arch.helium.http2.HTTP2_IOReactive;
import com.s8.stack.arch.helium.http2.HTTP2_Inbound;
import com.s8.stack.arch.helium.http2.utilities.BytesBlock;

public class ReceivingFramePayload implements HTTP2_IOReactive {

	private HTTP2_Inbound inbound;

	private HTTP2_Frame frame;

	private BytesBlock inputBuffer;

	public ReceivingFramePayload(HTTP2_Inbound inbound, HTTP2_Frame frame, int payloadLength) {
		super();
		this.inbound = inbound;
		this.frame = frame;
		this.inputBuffer = new BytesBlock(payloadLength);
	}

	@Override
	public HTTP2_Error on(ByteBuffer buffer) {
		inputBuffer.pullFromBuffer(buffer);
		if(inputBuffer.isFilled()) {


			if(inbound.isVerbose()) {
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date date = new Date();
				System.out.println("[HTTP2] Frame received: "+frame.getType()+" at "+dateFormat.format(date));
			}

			// get payload
			HTTP2_Error error = frame.parsePayload(inputBuffer);
			if(error != HTTP2_Error.NO_ERROR) { return error;}

			//inbound.onFrameReceived(frame);
			error = frame.onReceived(inbound.getEndpoint());
			if(error != HTTP2_Error.NO_ERROR) { return error;}
			
			// start listening for new frame
			inbound.setState(new ReceivingFrameHeader(inbound));	

			return HTTP2_Error.NO_ERROR;
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
			return HTTP2_Error.NO_ERROR; // Shadock keeps pumping
		}
	}
}
