package com.s8.stack.arch.helium.http1.headers;


import java.io.IOException;

import com.s8.stack.arch.helium.http1.messages.HTTP1_Request;
import com.s8.stack.arch.helium.http1.messages.HTTP1_Response;

/**
 * 
 * @author pierreconvert
 *
 */
public class TransferEncoding extends HTTP1_Header {

	/**
	 * 
	 */
	public final static Prototype PROTOTYPE = new Prototype(0x06, "Transfer-Encoding"){
		
		@Override
		public HTTP1_Header create() {
			return new TransferEncoding();
		}
		
		@Override
		public void set(HTTP1_Request request, HTTP1_Header header) {
			request.transferEncoding = (TransferEncoding) header;
		}

		@Override
		public HTTP1_Header get(HTTP1_Response response) {
			return response.transferEncoding;
		}
	};
	
	public String value;

	public TransferEncoding() {
		super();
	}
	
	public TransferEncoding(String value) {
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
