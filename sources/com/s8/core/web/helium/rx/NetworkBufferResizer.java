package com.s8.core.web.helium.rx;

import java.nio.ByteBuffer;


/**
 * 
 * @author pierreconvert
 *
 */
public interface NetworkBufferResizer {

	public ByteBuffer resizeNetworkBuffer(int capacity);
}