package com.s8.stack.arch.helium.rx;

import java.nio.ByteBuffer;

/**
 * <h1>IOReactive</h1>
 * <p>
 * Based on the "don't call us, we'll call you" principle. 
 * Namely, use this class by overriding this method and supply 
 * bytes when required.
 * </p>
 * <p>
 * Note that is the responsability of the application to flip/clear/compact buffer
 * </p>
 * @author pc
 *
 */
public interface RxReceiver {


	/**
	 * 
	 * @param buffer the buffer to read from (passed in read mode state)
	 * 
	 * @return is another receiving requested
	 */
	public boolean onReceived(ByteBuffer buffer, int nBytes);
	
	
}
