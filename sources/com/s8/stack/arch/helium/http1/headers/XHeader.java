package com.s8.stack.arch.helium.http1.headers;


import java.io.IOException;

import com.s8.stack.arch.helium.http1.messages.HTTP1_Request;
import com.s8.stack.arch.helium.http1.messages.HTTP1_Response;

public class XHeader extends HTTP1_Header {


	public final static Prototype PROTOTYPE = new Prototype(0x07, "Response-Truc"){
		public @Override HTTP1_Header create() {
			return new XHeader();
		}

		@Override
		public void set(HTTP1_Request request, HTTP1_Header header) {
			
		}

		@Override
		public HTTP1_Header get(HTTP1_Response response) {
			return null;
		}
	};
	

	@Override
	public HTTP1_Header.Prototype getPrototype() {
		return PROTOTYPE;
	}
	
	
	public String type;

	
	public XHeader() {
		super();
	}
	
	public XHeader(String type){
		super();
		this.type = type;
	}
	
	
	@Override
	public void parse(String value) {
		type = value;
	}

	@Override
	public String compose() throws IOException {
		if(type!=null){
			return type;
		}
		else {
			throw new IOException("Missing type");
		}
	}
	
}
