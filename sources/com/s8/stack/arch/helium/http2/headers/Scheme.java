package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.FEW_STATES;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.GENERAL;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierre convert
 */
public class Scheme extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x54,
			new String[] { ":scheme"}, 
			true,
			GENERAL, 
			FEW_STATES) {

		@Override
		public HTTP2_Header parse(String value) {
			return new Scheme(value);
		}

		@Override
		public HTTP2_Header create() {
			return new Scheme();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.scheme;
		}
	};

	

	public String value;

	
	public Scheme() {
		super();
	}
	
	public Scheme(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.scheme = this;
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
