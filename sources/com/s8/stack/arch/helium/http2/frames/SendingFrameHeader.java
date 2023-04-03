package com.s8.stack.arch.helium.http2.frames;

import java.nio.ByteBuffer;

import com.s8.stack.arch.helium.http2.HTTP2_Error;
import com.s8.stack.arch.helium.http2.HTTP2_IOReactive;
import com.s8.stack.arch.helium.http2.HTTP2_Outbound;
import com.s8.stack.arch.helium.http2.utilities.BytesBlock;

public class SendingFrameHeader implements HTTP2_IOReactive {

	private HTTP2_Outbound outbound;

	private HTTP2_Frame frame;
	
	private BytesBlock block;
	

	public SendingFrameHeader(HTTP2_Outbound outbound) {
		super();
		this.outbound = outbound;
	}

	@Override
	public HTTP2_Error on(ByteBuffer buffer) {
		if(frame==null) { // no frame loaded
			frame = outbound.next();
			if(frame!=null) {
				block = frame.getHeader().compose();
				
				if(outbound.isVerbose()) {
					System.out.println("[SendingFrameHeader] Start sending frame: "+frame.getType());
				}
				
				
				if(block.pushToBuffer(buffer)) {
					// if successfully pushed all bytes to buffer, move to next state...
					outbound.setState(new SendingFramePayload(outbound, frame));
				}
				// else: keep on pushing header
			}
			else {
				// keep same state
				outbound.setAlive(false); // state has been exhausted
			}
		}
		else { // keep on pushing header
			if(block.pushToBuffer(buffer)) {
				// if successfully pushed all bytes to buffer, move to next state...
				outbound.setState(new SendingFramePayload(outbound, frame));
			}
			// else: keep on pushing header
		}
		
		/*
		 * We need to have a completely transferred block to start parsing
		 * (note: recopy has been preferred to full asynchronous code (with -for instance- states for each of the parsing steps), 
		 * for the following reasons:
		 * <ul>
		 * <li> sake of code readability and simplicity</li>
		 * <li> by block recopy overhead is small</li>
		 * </ul>
		 */
		
		return HTTP2_Error.NO_ERROR; // feed...
	}

}
