package com.s8.core.web.helium.utilities;

import java.nio.ByteBuffer;

public class HeUtilities {

	
	/**
	 * pos=458 lim=458 cap=17408]
	 * @return
	 */
	public static String printInfo(ByteBuffer buffer) {
		return "pos="+buffer.position()+" lim="+buffer.limit()+" cap="+buffer.capacity();
	}
}
