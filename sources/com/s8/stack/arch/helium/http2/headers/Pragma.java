package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.ALWAYS_RENEWED;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.GENERAL;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierre convert
 */
public class Pragma extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0xa1,
			new String[] { "pragma", "Pragma", "PRAGMA" }, 
			false,
			GENERAL, 
			ALWAYS_RENEWED) {

		@Override
		public HTTP2_Header parse(String value) {
			return new Pragma(value);
		}

		@Override
		public HTTP2_Header create() {
			return new Pragma();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.pragma;
		}
	};

	

	public String value;

	
	public Pragma() {
		super();
	}
	
	public Pragma(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.pragma = this;
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
