package com.s8.core.web.helium.rx;

import java.nio.channels.SocketChannel;

class IdentifierGenerator {
	

	private final Object lock = new Object();

	private long nextId = 0x64L;


	/**
	 * 
	 * @param channel
	 * @return
	 */
	public String generate(SocketChannel channel) {
		String main = channel.toString();
		long tag = 0L;
		synchronized (lock) { tag = nextId++; }
		return Long.toHexString(tag) + '[' + main + ']';
	}
}
