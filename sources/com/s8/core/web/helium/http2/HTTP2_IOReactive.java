package com.s8.core.web.helium.http2;

import java.nio.ByteBuffer;

/**
 * Inspired by <code>QxIOReactive</code>
 * @author pc
 *
 */
public interface HTTP2_IOReactive {

	public HTTP2_Error on(ByteBuffer buffer);
	
}
