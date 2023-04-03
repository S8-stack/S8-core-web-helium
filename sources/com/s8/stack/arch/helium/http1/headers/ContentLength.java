package com.s8.stack.arch.helium.http1.headers;


import java.io.IOException;

import com.s8.stack.arch.helium.http1.messages.HTTP1_Request;
import com.s8.stack.arch.helium.http1.messages.HTTP1_Response;

public class ContentLength extends HTTP1_Header {


	/**
	 * 
	 */
	public final static Prototype PROTOTYPE = new Prototype(0x05, "Content-Length"){
		
		@Override
		public HTTP1_Header create() {
			return new ContentLength();
		}

		@Override
		public void set(HTTP1_Request request, HTTP1_Header header) {
			request.contentLength = (ContentLength) header;
		}

		@Override
		public HTTP1_Header get(HTTP1_Response response) {
			return response.contentLength;
		}
	};
	
	/**
	 * bytecount of content
	 */
	public int length;
	
	public ContentLength() {
		super();
	}

	
	public ContentLength(int length) {
		super();
		this.length = length;
	}





	@Override
	public HTTP1_Header.Prototype getPrototype() {
		return PROTOTYPE;
	}
	
	
	@Override
	public void parse(String value) {
		length = Integer.valueOf(value);
	}

	@Override
	public String compose() throws IOException {
		return Integer.toString(length);
	}

}
