package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.ALWAYS_RENEWED;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.REQUEST;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * @author pierreconvert
 */
public class Expect extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x32,
			new String[] { "expect" }, 
			false,
			REQUEST, 
			ALWAYS_RENEWED) {

		@Override
		public HTTP2_Header parse(String value) {
			return new Expect(value);
		}

		@Override
		public HTTP2_Header create() {
			return new Expect();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.expect;
		}
	};

	

	public String value;

	
	public Expect() {
		super();
	}
	
	public Expect(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.expect = this;
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
