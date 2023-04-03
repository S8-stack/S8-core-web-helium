package com.s8.stack.arch.helium.http2.hpack;

import com.s8.stack.arch.helium.http2.headers.HTTP2_Header;

public abstract class HPACK_HeaderEntry {

	public abstract int getIndex();

	public abstract HTTP2_Header getHeader();

	/**
	 * code for easy mapping of header entry
	 * @return
	 */
	public int getCode() {
		return getHeader().getPrototype().code;
	}
	
	/**
	 * next entry in bucket chain
	 */
	public HPACK_HeaderEntry next;

	/**
	 * next entry in bucket chain
	 */
	public HPACK_HeaderEntry previous;


	public void relink() {
		if(previous!=null) {
			previous.next = next;
		}
		if(next!=null) {
			next.previous = previous;
		}
	}
}
