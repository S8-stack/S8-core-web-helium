package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.ALWAYS_RENEWED;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.REQUEST;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierre convert
 * 
 */
public class Referer extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x51,
			new String[] { "referer" }, 
			false,
			REQUEST, 
			ALWAYS_RENEWED) {

		@Override
		public HTTP2_Header parse(String value) {
			return new Referer(value);
		}

		@Override
		public HTTP2_Header create() {
			return new Referer();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.referer;
		}
	};

	
	public String value;

	public Referer() {
		super();
	}
	
	public Referer(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.referer = this;
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
