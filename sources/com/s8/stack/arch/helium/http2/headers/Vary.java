package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.ALWAYS_RENEWED;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.RESPONSE;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierre convert
 */
public class Vary extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x64,
			new String[] { "vary", "Vary", "VARY" }, 
			false,
			RESPONSE, 
			ALWAYS_RENEWED) {

		@Override
		public HTTP2_Header parse(String value) {
			return new Vary(value);
		}

		@Override
		public HTTP2_Header create() {
			return new Vary();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.vary;
		}
	};

	

	public String value;

	
	public Vary() {
		super();
	}
	
	public Vary(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.vary = this;
	}

	@Override
	public Prototype getPrototype() {
		return PROTOTYPE;
	}
	
	@Override
	public String getValue() {
		return value;
	}
	
	@Override
	public void setValue(String value) {
		this.value = value;
	}
	
}
