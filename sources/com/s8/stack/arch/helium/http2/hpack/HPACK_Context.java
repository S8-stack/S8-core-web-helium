package com.s8.stack.arch.helium.http2.hpack;

import com.s8.stack.arch.helium.http2.headers.HTTP2_Header;
import com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderMapping;

public class HPACK_Context {
	
	
	
	public final static int STATIC_TABLE_LENGTH = 61;

	public HTTP2_HeaderMapping mapping;

	
	public HPACK_Context(boolean isVerbose) {
		super();
		mapping = new HTTP2_HeaderMapping(isVerbose);
	}
	
	
	
	
	public interface HeadersFragmentHandler {
		public void handle(HPACK_Data data);
	}

	
	
	
	public HTTP2_Header.Prototype getPrototype(String name) {
		return mapping.get(name);
	}
	
	
	
	public HTTP2_Header createHeader(String name) {
		return mapping.get(name).create();
	}
	
	public HTTP2_Header createHeader(String name, String value) {
		return mapping.get(name).parse(value);
	}


}
