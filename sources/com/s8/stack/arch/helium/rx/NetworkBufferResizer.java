package com.s8.stack.arch.helium.rx;

import java.nio.ByteBuffer;


/**
 * 
 * @author pierreconvert
 *
 */
public interface NetworkBufferResizer {

	public ByteBuffer resizeNetworkBuffer(int capacity);
}