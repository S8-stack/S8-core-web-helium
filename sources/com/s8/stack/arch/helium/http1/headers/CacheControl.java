package com.s8.stack.arch.helium.http1.headers;


import java.io.IOException;

import com.s8.stack.arch.helium.http1.messages.HTTP1_Request;
import com.s8.stack.arch.helium.http1.messages.HTTP1_Response;

public class CacheControl extends HTTP1_Header {

	public final static Prototype PROTOTYPE = new Prototype(0x06, "Cache-Control"){
		public @Override HTTP1_Header create() {
			return new CacheControl();
		}
		
		@Override
		public void set(HTTP1_Request request, HTTP1_Header header) {
			request.cacheControl = (CacheControl) header;
		}

		@Override
		public HTTP1_Header get(HTTP1_Response response) {
			return response.cacheControl;
		}
	};
	
	public String value;

	public CacheControl() {
		super();
	}
	
	public CacheControl(String value) {
		super();
		this.value = value;
	}



	@Override
	public HTTP1_Header.Prototype getPrototype() {
		return PROTOTYPE;
	}
	

	@Override
	public void parse(String value) {
		this.value = value;
	}


	@Override
	public String compose() throws IOException {
		return value;
	}

}
